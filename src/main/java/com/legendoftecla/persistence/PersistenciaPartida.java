package com.legendoftecla.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.legendoftecla.console.Consola;
import com.legendoftecla.effects.*;
import com.legendoftecla.exceptions.JuegoException;
import com.legendoftecla.model.characters.*;
import com.legendoftecla.model.elements.*;
import com.legendoftecla.model.items.*;
import com.legendoftecla.model.world.*;
import com.legendoftecla.missions.*;
import com.legendoftecla.persistence.PartidaGuardada.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Serializador de savegames con validacion de version y corrupcion. */
public final class PersistenciaPartida {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    static final int MAX_PARTIDA_BYTES = 32 * 1024 * 1024;
    private PersistenciaPartida() { }

    public static void guardar(Juego juego, Path archivo, long seed) throws JuegoException {
        try {
            String contenido = GSON.toJson(capturar(juego, seed));
            if (contenido.getBytes(StandardCharsets.UTF_8).length > MAX_PARTIDA_BYTES) {
                throw new IOException("La partida supera el limite de 32 MiB.");
            }
            EscrituraAtomica.escribir(archivo, contenido);
        } catch (IOException error) {
            throw new JuegoException("No se pudo guardar la partida: " + error.getMessage());
        }
    }

    public static Juego cargar(Path archivo, Consola consola) throws JuegoException {
        try {
            PartidaGuardada guardada = GSON.fromJson(
                    leerAcotado(archivo), PartidaGuardada.class);
            validar(guardada);
            return restaurar(guardada, consola);
        } catch (IOException | RuntimeException error) {
            throw new JuegoException("Partida corrupta o ilegible: " + error.getMessage());
        }
    }

    /** Impide reservar memoria sin limite al abrir partidas no confiables. */
    private static String leerAcotado(Path archivo) throws IOException {
        try (InputStream entrada = Files.newInputStream(archivo)) {
            byte[] datos = entrada.readNBytes(MAX_PARTIDA_BYTES + 1);
            if (datos.length > MAX_PARTIDA_BYTES) {
                throw new IOException("La partida supera el limite de 32 MiB.");
            }
            // Rechazar caracteres UTF-8 malformados, no sustituirlos silenciosamente.
            return StandardCharsets.UTF_8.newDecoder()
                    .decode(ByteBuffer.wrap(datos)).toString();
        }
    }

    public static PartidaGuardada capturar(Juego juego, long seed) {
        Mapa mapa = juego.getMapa();
        List<CeldaEstado> celdas = new ArrayList<>();
        for (int f = 0; f < mapa.getFilas(); f++) {
            for (int c = 0; c < mapa.getColumnas(); c++) {
                Celda celda = mapa.getCelda(new Posicion(f, c));
                celdas.add(new CeldaEstado(f, c, celda.getDescripcion(),
                        celda.isTerrenoTransitable(), celda.isOscura(),
                        celda.isOscuridadPermanente(), celda.getTipoSuelo() == TipoSuelo.MADERA,
                        celda.hasAntorchaMural(), celda.hasFuenteAgua(), celda.getNivelFuego(),
                        celda.getObjetos().stream().map(PersistenciaPartida::objeto).toList(),
                        celda.getElementos().stream().map(PersistenciaPartida::elemento).toList()));
            }
        }
        MapaEstado mapaEstado = new MapaEstado(mapa.getNombre(), mapa.getDescripcion(),
                mapa.getFilas(), mapa.getColumnas(), mapa.getInicio(), mapa.getObjetivo(), celdas);
        List<Aliado> registrados = juego.getAliadosRegistrados();
        java.util.Map<Aliado, Integer> indices = new java.util.IdentityHashMap<>();
        for (int i = 0; i < registrados.size(); i++) {
            indices.put(registrados.get(i), i);
        }
        List<Integer> evacuados = juego.getAliadosExtraidosDetalle().stream().map(aliado -> {
            Integer indice = indices.get(aliado);
            if (indice == null) {
                throw new IllegalStateException("Aliado evacuado no registrado.");
            }
            return indice;
        }).toList();
        return new PartidaGuardada(PartidaGuardada.VERSION_ACTUAL, seed, juego.getPasos(),
                juego.getPasosMaximos(), mapaEstado, personaje(juego.getJugador()),
                juego.getAliados().stream().map(PersistenciaPartida::personaje).toList(),
                juego.getEnemigos().stream().map(PersistenciaPartida::personaje).toList(),
                juego.getCeldasInspeccionadas(),
                juego.getMision() == null ? null : juego.getMision().getId(), juego.getPuntuacion(),
                juego.getEstadisticas().snapshot(), juego.getLogros().getDesbloqueados(),
                mision(juego.getMision()), juego.isMejorasEquipoAliadoPermitidas(),
                juego.isMunicionAliadaAutomatica(),
                registrados.stream().map(PersistenciaPartida::personaje).toList(),
                evacuados, juego.getCondicionVictoria().name(),
                juego.getFormacionAliada().name());
    }

