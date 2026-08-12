package com.legendoftecla.loader;

import com.legendoftecla.console.Consola;
import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.constants.GameConstants;
import com.legendoftecla.exceptions.JuegoException;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Francotirador;
import com.legendoftecla.model.characters.HeavyFloater;
import com.legendoftecla.model.characters.Jugador;
import com.legendoftecla.model.characters.LightFloater;
import com.legendoftecla.model.characters.Marine;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.characters.Zapador;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Armadura;
import com.legendoftecla.model.items.Binocular;
import com.legendoftecla.model.items.Botiquin;
import com.legendoftecla.model.items.Explosivo;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.items.ToritoRojo;
import com.legendoftecla.model.items.CuboAgua;
import com.legendoftecla.model.items.Linterna;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.DimensionesMapa;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.model.world.TipoSuelo;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Random;

/** Carga el formato completo generado por el editor grafico. */
public final class CargadorJuegoJson extends CargadorJuegoBase {
    private Path directorio;

    /**
     * Crea una instancia de {@code CargadorJuegoJson}.
      * @param clase valor de {@code clase}
      * @param consola valor de {@code consola}
      * @param dificultad valor de {@code dificultad}
      * @param dimensiones valor de {@code dimensiones}
      * @param directorio valor de {@code directorio}
      * @param nombreJugador valor de {@code nombreJugador}
      * @param conAliados indica si se deben generar aliados automaticamente
     */
    public CargadorJuegoJson(Consola consola, String nombreJugador, String clase, Path directorio,
            Dificultad dificultad, DimensionesMapa dimensiones, boolean conAliados) {
        super(consola, nombreJugador, clase, dificultad, dimensiones, conAliados);
        setDirectorio(directorio);
    }

    /** @return directorio JSON normalizado */
    public Path getDirectorio() {
        return directorio;
    }

    /** @param directorio directorio no nulo */
    public void setDirectorio(Path directorio) {
        this.directorio = com.legendoftecla.validation.Validaciones
                .noNulo(directorio, "Directorio JSON").normalize();
    }