    private static void validar(PartidaGuardada guardada) throws JuegoException {
        if (guardada == null) throw new JuegoException("El JSON no contiene una partida.");
        if (guardada.version() != PartidaGuardada.VERSION_ACTUAL
                && guardada.version() != PartidaGuardada.VERSION_INICIAL) {
            throw new JuegoException("Version de partida no compatible: " + guardada.version());
        }
        if (guardada.mapa() == null || guardada.jugador() == null
                || guardada.mapa().celdas() == null || guardada.aliados() == null
                || guardada.enemigos() == null) {
            throw new JuegoException("Faltan datos obligatorios de partida.");
        }
        validarIntegridadMapa(guardada.mapa());
        if (guardada.version() == PartidaGuardada.VERSION_ACTUAL) {
            if (guardada.aliadosRegistrados() == null || guardada.indicesExtraidos() == null
                    || guardada.condicionVictoria() == null || guardada.formacionAliada() == null) {
                throw new JuegoException("Falta el historial de evacuacion de la partida v2.");
            }
            int total = guardada.aliadosRegistrados().size();
            java.util.Set<Integer> indices = new java.util.HashSet<>();
            for (Integer indice : guardada.indicesExtraidos()) {
                if (indice == null || indice < 0 || indice >= total || !indices.add(indice)) {
                    throw new JuegoException("Indices de evacuados corruptos.");
                }
            }
            if (total > com.legendoftecla.validation.Limites.ALIADOS_MAXIMOS
                    || total - indices.size() != guardada.aliados().size()) {
                throw new JuegoException("Historial de aliados inconsistente.");
            }
        }
    }

    /**
     * Un savegame debe describir todas las celdas del mapa una sola vez.
     * De lo contrario, la restauracion puede dejar referencias nulas o
     * reemplazar silenciosamente el contenido de una casilla duplicada.
     */
    private static void validarIntegridadMapa(MapaEstado mapa) throws JuegoException {
        int filas = mapa.filas();
        int columnas = mapa.columnas();
        if (filas < com.legendoftecla.validation.Limites.MAPA_MINIMO
                || filas > com.legendoftecla.validation.Limites.MAPA_MAXIMO
                || columnas < com.legendoftecla.validation.Limites.MAPA_MINIMO
                || columnas > com.legendoftecla.validation.Limites.MAPA_MAXIMO) {
            throw new JuegoException("Dimensiones de mapa fuera de los limites permitidos.");
        }
        if (mapa.celdas().size() != filas * columnas) {
            throw new JuegoException("Numero de celdas del mapa inconsistente.");
        }
        boolean[] ocupadas = new boolean[filas * columnas];
        for (CeldaEstado celda : mapa.celdas()) {
            if (celda == null || celda.fila() < 0 || celda.fila() >= filas
                    || celda.columna() < 0 || celda.columna() >= columnas) {
                throw new JuegoException("Celda del mapa nula o fuera de limites.");
            }
            int indice = celda.fila() * columnas + celda.columna();
            if (ocupadas[indice]) {
                throw new JuegoException("La partida contiene celdas del mapa duplicadas.");
            }
            ocupadas[indice] = true;
        }
    }

    private static Juego restaurar(PartidaGuardada estado, Consola consola) {
        MapaEstado datos = estado.mapa();
        Mapa mapa = new Mapa(datos.nombre(), datos.descripcion(), datos.filas(), datos.columnas(),
                datos.inicio(), datos.objetivo());
        for (CeldaEstado celdaEstado : datos.celdas()) {
            Celda celda = new Celda(celdaEstado.descripcion(), celdaEstado.transitable());
            celda.setOscura(celdaEstado.oscura());
            celda.setOscuridadPermanente(celdaEstado.oscuridadPermanente());
            celda.setTipoSuelo(celdaEstado.madera() ? TipoSuelo.MADERA : TipoSuelo.PIEDRA);
            celda.setAntorchaMural(celdaEstado.antorcha());
            celda.setFuenteAgua(celdaEstado.fuente());
            celda.setNivelFuego(celdaEstado.fuego());
            celda.setObjetos(lista(celdaEstado.objetos()).stream()
                    .map(PersistenciaPartida::crearObjeto).toList());
            celda.setElementos(lista(celdaEstado.elementos()).stream()
                    .map(PersistenciaPartida::crearElemento).toList());
            mapa.setCelda(celdaEstado.fila(), celdaEstado.columna(), celda);
        }
        Jugador jugador = (Jugador) crearPersonaje(estado.jugador());
        Juego juego = new Juego(consola, mapa, jugador, estado.pasosMaximos());
        juego.setPasos(estado.turnos());
        juego.setPuntuacion(estado.puntuacion());
        juego.setCeldasInspeccionadas(estado.inspeccionadas());
        juego.setMejorasEquipoAliadoPermitidas(estado.mejorasEquipoAliado() == null
                || estado.mejorasEquipoAliado());
        juego.setMunicionAliadaAutomatica(estado.municionAliadaAutomatica() == null
                || estado.municionAliadaAutomatica());
        if (estado.version() == PartidaGuardada.VERSION_ACTUAL) {
            restaurarEscuadron(juego, estado);
        } else {
            estado.aliados().stream().map(PersistenciaPartida::crearPersonaje)
                    .map(Aliado.class::cast).forEach(juego::agregarAliado);
        }
        estado.enemigos().stream().map(PersistenciaPartida::crearPersonaje)
                .map(Enemigo.class::cast).forEach(juego::agregarEnemigo);
        juego.getEstadisticas().restaurar(estado.estadisticas());
        juego.getLogros().restaurar(estado.logros());
        if (estado.mision() != null) juego.setMision(crearMision(estado.mision(), mapa));
        com.legendoftecla.engine.EquilibradorBandos.aplicar(juego);
        return juego;
    }

    /**
     * Reconstruye exactamente una instancia por aliado registrado, evitando
     * referencias duplicadas entre activos y evacuados.
     */
    private static void restaurarEscuadron(Juego juego, PartidaGuardada estado) {
        List<Aliado> registrados = estado.aliadosRegistrados().stream()
                .map(PersistenciaPartida::crearPersonaje).map(Aliado.class::cast).toList();
        java.util.Set<Integer> indicesExtraidos = new java.util.HashSet<>(estado.indicesExtraidos());
        List<Aliado> activos = new ArrayList<>();
        List<Aliado> evacuados = new ArrayList<>();
        for (int i = 0; i < registrados.size(); i++) {
            Aliado aliado = registrados.get(i);
            if (indicesExtraidos.contains(i)) {
                evacuados.add(aliado);
            } else {
                activos.add(aliado);
            }
        }
        juego.setAliadosRegistrados(registrados);
        juego.setAliados(activos);
        juego.setAliadosIniciales(registrados.size());
        juego.setAliadosExtraidosDetalle(evacuados);
        juego.setAliadosExtraidos(evacuados.size());
        juego.setCondicionVictoria(
                com.legendoftecla.constants.CondicionVictoria.valueOf(estado.condicionVictoria()));
        juego.setFormacionAliada(
                com.legendoftecla.constants.FormacionAliada.valueOf(estado.formacionAliada()));
    }