    @Override
    public Juego cargarJuego() throws JuegoException {
        EscenarioDefinicion definicion = SerializadorEscenarioJson.cargar(directorio);
        int filas = dimensiones == null ? definicion.getFilas() : dimensiones.filas();
        int columnas = dimensiones == null ? definicion.getColumnas() : dimensiones.columnas();
        if (filas < definicion.getFilas() || columnas < definicion.getColumnas()) {
            throw new JuegoException("Las dimensiones configuradas no pueden recortar el escenario JSON.");
        }

        Posicion inicio = posicion(definicion.getInicio());
        Posicion objetivo = posicion(definicion.getObjetivo());
        Mapa mapa = new Mapa(definicion.getNombre(), definicion.getDescripcion(),
                filas, columnas, inicio, objetivo);
        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                mapa.setCelda(fila, columna, new Celda("Celda " + fila + "," + columna, true));
            }
        }
        for (EscenarioDefinicion.CeldaDef celda : definicion.getCeldas()) {
            Celda cargada = new Celda(celda.getDescripcion(), celda.isTransitable());
            cargada.setOscuridadPermanente(celda.isOscura());
            cargada.setTipoSuelo(celda.isSueloMadera() ? TipoSuelo.MADERA : TipoSuelo.PIEDRA);
            cargada.setAntorchaMural(celda.hasAntorchaMural());
            cargada.setFuenteAgua(celda.hasFuenteAgua());
            cargada.setNivelFuego(celda.getNivelFuego());
            cargarElemento(cargada, celda);
            mapa.setCelda(celda.getFila(), celda.getColumna(), cargada);
        }

        Jugador jugador = crearJugador(inicio);
        Juego juego = new Juego(consola, mapa, jugador, definicion.getPasosMaximos());
        Enemigo.setMultiplicadorDanioGlobal(dificultad.getMultiplicadorDanioEnemigo());

        for (EscenarioDefinicion.ObjetoDef objetoDef : definicion.getObjetos()) {
            Posicion posicion = posicion(objetoDef);
            exigirTransitable(mapa, posicion, "objeto " + objetoDef.getNombre());
            mapa.getCelda(posicion).agregarObjeto(crearObjeto(objetoDef));
        }
        GeneradorAmbiente.completar(mapa, new Random(311));
        GeneradorSuministrosDificultad.poblar(mapa, dificultad, new Random(307));

        int cantidadEnemigos = dificultad.ajustarCantidadEnemigos(definicion.getEnemigos().size());
        for (int indice = 0; indice < cantidadEnemigos; indice++) {
            EscenarioDefinicion.PersonajeDef personajeDef =
                    definicion.getEnemigos().get(indice % definicion.getEnemigos().size());
            Posicion posicion = posicion(personajeDef);
            exigirTransitable(mapa, posicion, "enemigo " + personajeDef.getNombre());
            String nombre = indice < definicion.getEnemigos().size()
                    ? personajeDef.getNombre()
                    : personajeDef.getNombre() + "_extra_" + indice;
            Enemigo enemigo = crearEnemigo(personajeDef, nombre, posicion);
            enemigo.escalarSalud(dificultad.getMultiplicadorSaludEnemigo());
            mapa.getCelda(posicion).agregarEnemigo(enemigo);
            juego.agregarEnemigo(enemigo);
        }

        int cantidadAliados = conAliados
                ? GeneradorAliados.poblar(juego, mapa, dificultad, new Random(303), "AliadoJson")
                : 0;
        if (definicion.getMision() != null) {
            juego.setMision(crearMision(definicion.getMision(), juego));
        }

        consola.imprimirInfo("Escenario JSON cargado: " + definicion.getNombre()
                + " | dificultad=" + dificultad.getEtiqueta()
                + " | enemigos=" + cantidadEnemigos
                + " | aliados=" + cantidadAliados);
        return juego;
    }

    private Jugador crearJugador(Posicion inicio) {
        Mochila mochila = new Mochila(GameConstants.MOCHILA_CAPACIDAD_MAX, GameConstants.MOCHILA_PESO_MAX);
        return switch (clase.toLowerCase(Locale.ROOT)) {
            case "marine" -> new Marine(nombreJugador, inicio, mochila, GameConstants.MAX_VISION_BASE);
            case "francotirador" -> new Francotirador(
                    nombreJugador, inicio, mochila, GameConstants.MAX_VISION_BASE);
            default -> new Zapador(nombreJugador, inicio, mochila, GameConstants.MAX_VISION_BASE);
        };
    }

    private Enemigo crearEnemigo(EscenarioDefinicion.PersonajeDef definicion,
            String nombre, Posicion posicion) {
        Mochila mochila = new Mochila(8, 30);
        Enemigo enemigo = switch (definicion.getTipo().toLowerCase(Locale.ROOT)) {
            case "berserker" -> new com.legendoftecla.model.characters.Berserker(
                    nombre, posicion, mochila, definicion.getVision());
            case "medic" -> new com.legendoftecla.model.characters.Medic(
                    nombre, posicion, mochila, definicion.getVision());
            case "sniper" -> new com.legendoftecla.model.characters.Sniper(
                    nombre, posicion, mochila, definicion.getVision());
            case "pyro" -> new com.legendoftecla.model.characters.Pyro(
                    nombre, posicion, mochila, definicion.getVision());
            case "scout" -> new com.legendoftecla.model.characters.Scout(
                    nombre, posicion, mochila, definicion.getVision());
            case "commander" -> new com.legendoftecla.model.characters.Commander(
                    nombre, posicion, mochila, definicion.getVision());
            case "commanderprime" -> new com.legendoftecla.model.characters.CommanderPrime(
                    nombre, posicion, mochila, definicion.getVision());
            case "pyrooverlord" -> new com.legendoftecla.model.characters.PyroOverlord(
                    nombre, posicion, mochila, definicion.getVision());
            case "lightfloater", "light_floater" -> new LightFloater(
                    nombre, posicion, mochila, definicion.getVision());
            case "heavyfloater", "heavy_floater" -> new HeavyFloater(
                    nombre, posicion, mochila, definicion.getVision());
            default -> new Sectoid(nombre, posicion, mochila, definicion.getVision());
        };
        if (enemigo instanceof com.legendoftecla.ai.EnemigoTactico) {
            com.legendoftecla.engine.ArsenalEnemigo.asignar(enemigo, dificultad);
        }
        enemigo.configurarEstadisticas(
                definicion.getSalud(), definicion.getEnergia(), definicion.getVision());
        return enemigo;
    }

    private void cargarElemento(Celda destino, EscenarioDefinicion.CeldaDef origen) {
        if (origen.getElementoTipo() == null || origen.getElementoTipo().isBlank()) return;
        String id = origen.getElementoId() == null ? "elemento-" + origen.getFila()
                + "-" + origen.getColumna() : origen.getElementoId();
        String tipo = origen.getElementoTipo().toLowerCase(Locale.ROOT);
        com.legendoftecla.model.elements.ElementoMapa elemento = switch (tipo) {
            case "puerta" -> new com.legendoftecla.model.elements.Puerta(id,
                    origen.getElementoEstado() == null
                            ? com.legendoftecla.model.elements.EstadoPuerta.CERRADA
                            : com.legendoftecla.model.elements.EstadoPuerta.valueOf(
                                    origen.getElementoEstado().toUpperCase(Locale.ROOT)),
                    origen.getReferencia(), true, origen.getResistencia());
            case "terminal" -> new com.legendoftecla.model.elements.Terminal(
                    id, origen.getDificultad(), origen.getReferencia());
            case "interruptor" -> new com.legendoftecla.model.elements.Interruptor(
                    id, false, origen.getReferencia());
            case "cofre" -> new com.legendoftecla.model.elements.Cofre(id, java.util.List.of());
            case "barricada", "cobertura" -> new com.legendoftecla.model.elements.Barricada(
                    id, origen.getResistencia(), com.legendoftecla.model.elements.TipoCobertura.COMPLETA,
                    com.legendoftecla.model.elements.OrientacionCobertura.TODAS);
            case "pared_debil", "pareddebil" ->
                    new com.legendoftecla.model.elements.ParedDebil(
                            id, origen.getResistencia());
            case "mina", "trampa" -> new com.legendoftecla.model.elements.Mina(id, 20, 1, false);
            case "trampa_fuego" -> new com.legendoftecla.model.elements.TrampaFuego(id);
            case "trampa_veneno" -> new com.legendoftecla.model.elements.TrampaVeneno(id);
            case "trampa_electrica" -> new com.legendoftecla.model.elements.TrampaElectrica(id);
            case "alarma" -> new com.legendoftecla.model.elements.Alarma(id);
            default -> null;
        };
        if (elemento != null) destino.agregarElemento(elemento);
    }

    private Objeto crearObjeto(EscenarioDefinicion.ObjetoDef definicion) {
        String tipo = definicion.getTipo().toLowerCase(Locale.ROOT);
        String descripcion = definicion.getDescripcion();
        return switch (tipo) {
            case "arma" -> crearArma(definicion, descripcion);
            case "armadura" -> new Armadura(definicion.getNombre(), descripcion, definicion.getPeso(),
                    definicion.getValor(), definicion.getValorSecundario(),
                    definicion.getValorTerciario());
            case "binocular", "radar" -> new Binocular(
                    definicion.getNombre(), descripcion, definicion.getPeso(),
                    Math.max(1, definicion.getValor()));
            case "torito", "toritorojo", "energia" -> new ToritoRojo(
                    definicion.getNombre(), descripcion, definicion.getPeso(),
                    Math.max(1, definicion.getValor()));
            case "explosivo" -> new Explosivo(
                    definicion.getNombre(), descripcion, definicion.getPeso());
            case "granada" -> new com.legendoftecla.model.items.Granada(
                    definicion.getNombre(), descripcion, definicion.getPeso(),
                    com.legendoftecla.model.items.TipoGranada.valueOf(
                            (definicion.getTipoGranada() == null ? "FRAGMENTACION"
                                    : definicion.getTipoGranada()).toUpperCase(Locale.ROOT)));
            case "linterna" -> new Linterna(definicion.getNombre(), descripcion,
                    definicion.getPeso(), Math.max(1, definicion.getValor()));
            case "cubo", "cuboagua", "cubo_agua" -> new CuboAgua(definicion.getNombre(), descripcion,
                    definicion.getPeso(), definicion.getValor() > 0);
            case "municion" -> new com.legendoftecla.model.items.Municion(
                    definicion.getNombre(), definicion.getPeso(),
                    tipoMunicion(definicion), definicion.getCantidad() > 0
                            ? definicion.getCantidad() : Math.max(0, definicion.getValor()));
            case "credencial", "llave", "tarjeta" -> new com.legendoftecla.model.items.Credencial(
                    definicion.getNombre(), descripcion, definicion.getPeso(), definicion.getNombre());
            case "componente" -> new com.legendoftecla.model.items.Componente(
                    definicion.getNombre(), descripcion, definicion.getPeso());
            default -> new Botiquin(definicion.getNombre(), descripcion, definicion.getPeso(),
                    Math.max(1, definicion.getValor()));
        };
    }

    private Arma crearArma(EscenarioDefinicion.ObjetoDef definicion, String descripcion) {
        if (definicion.getTipoMunicion() == null
                || definicion.getTipoMunicion().isBlank()) {
            return new Arma(definicion.getNombre(), descripcion, definicion.getPeso(),
                    Math.max(1, definicion.getValor()), definicion.isDosManos());
        }
        if (definicion.getCategoriaArma() == null
                || definicion.getCategoriaArma().isBlank()) {
            return new Arma(definicion.getNombre(), descripcion, definicion.getPeso(),
                    Math.max(1, definicion.getValor()), definicion.isDosManos(),
                    tipoMunicion(definicion), definicion.getCapacidadCargador(),
                    definicion.getMunicionActual());
        }
        return new Arma(definicion.getNombre(), descripcion, definicion.getPeso(),
                Math.max(1, definicion.getValor()), definicion.isDosManos(),
                com.legendoftecla.model.items.CategoriaArma.valueOf(
                        definicion.getCategoriaArma().toUpperCase(Locale.ROOT)),
                tipoMunicion(definicion), definicion.getCapacidadCargador(),
                definicion.getMunicionActual());
    }

    private com.legendoftecla.model.items.TipoMunicion tipoMunicion(
            EscenarioDefinicion.ObjetoDef definicion) {
        String tipo = definicion.getTipoMunicion();
        if (tipo == null || tipo.isBlank()) {
            tipo = definicion.getDescripcion();
        }
        return com.legendoftecla.model.items.TipoMunicion.valueOf(
                tipo.toUpperCase(Locale.ROOT));
    }

    private com.legendoftecla.missions.Mision crearMision(
            EscenarioDefinicion.MisionDef definicion, Juego juego) throws JuegoException {
        java.util.List<com.legendoftecla.missions.ObjetivoMision> secundarios = new java.util.ArrayList<>();
        for (EscenarioDefinicion.ObjetivoDef secundario : definicion.getSecundarios()) {
            secundarios.add(crearObjetivo(secundario, juego));
        }
        return new com.legendoftecla.missions.Mision(definicion.getId(), definicion.getNombre(),
                crearObjetivo(definicion.getPrincipal(), juego), secundarios,
                definicion.getRecompensas());
    }

    private com.legendoftecla.missions.ObjetivoMision crearObjetivo(
            EscenarioDefinicion.ObjetivoDef definicion, Juego juego) throws JuegoException {
        String tipo = definicion.getTipo().toLowerCase(Locale.ROOT);
        String argumento = definicion.getArgumento();
        return switch (tipo) {
            case "alcanzar_salida", "salida" -> new com.legendoftecla.missions.AlcanzarSalida();
            case "eliminar_enemigo" -> new com.legendoftecla.missions.EliminarEnemigo(argumento);
            case "eliminar_jefe" -> new com.legendoftecla.missions.EliminarJefe(argumento);
            case "rescatar" -> new com.legendoftecla.missions.RescatarPersonaje(argumento);
            case "recuperar_objeto" -> new com.legendoftecla.missions.RecuperarObjeto(argumento);
            case "sobrevivir_turnos" -> new com.legendoftecla.missions.SobrevivirTurnos(
                    Math.max(1, definicion.getValor()));
            case "escoltar" -> new com.legendoftecla.missions.EscoltarPersonaje(argumento);
            case "apagar_incendio" -> new com.legendoftecla.missions.ApagarIncendio(
                    posicionObjetivo(definicion, juego));
            case "no_perder_aliados" -> new com.legendoftecla.missions.NoPerderAliados();
            case "sin_disparar" -> new com.legendoftecla.missions.CompletarSinDisparar(
                    new com.legendoftecla.missions.AlcanzarSalida());
            case "activar_terminal" -> new com.legendoftecla.missions.ActivarTerminal(
                    buscarTerminal(juego, argumento));
            default -> throw new JuegoException("Tipo de objetivo desconocido: " + tipo);
        };
    }

    private Posicion posicionObjetivo(EscenarioDefinicion.ObjetivoDef definicion, Juego juego) {
        return definicion.getPosicion() == null
                ? juego.getMapa().getObjetivo() : posicion(definicion.getPosicion());
    }

    private com.legendoftecla.model.elements.Terminal buscarTerminal(Juego juego, String id)
            throws JuegoException {
        for (int fila = 0; fila < juego.getMapa().getFilas(); fila++) {
            for (int columna = 0; columna < juego.getMapa().getColumnas(); columna++) {
                for (var elemento : juego.getMapa().getCelda(new Posicion(fila, columna)).getElementos()) {
                    if (elemento instanceof com.legendoftecla.model.elements.Terminal terminal
                            && terminal.getId().equals(id)) {
                        return terminal;
                    }
                }
            }
        }
        throw new JuegoException("Terminal de mision inexistente: " + id);
    }

    private Posicion posicion(EscenarioDefinicion.Punto punto) {
        return new Posicion(punto.getFila(), punto.getColumna());
    }

    private void exigirTransitable(Mapa mapa, Posicion posicion, String elemento) throws JuegoException {
        if (!mapa.esTransitable(posicion)) {
            throw new JuegoException("La posicion de " + elemento + " no es transitable.");
        }
    }
}