    private static PersonajeEstado personaje(Personaje personaje) {
        int nivel = personaje instanceof Jugador jugador ? jugador.getProgresion().getNivel()
                : personaje instanceof Aliado aliado ? aliado.getNivel() : 0;
        int experiencia = personaje instanceof Jugador jugador
                ? jugador.getProgresion().getExperiencia()
                : personaje instanceof Aliado aliado ? aliado.getProgresion().getExperiencia() : 0;
        java.util.Set<String> habilidades = personaje instanceof Jugador jugador
                ? jugador.getProgresion().getDesbloqueadas() : java.util.Set.of();
        return new PersonajeEstado(personaje.getClass().getSimpleName(), personaje.getNombre(),
                personaje.getSalud(), personaje.getSaludMaxima(), personaje.getEnergia(),
                personaje.getEnergiaMaxima(), personaje.getVisionBase(), personaje.getPosicion(),
                personaje.getMochila().getCapacidadMax(), personaje.getMochila().getPesoMax(),
                personaje.getMochila().getObjetos().stream().map(PersistenciaPartida::objeto).toList(),
                personaje.getArmasEquipadas().stream().map(PersistenciaPartida::objeto).toList(),
                personaje.getArmaduraEquipada() == null ? null : objeto(personaje.getArmaduraEquipada()),
                personaje.getEstados().getActivos(), nivel, experiencia, habilidades,
                personaje instanceof Aliado aliado ? aliado.getRol().name() : null,
                personaje instanceof Enemigo enemigo
                        ? enemigo.getMultiplicadorDanioPartida() : null);
    }

    private static Personaje crearPersonaje(PersonajeEstado estado) {
        Mochila mochila = new Mochila(estado.capacidad(), estado.pesoMax());
        mochila.setObjetos(lista(estado.inventario()).stream()
                .map(PersistenciaPartida::crearObjeto).toList());
        Personaje personaje = switch (estado.tipo()) {
            case "Francotirador" -> new Francotirador(estado.nombre(), estado.posicion(), mochila, estado.vision());
            case "Zapador" -> new Zapador(estado.nombre(), estado.posicion(), mochila, estado.vision());
            case "Aliado" -> new Aliado(estado.nombre(), estado.posicion(), mochila, estado.vision());
            case "HeavyFloater" -> new HeavyFloater(estado.nombre(), estado.posicion(), mochila, estado.vision());
            case "LightFloater" -> new LightFloater(estado.nombre(), estado.posicion(), mochila, estado.vision());
            case "Berserker" -> new Berserker(estado.nombre(), estado.posicion(), mochila, estado.vision());
            case "Medic" -> new Medic(estado.nombre(), estado.posicion(), mochila, estado.vision());
            case "Sniper" -> new Sniper(estado.nombre(), estado.posicion(), mochila, estado.vision());
            case "Pyro" -> new Pyro(estado.nombre(), estado.posicion(), mochila, estado.vision());
            case "Scout" -> new Scout(estado.nombre(), estado.posicion(), mochila, estado.vision());
            case "Commander" -> new Commander(estado.nombre(), estado.posicion(), mochila, estado.vision());
            case "CommanderPrime" -> new CommanderPrime(estado.nombre(), estado.posicion(), mochila, estado.vision());
            case "PyroOverlord" -> new PyroOverlord(estado.nombre(), estado.posicion(), mochila, estado.vision());
            case "Sectoid" -> new Sectoid(estado.nombre(), estado.posicion(), mochila, estado.vision());
            default -> new Marine(estado.nombre(), estado.posicion(), mochila, estado.vision());
        };
        personaje.configurarEstadisticas(estado.saludMaxima(), estado.energiaMaxima(), estado.vision());
        personaje.setSalud(estado.salud()); personaje.setEnergia(estado.energia());
        FaccionEquipo faccionHistorica = personaje instanceof Enemigo
                ? FaccionEquipo.ENEMIGA : FaccionEquipo.HUMANA;
        personaje.setArmasEquipadas(lista(estado.armas()).stream()
                .map(objeto -> crearObjeto(objeto, faccionHistorica))
                .map(Arma.class::cast).toList());
        if (estado.armadura() != null) personaje.setArmaduraEquipada(
                (Armadura) crearObjeto(estado.armadura(), faccionHistorica));
        lista(estado.estados()).forEach(activo -> personaje.getEstados().restaurar(
                efecto(activo.tipo()), activo.turnosRestantes(), activo.acumulaciones()));
        if (personaje instanceof Enemigo enemigo && estado.multiplicadorDanio() != null) {
            enemigo.setMultiplicadorDanioPartida(estado.multiplicadorDanio());
        }
        if (personaje instanceof Jugador jugador && estado.nivel() > 0) {
            jugador.getProgresion().restaurar(estado.nivel(), estado.experiencia(),
                    estado.habilidades());
        } else if (personaje instanceof Aliado aliado) {
            if (estado.nivel() > 0) {
                aliado.getProgresion().restaurar(estado.nivel(), estado.experiencia(), java.util.Set.of());
            }
            if (estado.rolAliado() != null) aliado.setRol(
                    RolAliado.valueOf(estado.rolAliado()));
        }
        return personaje;
    }

    private static ObjetoEstado objeto(Objeto objeto) {
        if (objeto instanceof Arma arma) return new ObjetoEstado("Arma", arma.getNombre(),
                arma.getDescripcion(), arma.getPeso(), arma.getDanio(), arma.getCapacidadCargador(),
                arma.getMunicionActual(), arma.isDosManos(), arma.getTipoMunicion().name(),
                arma.getFaccion().name(), arma.getCategoria().name(),
                arma.getClaseArma().name(), arma.getPenetracionArmadura());
        if (objeto instanceof Armadura a) return new ObjetoEstado("Armadura", a.getNombre(),
                a.getDescripcion(), a.getPeso(), a.getDefensa(), a.getBonusSalud(),
                a.getBonusEnergia(), false, null, a.getFaccion().name(), null, null, 0);
        if (objeto instanceof Botiquin b) return simple(objeto, "Botiquin", b.getCuracion());
        if (objeto instanceof Binocular b) return simple(objeto, "Binocular", b.getRango());
        if (objeto instanceof Linterna l) return simple(objeto, "Linterna", l.getAlcance());
        if (objeto instanceof ToritoRojo t) return simple(objeto, "ToritoRojo", t.getEnergiaTurno());
        if (objeto instanceof CuboAgua c) return new ObjetoEstado("CuboAgua", c.getNombre(),
                c.getDescripcion(), c.getPeso(), 0, 0, 0, c.isLleno(), null, null, null, null, 0);
        if (objeto instanceof Municion m) return new ObjetoEstado("Municion", m.getNombre(),
                m.getDescripcion(), m.getPeso(), m.getCantidad(), 0, 0, false,
                m.getTipo().name(), null, null, null, 0);
        if (objeto instanceof Credencial c) return new ObjetoEstado("Credencial", c.getNombre(),
                c.getDescripcion(), c.getPeso(), 0, 0, 0, false,
                c.getCodigo(), null, null, null, 0);
        if (objeto instanceof Componente) return simple(objeto, "Componente", 0);
        return simple(objeto, "Explosivo", 0);
    }

    private static ObjetoEstado simple(Objeto o, String tipo, int valor) {
        return new ObjetoEstado(tipo, o.getNombre(), o.getDescripcion(), o.getPeso(),
                valor, 0, 0, false, null, null, null, null, 0);
    }

    private static Objeto crearObjeto(ObjetoEstado o) {
        return crearObjeto(o, FaccionEquipo.HUMANA);
    }

    private static Objeto crearObjeto(ObjetoEstado o, FaccionEquipo porDefecto) {
        FaccionEquipo faccion = o.faccion() == null
                ? porDefecto : FaccionEquipo.valueOf(o.faccion());
        return switch (o.tipo()) {
            case "Arma" -> crearArma(o, faccion);
            case "Armadura" -> new Armadura(o.nombre(), o.descripcion(), o.peso(),
                    o.valor(), o.valor2(), o.valor3(), faccion);
            case "Botiquin" -> new Botiquin(o.nombre(), o.descripcion(), o.peso(), o.valor());
            case "Binocular" -> new Binocular(o.nombre(), o.descripcion(), o.peso(), o.valor());
            case "Linterna" -> new Linterna(o.nombre(), o.descripcion(), o.peso(), o.valor());
            case "ToritoRojo" -> new ToritoRojo(o.nombre(), o.descripcion(), o.peso(), o.valor());
            case "CuboAgua" -> new CuboAgua(o.nombre(), o.descripcion(), o.peso(), o.bandera());
            case "Municion" -> new Municion(o.nombre(), o.peso(),
                    TipoMunicion.valueOf(o.subtipo()), o.valor());
            case "Credencial" -> new Credencial(o.nombre(), o.descripcion(), o.peso(), o.subtipo());
            case "Componente" -> new Componente(o.nombre(), o.descripcion(), o.peso());
            default -> new Explosivo(o.nombre(), o.descripcion(), o.peso());
        };
    }

    private static Arma crearArma(ObjetoEstado o, FaccionEquipo faccion) {
        CategoriaArma categoria = o.categoria() == null
                ? inferirCategoria(TipoMunicion.valueOf(o.subtipo()), o.nombre())
                : CategoriaArma.valueOf(o.categoria());
        if (o.claseArma() == null) {
            return new Arma(o.nombre(), o.descripcion(), o.peso(), o.valor(), o.bandera(),
                    categoria, TipoMunicion.valueOf(o.subtipo()), o.valor2(), o.valor3(), faccion);
        }
        return new Arma(o.nombre(), o.descripcion(), o.peso(), o.valor(), o.bandera(),
                categoria, TipoMunicion.valueOf(o.subtipo()), o.valor2(), o.valor3(), faccion,
                ClaseArma.valueOf(o.claseArma()), o.penetracionArmadura());
    }

    private static CategoriaArma inferirCategoria(TipoMunicion tipo, String nombre) {
        return switch (tipo) {
            case FLECHA -> CategoriaArma.ARCO;
            case VIROTE -> CategoriaArma.BALLESTA;
            case CUCHILLO_ARROJADIZO -> CategoriaArma.ARROJADIZA;
            case INFINITA -> nombre != null && nombre.toLowerCase(java.util.Locale.ROOT)
                    .matches(".*(espada|mandoble|garra).*")
                            ? CategoriaArma.MELE : CategoriaArma.FUEGO;
            default -> CategoriaArma.FUEGO;
        };
    }

    private static ElementoEstado elemento(ElementoMapa elemento) {
        ElementoBase base = (ElementoBase) elemento;
        if (elemento instanceof Puerta puerta) {
            return estadoElemento(elemento, puerta.getEstado().name(), puerta.getCredencial(),
                    base.getResistencia(), base.isDestructible(), 0, 0, 0, 0,
                    false, false, List.of());
        }
        if (elemento instanceof Barricada barricada) {
            return estadoElemento(elemento, barricada.getCobertura().name(),
                    barricada.getOrientacion().name(), base.getResistencia(), true,
                    0, 0, 0, 0, false, false, List.of());
        }
        if (elemento instanceof ParedDebil) {
            return estadoElemento(elemento, null, null, base.getResistencia(), true,
                    0, 0, 0, 0, false, false, List.of());
        }
        if (elemento instanceof Terminal terminal) {
            return estadoElemento(elemento, null, terminal.getObjetivoId(), 1, false,
                    terminal.getDificultad(), 0, 0, 0,
                    terminal.isHackeado(), false, List.of());
        }
        if (elemento instanceof Interruptor interruptor) {
            return estadoElemento(elemento, null, interruptor.getObjetivoId(), 1, false,
                    0, 0, 0, 0, interruptor.isActivo(), false, List.of());
        }
        if (elemento instanceof Cofre cofre) {
            return estadoElemento(elemento, null, null, base.getResistencia(), true,
                    0, 0, 0, 0, cofre.isAbierto(), false,
                    cofre.getContenido().stream().map(PersistenciaPartida::objeto).toList());
        }
        if (elemento instanceof Trampa trampa) {
            return estadoElemento(elemento, trampa.isRemota() ? "REMOTA" : null,
                    null, base.getResistencia(), true,
                    trampa.getDetectabilidad(), trampa.getDificultadDesactivacion(),
                    trampa.getDano(), trampa.getRadio(), trampa.isActiva(),
                    trampa.isDetectada(), List.of());
        }
        throw new IllegalArgumentException("Elemento no persistible: " + elemento.getClass().getName());
    }

    private static ElementoEstado estadoElemento(ElementoMapa elemento, String estado,
            String referencia, int resistencia, boolean destructible,
            int valor1, int valor2, int valor3, int valor4,
            boolean bandera1, boolean bandera2, List<ObjetoEstado> objetos) {
        return new ElementoEstado(elemento.getClass().getSimpleName(), elemento.getId(),
                estado, referencia, resistencia, destructible, valor1, valor2,
                valor3, valor4, bandera1, bandera2, objetos);
    }

    private static ElementoMapa crearElemento(ElementoEstado estado) {
        ElementoMapa elemento = switch (estado.tipo()) {
            case "Puerta" -> new Puerta(estado.id(), EstadoPuerta.valueOf(estado.estado()),
                    estado.referencia(), estado.destructible(), Math.max(1, estado.resistencia()));
            case "Barricada" -> new Barricada(estado.id(), Math.max(1, estado.resistencia()),
                    TipoCobertura.valueOf(estado.estado()),
                    OrientacionCobertura.valueOf(estado.referencia()));
            case "ParedDebil" -> new ParedDebil(estado.id(), Math.max(1, estado.resistencia()));
            case "Terminal" -> new Terminal(estado.id(), estado.valor1(), estado.referencia());
            case "Interruptor" -> new Interruptor(estado.id(), estado.bandera1(), estado.referencia());
            case "Cofre" -> new Cofre(estado.id(), lista(estado.objetos()).stream()
                    .map(PersistenciaPartida::crearObjeto).toList());
            case "Mina" -> new Mina(estado.id(), estado.valor3(), estado.valor4(),
                    "REMOTA".equals(estado.estado()));
            case "TrampaFuego" -> new TrampaFuego(estado.id());
            case "TrampaVeneno" -> new TrampaVeneno(estado.id());
            case "TrampaElectrica" -> new TrampaElectrica(estado.id());
            case "Alarma" -> new Alarma(estado.id());
            default -> throw new IllegalArgumentException(
                    "Tipo de elemento desconocido: " + estado.tipo());
        };
        restaurarEstadoElemento(elemento, estado);
        return elemento;
    }

    private static void restaurarEstadoElemento(ElementoMapa elemento, ElementoEstado estado) {
        if (elemento instanceof Terminal terminal && estado.bandera1()) {
            terminal.hackear(Integer.MAX_VALUE);
        }
        if (elemento instanceof Cofre cofre) {
            if (estado.bandera1()) cofre.abrir();
            cofre.recibirDanio(Math.max(0, 20 - Math.max(0, estado.resistencia())));
        }
        if (elemento instanceof Trampa trampa) {
            if (estado.bandera2()) trampa.detectar(personajeAuxiliar(), Integer.MAX_VALUE);
            if (!estado.bandera1()) trampa.disparar(List.of());
        }
        if ((elemento instanceof Puerta || elemento instanceof Barricada
                || elemento instanceof ParedDebil)
                && estado.resistencia() == 0) {
            elemento.recibirDanio(1);
        }
    }

    private static Personaje personajeAuxiliar() {
        return new Marine("Restaurador", new Posicion(0, 0), new Mochila(1, 1), 1);
    }

    private static <T> List<T> lista(List<T> valores) {
        return valores == null ? List.of() : valores;
    }

    private static MisionEstado mision(Mision mision) {
        if (mision == null) return null;
        return new MisionEstado(mision.getId(), mision.getNombre(),
                objetivo(mision.getPrincipal()),
                mision.getSecundarios().stream().map(PersistenciaPartida::objetivo).toList(),
                mision.getRecompensas());
    }

    private static ObjetivoEstado objetivo(ObjetivoMision objetivo) {
        if (objetivo instanceof AlcanzarSalida) return objetivo("SALIDA", null, 0, null, null);
        if (objetivo instanceof EliminarJefe jefe) {
            return objetivo("JEFE", jefe.getNombre(), 0, null, null);
        }
        if (objetivo instanceof EliminarEnemigo enemigo) {
            return objetivo("ENEMIGO", enemigo.getNombre(), 0, null, null);
        }
        if (objetivo instanceof RescatarPersonaje rescate) {
            return objetivo("RESCATAR", rescate.getNombre(), 0, null, null);
        }
        if (objetivo instanceof RecuperarObjeto recuperar) {
            return objetivo("RECUPERAR", recuperar.getNombre(), 0, null, null);
        }
        if (objetivo instanceof ActivarTerminal terminal) {
            return objetivo("TERMINAL", terminal.getTerminal().getId(), 0, null, null);
        }
        if (objetivo instanceof SobrevivirTurnos turnos) {
            return objetivo("SOBREVIVIR", null, turnos.getTurnos(), null, null);
        }
        if (objetivo instanceof EscoltarPersonaje escolta) {
            return objetivo("ESCOLTAR", escolta.getNombre(), 0, null, null);
        }
        if (objetivo instanceof ApagarIncendio incendio) {
            return objetivo("INCENDIO", null, 0, incendio.getPosicion(), null);
        }
        if (objetivo instanceof NoPerderAliados) {
            return objetivo("SIN_BAJAS", null, 0, null, null);
        }
        if (objetivo instanceof CompletarSinDisparar sinDisparar) {
            return objetivo("SIN_DISPARAR", null, 0, null,
                    objetivo(sinDisparar.getFinalizacion()));
        }
        throw new IllegalArgumentException(
                "Objetivo no persistible: " + objetivo.getClass().getName());
    }

    private static ObjetivoEstado objetivo(String tipo, String argumento, int valor,
            Posicion posicion, ObjetivoEstado anidado) {
        return new ObjetivoEstado(tipo, argumento, valor, posicion, anidado);
    }

    private static Mision crearMision(MisionEstado estado, Mapa mapa) {
        return new Mision(estado.id(), estado.nombre(), crearObjetivo(estado.principal(), mapa),
                lista(estado.secundarios()).stream()
                        .map(objetivo -> crearObjetivo(objetivo, mapa)).toList(),
                lista(estado.recompensas()));
    }

    private static ObjetivoMision crearObjetivo(ObjetivoEstado estado, Mapa mapa) {
        if (estado == null) throw new IllegalArgumentException("Objetivo ausente");
        return switch (estado.tipo()) {
            case "SALIDA" -> new AlcanzarSalida();
            case "JEFE" -> new EliminarJefe(estado.argumento());
            case "ENEMIGO" -> new EliminarEnemigo(estado.argumento());
            case "RESCATAR" -> new RescatarPersonaje(estado.argumento());
            case "RECUPERAR" -> new RecuperarObjeto(estado.argumento());
            case "TERMINAL" -> new ActivarTerminal(buscarTerminal(mapa, estado.argumento()));
            case "SOBREVIVIR" -> new SobrevivirTurnos(estado.valor());
            case "ESCOLTAR" -> new EscoltarPersonaje(estado.argumento());
            case "INCENDIO" -> new ApagarIncendio(estado.posicion());
            case "SIN_BAJAS" -> new NoPerderAliados();
            case "SIN_DISPARAR" -> new CompletarSinDisparar(
                    crearObjetivo(estado.anidado(), mapa));
            default -> throw new IllegalArgumentException(
                    "Tipo de objetivo desconocido: " + estado.tipo());
        };
    }

    private static Terminal buscarTerminal(Mapa mapa, String id) {
        for (int fila = 0; fila < mapa.getFilas(); fila++) {
            for (int columna = 0; columna < mapa.getColumnas(); columna++) {
                for (ElementoMapa elemento : mapa.getCelda(
                        new Posicion(fila, columna)).getElementos()) {
                    if (elemento instanceof Terminal terminal && terminal.getId().equals(id)) {
                        return terminal;
                    }
                }
            }
        }
        throw new IllegalArgumentException("Terminal de mision inexistente: " + id);
    }

    private static EfectoEstado efecto(TipoEstado tipo) {
        return switch (tipo) {
            case QUEMADO -> new Quemado(); case ENVENENADO -> new Envenenado();
            case SANGRADO -> new Sangrado(); case ATURDIDO -> new Aturdido();
            case CEGADO -> new Cegado(); case MOJADO -> new Mojado();
            case EXHAUSTO -> new Exhausto(); case ASUSTADO -> new Asustado();
            case INSPIRADO -> new Inspirado();
        };
    }
}
