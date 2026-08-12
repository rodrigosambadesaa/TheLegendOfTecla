package com.legendoftecla.engine;

import com.legendoftecla.commands.CommandContext;
import com.legendoftecla.commands.CommandParser;
import com.legendoftecla.commands.Comando;
import com.legendoftecla.commands.ComandoRecorrido;
import com.legendoftecla.commands.ComandoSalir;
import com.legendoftecla.commands.ComandoDescansar;
import com.legendoftecla.constants.FormacionAliada;
import com.legendoftecla.console.TipoMensaje;
import com.legendoftecla.audio.EventoSonido;
import com.legendoftecla.audio.GestorSonido;
import com.legendoftecla.audio.SuscriptorAudioEventos;
import com.legendoftecla.ai.EnemigoTactico;
import com.legendoftecla.ai.SistemaTurnosIA;
import com.legendoftecla.ai.SistemaRuido;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.events.MisionCompletada;
import com.legendoftecla.inventory.CooperacionInventario;
import com.legendoftecla.inventory.ServicioRecarga;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.items.Binocular;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Armadura;
import com.legendoftecla.model.items.Botiquin;
import com.legendoftecla.model.items.Explosivo;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.items.ToritoRojo;
import com.legendoftecla.model.items.Linterna;
import com.legendoftecla.model.items.CuboAgua;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.model.world.SistemaPuntuacion;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;

import java.util.HashSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Controla una partida completa sin conocer si se muestra en consola o en GUI.
 */
public final class MotorPartida {
    private static final int TURNOS_AYUDA_MINIMOS = 8;
    private Juego juego;
    private CommandContext contexto;
    private CommandParser parser;
    private Random random;
    private Map<Aliado, SituacionAliado> situacionesAliados;
    private Map<Aliado, Boolean> aliadosEnCombate;
    private boolean finalizada;
    private SistemaPuntuacion.EstadoFinalPartida estadoFinal;
    private int turnosAyudaAliados;
    private boolean avisoRescateEnergia;
    private boolean cooperacionInventarioActiva;
    private SistemaTurnosIA sistemaTurnosIA;
    private SistemaRuido sistemaRuido;

    /**
     * Crea una instancia de {@code MotorPartida}.
      * @param juego valor de {@code juego}
     */
    public MotorPartida(Juego juego) {
        setJuego(juego);
        setSistemaTurnosIA(new SistemaTurnosIA());
        setSistemaRuido(new SistemaRuido(juego));
        SuscriptorAudioEventos.registrar(juego);
        setContexto(new CommandContext(juego));
        setParser(new CommandParser(contexto));
        setRandom(new Random());
        Map<Aliado, SituacionAliado> situacionesIniciales = new HashMap<>();
        Map<Aliado, Boolean> combatesIniciales = new HashMap<>();
        juego.getAliadosRegistrados().forEach(aliado -> {
            situacionesIniciales.put(aliado, SituacionAliado.ACTIVO);
            combatesIniciales.put(aliado, false);
        });
        setSituacionesAliados(situacionesIniciales);
        setAliadosEnCombate(combatesIniciales);
        setFinalizada(false);
        setEstadoFinal(null);
        setTurnosAyudaAliados(0);
        setAvisoRescateEnergia(false);
        setCooperacionInventarioActiva(true);
        anunciarPartida();
        evaluarFinNatural();
    }

    /**
     * Obtiene el valor de {@code Juego}.
      * @return resultado de la operacion
     */
    public Juego getJuego() {
        return juego;
    }

    /** @param juego partida no nula */
    public void setJuego(Juego juego) {
        this.juego = Validaciones.noNulo(juego, "Juego");
    }

    /** @return fachada formal de percepcion, decision y ejecucion enemiga */
    public SistemaTurnosIA getSistemaTurnosIA() { return sistemaTurnosIA; }
    /** @param sistemaTurnosIA fachada no nula */
    public void setSistemaTurnosIA(SistemaTurnosIA sistemaTurnosIA) {
        this.sistemaTurnosIA = Validaciones.noNulo(sistemaTurnosIA, "Sistema de turnos IA");
    }
    /** @return distribuidor de ruido activo */
    public SistemaRuido getSistemaRuido() { return sistemaRuido; }
    /** @param sistemaRuido distribuidor no nulo; cierra la suscripcion anterior */
    public void setSistemaRuido(SistemaRuido sistemaRuido) {
        if (this.sistemaRuido != null) this.sistemaRuido.close();
        this.sistemaRuido = Validaciones.noNulo(sistemaRuido, "Sistema de ruido");
    }

    /** @return contexto de comandos */
    public CommandContext getContexto() { return contexto; }
    /** @param contexto contexto no nulo */
    public void setContexto(CommandContext contexto) {
        this.contexto = Validaciones.noNulo(contexto, "Contexto");
    }
    /** @return interprete de comandos */
    public CommandParser getParser() { return parser; }
    /** @param parser interprete no nulo */
    public void setParser(CommandParser parser) {
        this.parser = Validaciones.noNulo(parser, "Interprete de comandos");
    }
    /** @return generador aleatorio */
    public Random getRandom() { return random; }
    /** @param random generador no nulo */
    public void setRandom(Random random) {
        this.random = Validaciones.noNulo(random, "Generador aleatorio");
    }
    /** @return copia de las situaciones aliadas */
    public Map<Aliado, SituacionAliado> getSituacionesAliados() {
        return Map.copyOf(situacionesAliados);
    }
    /** @param situacionesAliados estados no nulos y acotados */
    public void setSituacionesAliados(Map<Aliado, SituacionAliado> situacionesAliados) {
        Validaciones.noNulo(situacionesAliados, "Situaciones aliadas");
        if (situacionesAliados.size() > Limites.ESTADISTICA || situacionesAliados.entrySet().stream()
                .anyMatch(e -> e.getKey() == null || e.getValue() == null)) {
            throw new IllegalArgumentException("Las situaciones aliadas no son validas.");
        }
        this.situacionesAliados = new HashMap<>(situacionesAliados);
    }
    /** @return copia del estado de combate aliado */
    public Map<Aliado, Boolean> getAliadosEnCombate() { return Map.copyOf(aliadosEnCombate); }
    /** @param aliadosEnCombate estados no nulos y acotados */
    public void setAliadosEnCombate(Map<Aliado, Boolean> aliadosEnCombate) {
        Validaciones.noNulo(aliadosEnCombate, "Combates aliados");
        if (aliadosEnCombate.size() > Limites.ESTADISTICA || aliadosEnCombate.entrySet().stream()
                .anyMatch(e -> e.getKey() == null || e.getValue() == null)) {
            throw new IllegalArgumentException("Los estados de combate aliado no son validos.");
        }
        this.aliadosEnCombate = new HashMap<>(aliadosEnCombate);
    }

    /**
     * Indica el estado de {@code Finalizada}.
      * @return resultado de la operacion
     */
    public boolean isFinalizada() {
        return finalizada;
    }

    /** @param finalizada estado de finalizacion */
    public void setFinalizada(boolean finalizada) { this.finalizada = finalizada; }

    /**
     * Obtiene el valor de {@code EstadoFinal}.
      * @return resultado de la operacion
     */
    public SistemaPuntuacion.EstadoFinalPartida getEstadoFinal() {
        return estadoFinal;
    }

    /** @param estadoFinal resultado final o {@code null} mientras continua */
    public void setEstadoFinal(SistemaPuntuacion.EstadoFinalPartida estadoFinal) {
        this.estadoFinal = estadoFinal;
    }

    /** @return turnos restantes de la orden de ayuda */
    public int getTurnosAyudaAliados() { return turnosAyudaAliados; }
    /** @param turnosAyudaAliados cantidad no negativa y acotada */
    public void setTurnosAyudaAliados(int turnosAyudaAliados) {
        this.turnosAyudaAliados = Validaciones.enteroEntre(
                turnosAyudaAliados, 0, Limites.PASOS_MAXIMOS, "Turnos de ayuda");
    }
    /** @return si ya se mostro el aviso de rescate */
    public boolean isAvisoRescateEnergia() { return avisoRescateEnergia; }
    /** @param avisoRescateEnergia estado del aviso */
    public void setAvisoRescateEnergia(boolean avisoRescateEnergia) {
        this.avisoRescateEnergia = avisoRescateEnergia;
    }
    /** @return si los aliados pueden compartir municion y mejoras automaticamente */
    public boolean isCooperacionInventarioActiva() { return cooperacionInventarioActiva; }
    /** @param activa habilita o deshabilita la cooperacion automatica */
    public void setCooperacionInventarioActiva(boolean activa) {
        cooperacionInventarioActiva = activa;
    }

    /**
     * Obtiene el valor de {@code EstadoJugador}.
      * @return resultado de la operacion
     */
    public String getEstadoJugador() {
        return juego.getJugador().getNombre()
                + "  Salud " + juego.getJugador().getSalud() + "/" + juego.getJugador().getSaludMaxima()
                + "  Energia " + juego.getJugador().getEnergia() + "/" + juego.getJugador().getEnergiaMaxima()
                + "  Pasos " + juego.getPasos() + "/" + juego.getPasosMaximos()
                + "  Formacion " + juego.getFormacionAliada().getEtiqueta()
                + "  Efectos " + SistemaEstados.resumen(juego.getJugador())
                + (turnosAyudaAliados > 0 ? "  Ayuda aliada " + turnosAyudaAliados : "");
    }

    /**
     * Genera el estado completo y persistente de todos los aliados de la partida.
     *
     * @return resumen con vida, energia, posicion, objetos, equipo y situacion de cada aliado
     */
    public String getEstadoAliados() {
        List<Aliado> aliados = juego.getAliadosRegistrados();
        if (aliados.isEmpty()) {
            return "Aliados: ninguno.";
        }
        long evacuados = aliados.stream().filter(juego::estaAliadoExtraido).count();
        long caidos = aliados.stream().filter(aliado -> aliado.getSalud() <= 0).count();
        long enCombate = aliados.stream().filter(this::estaAliadoEnCombate).count();
        StringJoiner lineas = new StringJoiner("\n");
        lineas.add("ALIADOS " + aliados.size() + " | activos=" + (aliados.size() - evacuados - caidos)
                + " | en combate=" + enCombate + " | evacuados=" + evacuados + " | caidos=" + caidos);
        for (Aliado aliado : aliados) {
            SituacionAliado situacion = obtenerSituacionAliado(aliado);
            String posicion = juego.estaAliadoExtraido(aliado)
                    ? "salida " + aliado.getPosicion()
                    : aliado.getPosicion().toString();
            lineas.add("- " + aliado.getNombre() + " | Estado " + situacion.etiqueta
                    + " | Combate " + (estaAliadoEnCombate(aliado) ? "EN COMBATE" : "FUERA DE COMBATE")
                    + " | Vida " + aliado.getSalud() + "/" + aliado.getSaludMaxima()
                    + " | Energia " + aliado.getEnergia() + "/" + aliado.getEnergiaMaxima()
                    + " | Posicion " + posicion
                    + " | Efectos " + SistemaEstados.resumen(aliado));
            lineas.add("  Objetos: " + listarObjetos(aliado) + " | Equipo: " + listarEquipo(aliado));
        }
        return lineas.toString();
    }

    /**
     * Ejecuta la operacion publica {@code ejecutarComando}.
      * @param linea valor de {@code linea}
      * @return resultado de la operacion
     */
    public boolean ejecutarComando(String linea) {
        if (finalizada) {
            return false;
        }
        juego.getJugador().resetTurno();
        try {
            Comando comando = parser.parse(linea == null ? "" : linea);
            SistemaEstados.iniciarTurno(juego);
            if (juego.getJugador().getSalud() <= 0) {
                evaluarFinNatural();
                return false;
            }
            boolean aturdido = juego.getJugador().getEstados().consumirBloqueoAccion();
            if (aturdido) {
                juego.getConsola().imprimirAdvertencia(
                        "Estas aturdido y pierdes la accion de este turno.");
            } else {
                comando.ejecutar();
            }
            if (!aturdido && comando instanceof ComandoSalir) {
                finalizar(SistemaPuntuacion.EstadoFinalPartida.SALIDA_MANUAL);
                return false;
            }
            if (contexto.getJuego() != juego) {
                reemplazarJuegoCargado(contexto.getJuego());
                anunciarPartida();
                evaluarFinNatural();
                return !finalizada;
            }
            if (juego.consumirSolicitudAyudaAliados()) {
                setTurnosAyudaAliados(Math.max(TURNOS_AYUDA_MINIMOS,
                        juego.getMapa().getFilas() + juego.getMapa().getColumnas()));
            }
            ejecutarTurnoAliados();
            ejecutarTurnoNPC(comando instanceof ComandoDescansar);
            avanzarOrdenAyuda();
            SistemaIncendios.avanzarTurno(juego, random);
            SistemaEstados.finalizarTurno(juego);
        } catch (ComandoException e) {
            juego.getConsola().imprimir("Error de comando: " + e.getMessage(), TipoMensaje.ERROR);
        } catch (Exception e) {
            juego.getConsola().imprimir("Error inesperado: " + e.getMessage(), TipoMensaje.ERROR);
        }
        evaluarFinNatural();
        return !finalizada;
    }

    private void reemplazarJuegoCargado(Juego cargado) {
        setJuego(cargado);
        setSistemaRuido(new SistemaRuido(cargado));
        SuscriptorAudioEventos.registrar(cargado);
        Map<Aliado, SituacionAliado> situaciones = new HashMap<>();
        Map<Aliado, Boolean> combates = new HashMap<>();
        cargado.getAliadosRegistrados().forEach(aliado -> {
            situaciones.put(aliado, SituacionAliado.ACTIVO);
            combates.put(aliado, false);
        });
        setSituacionesAliados(situaciones);
        setAliadosEnCombate(combates);
        setTurnosAyudaAliados(0);
        setAvisoRescateEnergia(false);
        setEstadoFinal(null);
        setFinalizada(false);
    }

    /**
     * Indica si los aliados tienen activa una orden para acudir al jugador.
     *
     * @return {@code true} mientras queden turnos de ayuda
     */
    public boolean isAyudaAliadaActiva() {
        return turnosAyudaAliados > 0;
    }

    /**
     * Obtiene el valor de {@code AliadosVisibles}.
      * @return resultado de la operacion
     */
    public Set<Posicion> getAliadosVisibles() {
        Set<Posicion> visibles = new HashSet<>();
        for (Aliado aliado : juego.getAliados()) {
            if (aliado.getSalud() > 0 && SistemaIluminacion.hayLuz(juego, aliado.getPosicion())) {
                visibles.add(aliado.getPosicion());
            }
        }
        return visibles;
    }

    /**
     * Obtiene el valor de {@code EnemigosVisibles}.
      * @return resultado de la operacion
     */
    public Set<Posicion> getEnemigosVisibles() {
        Set<Posicion> visibles = new HashSet<>();
        Posicion jugadorPos = juego.getJugador().getPosicion();
        int vision = juego.getJugador().getRangoVision();
        for (Enemigo enemigo : juego.getEnemigos()) {
            if (enemigo.getSalud() > 0
                    && jugadorPos.distanciaManhattan(enemigo.getPosicion()) <= vision
                    && SistemaIluminacion.hayLuz(juego, enemigo.getPosicion())) {
                visibles.add(enemigo.getPosicion());
            }
        }
        return visibles;
    }

    /** @return todas las celdas que pueden representarse con detalle pese a la oscuridad */
    public Set<Posicion> getCeldasIluminadas() {
        Set<Posicion> iluminadas = new HashSet<>();
        for (int f = 0; f < juego.getMapa().getFilas(); f++) {
            for (int c = 0; c < juego.getMapa().getColumnas(); c++) {
                Posicion posicion = new Posicion(f, c);
                if (SistemaIluminacion.hayLuz(juego, posicion)) iluminadas.add(posicion);
            }
        }
        return iluminadas;
    }

    /** Indica si una celda tiene luz suficiente para mostrar su contenido. */
    public boolean hayLuzEn(Posicion posicion) { return SistemaIluminacion.hayLuz(juego, posicion); }

    private void anunciarPartida() {
        juego.getConsola().imprimir("Mapa: " + juego.getMapa().getNombre(), TipoMensaje.INFO);
        juego.getConsola().imprimir(juego.getMapa().getDescripcion(), TipoMensaje.INFO);
        if (!juego.getAliados().isEmpty()) {
            juego.getConsola().imprimir("Aliados desplegados: " + juego.getAliados().size(), TipoMensaje.INFO);
            juego.getConsola().imprimir(
                    "Condicion de victoria: " + juego.getCondicionVictoria().getEtiqueta() + ".",
                    TipoMensaje.INFO);
        }
    }

    private void evaluarFinNatural() {
        if (finalizada) {
            return;
        }
        if (juego.getJugador().getEnergia() > 0) {
            setAvisoRescateEnergia(false);
        }
        if (juego.jugadorGano()) {
            finalizar(SistemaPuntuacion.EstadoFinalPartida.VICTORIA);
            return;
        }
        if (juego.getJugador().getSalud() <= 0) {
            finalizar(SistemaPuntuacion.EstadoFinalPartida.MUERTE);
            return;
        }
        if (juego.getJugador().getEnergia() <= 0) {
            if (!hayRescateEnergiaPosible()) {
                juego.getConsola().imprimirAdvertencia(
                        "Rescate imposible: no queda ningun Torito que un aliado pueda entregar sin agotarse.");
                finalizar(SistemaPuntuacion.EstadoFinalPartida.MUERTE);
                return;
            }
            if (!avisoRescateEnergia) {
                setAvisoRescateEnergia(true);
                juego.getConsola().imprimirAdvertencia(
                        "Te has quedado inmovilizado. Pide ayuda: hay un Torito que un aliado puede entregar.");
            }
        }
        if (juego.excedioPasos()) {
            finalizar(SistemaPuntuacion.EstadoFinalPartida.SIN_PASOS);
        }
    }

    private void finalizar(SistemaPuntuacion.EstadoFinalPartida estado) {
        if (finalizada) {
            return;
        }
        setFinalizada(true);
        setEstadoFinal(Validaciones.noNulo(estado, "Estado final"));
        if (estado == SistemaPuntuacion.EstadoFinalPartida.VICTORIA) {
            juego.publicarEvento(new MisionCompletada(
                    juego.getBusEventos().ahora(), juego.getMision() == null
                            ? "victoria-original" : juego.getMision().getId()));
        }
        switch (estado) {
            case VICTORIA -> juego.getConsola().imprimir("Has llegado al objetivo. Victoria.", TipoMensaje.EXITO);
            case MUERTE -> juego.getConsola().imprimir(
                    "Has muerto o te has quedado sin energia.", TipoMensaje.ERROR);
            case SIN_PASOS -> juego.getConsola().imprimir(
                    "Superaste el numero maximo de pasos.", TipoMensaje.ADVERTENCIA);
            case SALIDA_MANUAL -> juego.getConsola().imprimir("Partida finalizada.", TipoMensaje.INFO);
        }
        if (!juego.getAliadosRegistrados().isEmpty()) {
            juego.getConsola().imprimir(getEstadoAliados(), TipoMensaje.ESTADO);
        }

        SistemaPuntuacion.ResultadoPuntuacion puntuacion = SistemaPuntuacion.calcular(juego, estado);
        juego.setPuntuacion(puntuacion.getTotal());
        for (String linea : puntuacion.formatearDesglose()) {
            juego.getConsola().imprimir(linea, TipoMensaje.INFO);
        }
        new ComandoRecorrido(contexto).ejecutar();
    }

    private void ejecutarTurnoNPC(boolean jugadorDescansando) {
        List<Enemigo> snapshot = List.copyOf(juego.getEnemigos());
        for (Enemigo enemigo : snapshot) {
            if (enemigo.getSalud() <= 0) {
                continue;
            }
            if (enemigo.getEstados().consumirBloqueoAccion()) {
                continue;
            }
            if (enemigo instanceof EnemigoTactico
                    || enemigo instanceof com.legendoftecla.model.characters.Jefe) {
                sistemaTurnosIA.ejecutar(juego, enemigo, random);
                continue;
            }
            boolean formacionDetectada = enemigoDetectaFormacion(enemigo);
            Personaje objetivoTactico = formacionDetectada ? seleccionarObjetivoTactico(enemigo) : null;
            if (formacionDetectada) {
                juego.getConsola().imprimirInfo(enemigo.getNombre() + " detecta la formacion "
                        + juego.getFormacionAliada().getEtiqueta() + " y adapta su ataque.");
            }
            int movimientos = jugadorDescansando || formacionDetectada
                    ? Math.max(1, random.nextInt(3)) : random.nextInt(3);
            for (int i = 0; i < movimientos; i++) {
                Personaje objetivo = objetivoTactico == null ? juego.getJugador() : objetivoTactico;
                int distancia = enemigo.getPosicion().distanciaManhattan(objetivo.getPosicion());
                if (distancia <= enemigo.getRangoVision()
                        && juego.getMapa().hayLineaAtaque(enemigo.getPosicion(), objetivo.getPosicion())) {
                    if (!enemigo.puedeAtacar()) {
                        intentarRecargar(enemigo);
                        break;
                    }
                    SistemaCombate.atacar(juego, enemigo, objetivo, random);
                    break;
                }
                Direccion direccion = jugadorDescansando || formacionDetectada
                        ? buscarPrimerPasoEnemigo(enemigo.getPosicion(), objetivo.getPosicion())
                        : Direccion.values()[random.nextInt(Direccion.values().length)];
                if (direccion == null) {
                    break;
                }
                Posicion origen = enemigo.getPosicion();
                Posicion destino = origen.mover(direccion);
                if (juego.getMapa().esTransitable(destino)) {
                    juego.getMapa().getCelda(origen).quitarEnemigo(enemigo);
                    try {
                        enemigo.mover(direccion, juego);
                        juego.getMapa().getCelda(enemigo.getPosicion()).agregarEnemigo(enemigo);
                        GestorSonido.reproducir(EventoSonido.MOVIMIENTO,
                                enemigo.getPosicion(), juego.getJugador().getPosicion());
                        if (jugadorDescansando && !formacionDetectada) {
                            juego.getConsola().imprimirInfo(
                                    enemigo.getNombre() + " se acerca mientras descansas.");
                        }
                    } catch (Exception ignored) {
                        juego.getMapa().getCelda(origen).agregarEnemigo(enemigo);
                    }
                }
            }
        }
    }

    private void ejecutarTurnoAliados() {
        List<Aliado> aliados = List.copyOf(juego.getAliados());
        for (Aliado aliado : aliados) {
            if (aliado.getSalud() <= 0) {
                cambiarSituacion(aliado, SituacionAliado.CAIDO);
                marcarCombate(aliado, false);
                continue;
            }
            aliado.resetTurno();
            if (aliado.getEstados().consumirBloqueoAccion()) {
                juego.getConsola().imprimirAdvertencia(
                        aliado.getNombre() + " esta aturdido y pierde su accion.");
                continue;
            }
            gestionarLinternaAliada(aliado);
            usarBinocularSiConviene(aliado);
            marcarCombate(aliado, false);
            SituacionAliado anterior = situacionesAliados.getOrDefault(aliado, SituacionAliado.ACTIVO);
            cambiarSituacion(aliado, anterior == SituacionAliado.EN_COMBATE
                    || anterior == SituacionAliado.FUERA_DE_COMBATE
                            ? SituacionAliado.FUERA_DE_COMBATE
                            : SituacionAliado.ACTIVO);
            if (extraerAliadoSiProcede(aliado)) {
                continue;
            }
            interactuarConObjetos(aliado);
            if (evacuarAliadoSiTieneLaSalidaAlAlcance(aliado)) {
                continue;
            }
            if (turnosAyudaAliados > 0 && prepararAliadoParaAyuda(aliado)) {
                continue;
            }
            if (priorizarAyudaJugador(aliado)) {
                continue;
            }
            if (turnosAyudaAliados > 0) {
                if (buscarSuministroNecesarioParaJugador(aliado)) {
                    continue;
                }
                ejecutarOrdenAyuda(aliado);
                continue;
            }
            if (juego.getFormacionAliada() != FormacionAliada.SIN_FORMACION) {
                ejecutarFormacion(aliado);
                extraerAliadoSiProcede(aliado);
                continue;
            }
            if (asistirAliadoPrioritario(aliado)) {
                continue;
            }
            if (explorarSuministrosDesconocidos(aliado)) {
                continue;
            }
            Enemigo objetivo = buscarEnemigoMasCercano(aliado);
            if (objetivo == null) {
                if (situacionesAliados.get(aliado) != SituacionAliado.FUERA_DE_COMBATE) {
                    cambiarSituacion(aliado, SituacionAliado.ACOMPANANDO);
                }
                Posicion destino = juego.getJugador().getPosicion().equals(juego.getMapa().getObjetivo())
                        ? juego.getMapa().getObjetivo()
                        : juego.getJugador().getPosicion();
                moverAliadoHaciaObjetivo(aliado, destino);
                extraerAliadoSiProcede(aliado);
                continue;
            }
            cambiarSituacion(aliado, SituacionAliado.EN_COMBATE);
            marcarCombate(aliado, true);
            if (puedeAliadoAtacarA(aliado, objetivo)) {
                if (!debeAliadoAtacarConRadar(aliado, objetivo)) {
                    continue;
                }
                SistemaCombate.atacar(juego, aliado, objetivo, random);
                if (objetivo.getSalud() <= 0) {
                    eliminarEnemigoDerrotado(aliado, objetivo);
                }
            } else if (!aliado.puedeAtacar()) {
                intentarRecargar(aliado);
            } else {
                moverAliadoHaciaObjetivo(aliado, objetivo.getPosicion());
            }
        }
    }

    private boolean priorizarAyudaJugador(Aliado aliado) {
        Personaje jugador = juego.getJugador();
        if (asistirJugador(aliado)) {
            return true;
        }
        boolean necesitaVida = jugador.getSalud() < jugador.getSaludMaxima();
        boolean necesitaEnergia = jugador.getEnergia() < jugador.getEnergiaMaxima();
        boolean llevaAyuda = necesitaVida
                && aliado.getMochila().getObjetos().stream().anyMatch(Botiquin.class::isInstance)
                || necesitaEnergia
                && aliado.getMochila().getObjetos().stream().anyMatch(ToritoRojo.class::isInstance);
        int distanciaJugador = aliado.getPosicion().distanciaManhattan(jugador.getPosicion());
        if (llevaAyuda && distanciaJugador > 1) {
            cambiarSituacion(aliado, SituacionAliado.ACUDIENDO);
            moverAliadoHaciaObjetivo(aliado, jugador.getPosicion());
            return true;
        }
        Enemigo amenaza = buscarEnemigoCercanoAlJugador();
        if (amenaza != null) {
            cambiarSituacion(aliado, SituacionAliado.EN_COMBATE);
            marcarCombate(aliado, true);
            if (puedeAliadoAtacarA(aliado, amenaza)) {
                if (debeAliadoAtacarConRadar(aliado, amenaza)) {
                    SistemaCombate.atacar(juego, aliado, amenaza, random);
                    eliminarEnemigoDerrotado(aliado, amenaza);
                }
            } else if (!aliado.puedeAtacar()) {
                intentarRecargar(aliado);
            } else {
                moverAliadoHaciaObjetivo(aliado, amenaza.getPosicion());
            }
            return true;
        }
        Enemigo amenazaInmediata = buscarEnemigoMasCercano(aliado);
        if (amenazaInmediata != null
                && aliado.getPosicion().distanciaManhattan(amenazaInmediata.getPosicion()) <= 1) {
            cambiarSituacion(aliado, SituacionAliado.EN_COMBATE);
            marcarCombate(aliado, true);
            if (puedeAliadoAtacarA(aliado, amenazaInmediata)
                    && debeAliadoAtacarConRadar(aliado, amenazaInmediata)) {
                SistemaCombate.atacar(juego, aliado, amenazaInmediata, random);
                eliminarEnemigoDerrotado(aliado, amenazaInmediata);
            }
            return true;
        }
        if ((necesitaVida || necesitaEnergia) && buscarSuministroNecesarioParaJugador(aliado)) {
            return true;
        }
        if (distanciaJugador > 2) {
            cambiarSituacion(aliado, SituacionAliado.ACOMPANANDO);
            moverAliadoHaciaObjetivo(aliado, jugador.getPosicion());
            return true;
        }
        return false;
    }

    private void ejecutarOrdenAyuda(Aliado aliado) {
        if (!puedeAcudirSinPeligro(aliado)) {
            cambiarSituacion(aliado, SituacionAliado.EN_ESPERA_POR_RIESGO);
            juego.getConsola().imprimirInfo(
                    aliado.getNombre() + " no acude: su vida correria peligro.");
            return;
        }
        Posicion jugador = juego.getJugador().getPosicion();
        if (aliado.getPosicion().distanciaManhattan(jugador) > 1) {
            cambiarSituacion(aliado, SituacionAliado.ACUDIENDO);
            moverAliadoHaciaObjetivo(aliado, jugador);
            return;
        }
        Enemigo objetivo = buscarEnemigoCercanoAlJugador();
        if (objetivo == null) {
            cambiarSituacion(aliado, SituacionAliado.PROTEGIENDO);
            return;
        }
        cambiarSituacion(aliado, SituacionAliado.EN_COMBATE);
        marcarCombate(aliado, true);
        if (puedeAliadoAtacarA(aliado, objetivo)) {
            if (debeAliadoAtacarConRadar(aliado, objetivo)) {
                SistemaCombate.atacar(juego, aliado, objetivo, random);
                eliminarEnemigoDerrotado(aliado, objetivo);
            }
        } else if (!aliado.puedeAtacar()) {
            intentarRecargar(aliado);
        } else {
            moverAliadoHaciaObjetivo(aliado, objetivo.getPosicion());
        }
    }

    private boolean puedeAcudirSinPeligro(Aliado aliado) {
        double saludRelativa = (double) aliado.getSalud() / Math.max(1, aliado.getSaludMaxima());
        return saludRelativa >= 0.55 && estimarRiesgoRecibido(aliado) < aliado.getSalud() * 0.50;
    }

    private boolean prepararAliadoParaAyuda(Aliado aliado) {
        int distancia = calcularDistanciaRutaAliado(aliado.getPosicion(), juego.getJugador().getPosicion());
        if (distancia < 0) {
            cambiarSituacion(aliado, SituacionAliado.SIN_RUTA);
            juego.getConsola().imprimirAdvertencia(
                    aliado.getNombre() + " no puede asistir: no existe una ruta hasta el jugador.");
            return true;
        }
        int pasosNecesarios = Math.max(0, distancia - 1);
        int costeMovimiento = aliado.estimarCosteMovimiento();
        int reservaEnergia = Math.max(costeMovimiento * 2,
                (int) Math.ceil(aliado.getEnergiaMaxima() * 0.15));
        int energiaNecesaria = pasosNecesarios * costeMovimiento + reservaEnergia;
        int riesgo = estimarRiesgoRecibido(aliado);
        int saludNecesaria = Math.min(aliado.getSaludMaxima(),
                Math.max((int) Math.ceil(aliado.getSaludMaxima() * 0.65),
                        riesgo * Math.max(1, Math.min(3, pasosNecesarios))
                                + (int) Math.ceil(aliado.getSaludMaxima() * 0.30)));

        if (aliado.getSalud() < saludNecesaria) {
            if (usarBotiquin(aliado, aliado)) {
                cambiarSituacion(aliado, SituacionAliado.REABASTECIENDOSE);
                return true;
            }
            cambiarSituacion(aliado, SituacionAliado.EN_ESPERA_POR_RECURSOS);
            juego.getConsola().imprimirAdvertencia(aliado.getNombre()
                    + " aplaza la ayuda: su vida correria peligro y no dispone de botiquin.");
            return true;
        }

        if (aliado.getEnergia() < energiaNecesaria) {
            long toritos = aliado.getMochila().getObjetos().stream().filter(ToritoRojo.class::isInstance).count();
            boolean reservarParaJugador = juego.getJugador().getEnergia() < juego.getJugador().getEnergiaMaxima();
            if ((!reservarParaJugador || toritos > 1) && usarTorito(aliado, aliado)) {
                cambiarSituacion(aliado, SituacionAliado.REABASTECIENDOSE);
                return true;
            }
            Posicion suministro = buscarObjetoAccesibleMasCercano(aliado, ToritoRojo.class);
            int pasosSuministro = suministro == null
                    ? -1
                    : calcularDistanciaRutaAliado(aliado.getPosicion(), suministro);
            if (pasosSuministro > 0
                    && aliado.getEnergia() >= pasosSuministro * costeMovimiento + costeMovimiento) {
                cambiarSituacion(aliado, SituacionAliado.BUSCANDO_SUMINISTROS);
                moverAliadoHaciaObjetivo(aliado, suministro);
                return true;
            }
            cambiarSituacion(aliado, SituacionAliado.EN_ESPERA_POR_RECURSOS);
            juego.getConsola().imprimirAdvertencia(aliado.getNombre()
                    + " aplaza la ayuda: no puede llegar sin agotar su energia.");
            return true;
        }
        return false;
    }

    private boolean buscarSuministroNecesarioParaJugador(Aliado aliado) {
        Personaje jugador = juego.getJugador();
        boolean necesitaBotiquin = jugador.getSalud() < jugador.getSaludMaxima()
                && aliado.getMochila().getObjetos().stream().noneMatch(Botiquin.class::isInstance);
        boolean necesitaTorito = jugador.getEnergia() < jugador.getEnergiaMaxima()
                && aliado.getMochila().getObjetos().stream().noneMatch(ToritoRojo.class::isInstance);
        if (!necesitaBotiquin && !necesitaTorito) {
            return false;
        }
        if (necesitaBotiquin && moverAliadoHaciaSuministro(aliado, Botiquin.class)) {
            return true;
        }
        if (necesitaTorito && moverAliadoHaciaSuministro(aliado, ToritoRojo.class)) {
            return true;
        }
        return explorarSuministrosDesconocidos(aliado);
    }

    private boolean explorarSuministrosDesconocidos(Aliado aliado) {
        Posicion destino = buscarCeldaSinInspeccionarMasCercana(aliado);
        if (destino == null || aliado.getEnergia() < aliado.estimarCosteMovimiento() * 2) {
            return false;
        }
        cambiarSituacion(aliado, SituacionAliado.BUSCANDO_SUMINISTROS);
        moverAliadoHaciaObjetivo(aliado, destino);
        juego.getConsola().imprimirInfo(aliado.getNombre()
                + " explora una celda desconocida para buscar suministros para el jugador.");
        return true;
    }

    private boolean moverAliadoHaciaSuministro(Aliado aliado, Class<? extends Objeto> tipo) {
        Posicion suministro = buscarObjetoAccesibleMasCercano(aliado, tipo);
        if (suministro == null) {
            return false;
        }
        int distancia = calcularDistanciaRutaAliado(aliado.getPosicion(), suministro);
        int coste = aliado.estimarCosteMovimiento();
        if (distancia < 0 || aliado.getEnergia() < distancia * coste + coste) {
            return false;
        }
        cambiarSituacion(aliado, SituacionAliado.BUSCANDO_SUMINISTROS);
        moverAliadoHaciaObjetivo(aliado, suministro);
        return true;
    }

    private void interactuarConObjetos(Aliado aliado) {
        Posicion posicion = aliado.getPosicion();
        boolean primeraInspeccion = juego.inspeccionarCeldaAliado(aliado);
        Celda celda = juego.getMapa().getCelda(posicion);
        gestionarAguaAliada(aliado, celda);
        if (primeraInspeccion) {
            juego.getConsola().imprimirInfo(aliado.getNombre() + " inspecciona la celda " + posicion
                    + " y encuentra " + celda.getObjetos().size() + " objeto(s).");
        }
        if (!juego.isCeldaInspeccionada(aliado, posicion)) {
            return;
        }
        List<Objeto> objetos = List.copyOf(celda.getObjetos());
        for (Objeto objeto : objetos) {
            if (objeto instanceof Explosivo) {
                continue;
            }
            if (objeto instanceof Arma arma) {
                gestionarArmaInspeccionada(aliado, arma, celda);
            } else if (objeto instanceof Armadura armadura) {
                gestionarArmaduraInspeccionada(aliado, armadura, celda);
            } else if (objeto instanceof Binocular binocular) {
                gestionarBinocularInspeccionado(aliado, binocular, celda);
            } else {
                recogerObjetoAliado(aliado, objeto, celda);
            }
        }
    }

    private void recogerObjetoAliado(Aliado aliado, Objeto objeto, Celda celda) {
        if (!aliado.getMochila().puedeGuardar(objeto)) {
            return;
        }
        Objeto recogido = celda.quitarObjetoPorNombre(objeto.getNombre());
        if (recogido == null) {
            return;
        }
        try {
            aliado.coger(recogido);
            juego.getConsola().imprimirInfo(aliado.getNombre() + " recoge " + recogido.getNombre() + ".");
        } catch (Exception e) {
            celda.agregarObjeto(recogido);
        }
    }

    private void gestionarArmaInspeccionada(Aliado aliado, Arma candidata, Celda celda) {
        List<Arma> equipadas = aliado.getArmasEquipadas();
        int manosUsadas = equipadas.stream().mapToInt(arma -> arma.isDosManos() ? 2 : 1).sum();
        int manosCandidata = candidata.isDosManos() ? 2 : 1;
        if (manosUsadas + manosCandidata <= 2) {
            equiparDesdeCelda(aliado, candidata, celda);
            return;
        }

        List<Arma> aSustituir = new ArrayList<>();
        if (candidata.isDosManos()) {
            int danioActual = equipadas.stream().mapToInt(Arma::getDanio).sum();
            if (candidata.getDanio() > danioActual) {
                aSustituir.addAll(equipadas);
            }
        } else {
            Arma peor = equipadas.stream().min((primera, segunda) ->
                    Integer.compare(primera.getDanio(), segunda.getDanio())).orElse(null);
            if (peor != null && !peor.isDosManos() && candidata.getDanio() > peor.getDanio()) {
                aSustituir.add(peor);
            } else if (peor != null && peor.isDosManos() && candidata.getDanio() > peor.getDanio()) {
                aSustituir.add(peor);
            }
        }
        if (!aSustituir.isEmpty() && desequiparYTirar(aliado, aSustituir, celda)) {
            equiparDesdeCelda(aliado, candidata, celda);
        }
    }

    private void gestionarArmaduraInspeccionada(Aliado aliado, Armadura candidata, Celda celda) {
        Armadura actual = aliado.getArmaduraEquipada();
        if (actual == null) {
            equiparDesdeCelda(aliado, candidata, celda);
            return;
        }
        if (valorArmadura(candidata) <= valorArmadura(actual)
                || !desequiparYTirar(aliado, List.of(actual), celda)) {
            return;
        }
        equiparDesdeCelda(aliado, candidata, celda);
    }

    private int valorArmadura(Armadura armadura) {
        return armadura.getDefensa() * 10 + armadura.getBonusSalud() + armadura.getBonusEnergia() / 2;
    }

    private void gestionarBinocularInspeccionado(Aliado aliado, Binocular candidato, Celda celda) {
        Binocular actual = aliado.getBinocularEquipado();
        if (actual == null) {
            equiparDesdeCelda(aliado, candidato, celda);
            return;
        }
        if (candidato.getRango() <= actual.getRango()
                || !desequiparYTirar(aliado, List.of(actual), celda)) {
            return;
        }
        equiparDesdeCelda(aliado, candidato, celda);
    }

    private void equiparDesdeCelda(Aliado aliado, Objeto objeto, Celda celda) {
        Objeto retirado = celda.quitarObjetoPorNombre(objeto.getNombre());
        if (retirado == null) {
            return;
        }
        try {
            aliado.equipar(retirado);
            juego.getConsola().imprimirInfo(
                    aliado.getNombre() + " recoge y equipa " + objeto.getNombre() + ".");
            GestorSonido.reproducir(EventoSonido.EQUIPAR,
                    aliado.getPosicion(), juego.getJugador().getPosicion());
        } catch (Exception e) {
            celda.agregarObjeto(retirado);
        }
    }

    private boolean desequiparYTirar(Aliado aliado, List<? extends Objeto> objetos, Celda celda) {
        for (Objeto objeto : objetos) {
            if (!liberarEspacioParaDesequipar(aliado, objeto, celda)) {
                return false;
            }
            try {
                aliado.desequipar(objeto.getNombre());
                GestorSonido.reproducir(EventoSonido.DESEQUIPAR,
                        aliado.getPosicion(), juego.getJugador().getPosicion());
                if (!tirarObjetoAliado(aliado, objeto.getNombre(), celda,
                        " para sustituirlo por una opcion mejor")) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    private boolean liberarEspacioParaDesequipar(Aliado aliado, Objeto objeto, Celda celda) {
        while (!aliado.getMochila().puedeGuardar(objeto)) {
            Objeto descarte = aliado.getMochila().getObjetos().stream()
                    .min((primero, segundo) -> Integer.compare(
                            valorConservacion(primero), valorConservacion(segundo)))
                    .orElse(null);
            if (descarte == null) {
                return false;
            }
            if (!tirarObjetoAliado(aliado, descarte.getNombre(), celda, " para liberar espacio")) {
                return false;
            }
        }
        return true;
    }

    private int valorConservacion(Objeto objeto) {
        if (objeto instanceof Botiquin || objeto instanceof ToritoRojo) {
            return 100;
        }
        if (objeto instanceof Binocular) {
            return 80;
        }
        if (objeto instanceof Arma arma) {
            return arma.getDanio();
        }
        if (objeto instanceof Armadura armadura) {
            return valorArmadura(armadura);
        }
        return 1;
    }

    private boolean tirarObjetoAliado(Aliado aliado, String nombre, Celda celda, String motivo) {
        try {
            Objeto descartado = aliado.tirar(nombre);
            celda.agregarObjeto(descartado);
            juego.getConsola().imprimirInfo(
                    aliado.getNombre() + " tira " + descartado.getNombre()
                            + " en la celda " + aliado.getPosicion() + motivo + ".");
            GestorSonido.reproducir(EventoSonido.TIRAR,
                    aliado.getPosicion(), juego.getJugador().getPosicion());
            return true;
        } catch (Exception ignored) {
            // El objeto permanece donde estaba si no puede retirarse.
            return false;
        }
    }

    private boolean asistirJugador(Aliado aliado) {
        Personaje jugador = juego.getJugador();
        if (aliado.getPosicion().distanciaManhattan(jugador.getPosicion()) > 1) {
            return false;
        }
        return usarSuministro(aliado, jugador);
    }

    private boolean asistirAliadoPrioritario(Aliado donante) {
        Aliado destinatarioSalud = juego.getAliados().stream()
                .filter(aliado -> aliado.getSalud() > 0 && aliado.getSalud() < aliado.getSaludMaxima())
                .filter(aliado -> donante.getPosicion().distanciaManhattan(aliado.getPosicion()) <= 1)
                .min((primero, segundo) -> Double.compare(
                        (double) primero.getSalud() / primero.getSaludMaxima(),
                        (double) segundo.getSalud() / segundo.getSaludMaxima()))
                .orElse(null);
        if (destinatarioSalud != null && usarBotiquin(donante, destinatarioSalud)) {
            return true;
        }
        Aliado destinatarioEnergia = juego.getAliados().stream()
                .filter(aliado -> aliado.getSalud() > 0 && aliado.getEnergia() < aliado.getEnergiaMaxima())
                .filter(aliado -> donante.getPosicion().distanciaManhattan(aliado.getPosicion()) <= 1)
                .min((primero, segundo) -> Double.compare(
                        (double) primero.getEnergia() / primero.getEnergiaMaxima(),
                        (double) segundo.getEnergia() / segundo.getEnergiaMaxima()))
                .orElse(null);
        return destinatarioEnergia != null && usarTorito(donante, destinatarioEnergia);
    }

    private boolean usarSuministro(Aliado donante, Personaje destinatario) {
        if (destinatario.getSalud() < destinatario.getSaludMaxima()
                && usarBotiquin(donante, destinatario)) {
            return true;
        }
        if (destinatario.getEnergia() < destinatario.getEnergiaMaxima()
                && usarTorito(donante, destinatario)) {
            return true;
        }
        if (!cooperacionInventarioActiva) {
            return false;
        }
        CooperacionInventario cooperacion = new CooperacionInventario(1);
        return cooperacion.compartirMunicion(donante, destinatario)
                || cooperacion.transferirMejorArma(donante, destinatario);
    }

    private boolean usarBotiquin(Aliado donante, Personaje destinatario) {
        Objeto objeto = donante.getMochila().getObjetos().stream()
                .filter(Botiquin.class::isInstance)
                .findFirst()
                .orElse(null);
        return usarSuministro(donante, destinatario, objeto, "vida");
    }

    private boolean usarTorito(Aliado donante, Personaje destinatario) {
        Objeto objeto = donante.getMochila().getObjetos().stream()
                .filter(ToritoRojo.class::isInstance)
                .findFirst()
                .orElse(null);
        return usarSuministro(donante, destinatario, objeto, "energia");
    }

    private boolean usarSuministro(Aliado donante, Personaje destinatario, Objeto objeto, String recurso) {
        if (objeto == null) {
            return false;
        }
        Objeto retirado = donante.getMochila().quitarPorNombre(objeto.getNombre());
        try {
            retirado.usar(destinatario);
            cambiarSituacion(donante, destinatario == juego.getJugador()
                    ? SituacionAliado.ASISTIENDO_JUGADOR
                    : SituacionAliado.ASISTIENDO_ALIADO);
            juego.getConsola().imprimirExito(donante.getNombre() + " usa " + retirado.getNombre()
                    + " para dar " + recurso + " a " + destinatario.getNombre() + ".");
            return true;
        } catch (Exception e) {
            donante.getMochila().guardar(retirado);
            return false;
        }
    }

    private Enemigo buscarEnemigoCercanoAlJugador() {
        Posicion jugador = juego.getJugador().getPosicion();
        int radioApoyo = Math.max(3, juego.getJugador().getRangoVision());
        return juego.getEnemigos().stream()
                .filter(enemigo -> enemigo.getSalud() > 0)
                .filter(enemigo -> enemigo.getPosicion().distanciaManhattan(jugador) <= radioApoyo)
                .min((primero, segundo) -> Integer.compare(
                        primero.getPosicion().distanciaManhattan(jugador),
                        segundo.getPosicion().distanciaManhattan(jugador)))
                .orElse(null);
    }

    private void eliminarEnemigoDerrotado(Aliado aliado, Enemigo objetivo) {
        if (objetivo.getSalud() > 0) {
            return;
        }
        juego.getMapa().getCelda(objetivo.getPosicion()).quitarEnemigo(objetivo);
        ServicioBotinEnemigo.soltar(
                juego.getMapa().getCelda(objetivo.getPosicion()), objetivo);
        juego.getConsola().imprimirExito(aliado.getNombre() + " elimina a " + objetivo.getNombre() + ".");
    }

    private void avanzarOrdenAyuda() {
        if (turnosAyudaAliados <= 0) {
            return;
        }
        setTurnosAyudaAliados(turnosAyudaAliados - 1);
        if (turnosAyudaAliados == 0) {
            juego.getConsola().imprimirInfo("La orden de ayuda aliada ha finalizado.");
        }
    }

    private boolean extraerAliadoSiProcede(Aliado aliado) {
        if (!aliado.getPosicion().equals(juego.getMapa().getObjetivo())) {
            return false;
        }
        juego.getMapa().getCelda(aliado.getPosicion()).quitarAliado(aliado);
        if (juego.extraerAliado(aliado)) {
            cambiarSituacion(aliado, SituacionAliado.EVACUADO);
            marcarCombate(aliado, false);
            juego.getConsola().imprimirInfo(aliado.getNombre() + " sale del mapa con vida. ("
                    + juego.getAliadosExtraidos() + "/" + juego.getAliadosIniciales() + ")");
        }
        return true;
    }

    private boolean debeAliadoAtacarConRadar(Aliado aliado, Enemigo objetivo) {
        if (!tieneRadar(aliado)) {
            return true;
        }
        double saludRelativa = (double) aliado.getSalud() / Math.max(1, aliado.getSaludMaxima());
        int riesgo = estimarRiesgoRecibido(aliado);
        if (saludRelativa < 0.55 || riesgo >= aliado.getSalud()) {
            juego.getConsola().imprimirInfo(
                    aliado.getNombre() + " evalua con radar y evita el ataque contra " + objetivo.getNombre() + ".");
            return false;
        }
        double probabilidadAtacar = saludRelativa >= 0.8 ? 0.70 : 0.50;
        if (riesgo > aliado.getSalud() * 0.35) {
            probabilidadAtacar -= 0.20;
        }
        boolean ataca = random.nextDouble() < probabilidadAtacar;
        if (!ataca) {
            juego.getConsola().imprimirInfo(
                    aliado.getNombre() + " detecta amenazas con radar y no ataca este turno.");
        }
        return ataca;
    }

    private boolean tieneRadar(Aliado aliado) {
        if (aliado.getBinocularEquipado() != null) {
            return true;
        }
        for (Objeto objeto : aliado.getMochila().getObjetos()) {
            if (objeto instanceof Binocular || objeto.getNombre().toLowerCase().contains("radar")) {
                return true;
            }
        }
        return false;
    }

    private void usarBinocularSiConviene(Aliado aliado) {
        Binocular equipado = aliado.getBinocularEquipado();
        Binocular enMochila = aliado.getMochila().getObjetos().stream()
                .filter(Binocular.class::isInstance)
                .map(Binocular.class::cast)
                .max(java.util.Comparator.comparingInt(Binocular::getRango))
                .orElse(null);
        boolean usarEquipado = equipado != null
                && (enMochila == null || equipado.getRango() >= enMochila.getRango());
        Binocular elegido = usarEquipado ? equipado : enMochila;
        if (elegido == null || !revelaNuevaAmenaza(aliado, elegido)) {
            return;
        }
        if (usarEquipado) {
            aliado.setBinocularEquipado(null);
        } else {
            aliado.getMochila().quitarPorNombre(elegido.getNombre());
        }
        elegido.usar(aliado);
        juego.getConsola().imprimirInfo(aliado.getNombre() + " usa " + elegido.getNombre()
                + " porque permite detectar una amenaza nueva este turno.");
    }

    private boolean intentarRecargar(Personaje personaje) {
        try {
            var resultado = new ServicioRecarga().recargar(personaje, null);
            juego.getConsola().imprimirInfo(personaje.getNombre() + " recarga "
                    + resultado.arma().getNombre() + " (" + resultado.cantidad() + ").");
            juego.publicarEvento(new com.legendoftecla.events.ArmaRecargada(
                    juego.getBusEventos().ahora(), personaje.getNombre(),
                    resultado.arma().getNombre(), resultado.cantidad(),
                    personaje.getPosicion()));
            return true;
        } catch (com.legendoftecla.exceptions.AccionInvalidaException error) {
            return false;
        }
    }

    private void gestionarLinternaAliada(Aliado aliado) {
        Linterna linterna = aliado.getMochila().getObjetos().stream()
                .filter(Linterna.class::isInstance).map(Linterna.class::cast).findFirst().orElse(null);
        if (linterna == null) return;
        boolean necesitaLuz = juego.getMapa().getCelda(aliado.getPosicion()).isOscura();
        if (necesitaLuz != aliado.isLinternaActiva()) {
            aliado.setLinternaActiva(necesitaLuz);
            aliado.setAlcanceLinterna(linterna.getAlcance());
            juego.getConsola().imprimirInfo(aliado.getNombre()
                    + (necesitaLuz ? " enciende " : " apaga ") + linterna.getNombre() + ".");
        }
    }

    private void gestionarAguaAliada(Aliado aliado, Celda celda) {
        CuboAgua cubo = aliado.getMochila().getObjetos().stream()
                .filter(CuboAgua.class::isInstance).map(CuboAgua.class::cast).findFirst().orElse(null);
        if (cubo == null) return;
        if (!cubo.isLleno() && celda.hasFuenteAgua()) {
            cubo.llenar();
            juego.getConsola().imprimirInfo(aliado.getNombre() + " llena " + cubo.getNombre() + ".");
            return;
        }
        if (!cubo.isLleno()) return;
        Posicion fuego = celda.estaArdiendo() ? aliado.getPosicion() : null;
        if (fuego == null) {
            for (Direccion direccion : Direccion.values()) {
                Posicion candidata = aliado.getPosicion().mover(direccion);
                if (juego.getMapa().estaDentro(candidata)
                        && juego.getMapa().getCelda(candidata).estaArdiendo()) {
                    fuego = candidata;
                    break;
                }
            }
        }
        if (fuego != null && SistemaIncendios.apagar(juego, fuego)) {
            cubo.consumirAgua();
            juego.getConsola().imprimirExito(aliado.getNombre() + " usa " + cubo.getNombre() + ".");
        }
    }

    private boolean revelaNuevaAmenaza(Aliado aliado, Binocular binocular) {
        int visionActual = aliado.getRangoVision();
        int visionAmpliada = visionActual + binocular.getRango();
        return juego.getEnemigos().stream().filter(enemigo -> enemigo.getSalud() > 0).anyMatch(enemigo -> {
            int distancia = aliado.getPosicion().distanciaManhattan(enemigo.getPosicion());
            return distancia > visionActual && distancia <= visionAmpliada
                    && juego.getMapa().hayLineaAtaque(aliado.getPosicion(), enemigo.getPosicion());
        });
    }

    private void ejecutarFormacion(Aliado aliado) {
        Enemigo amenaza = buscarAmenazaDeFormacion(aliado);
        if (amenaza == null && hayEscasezSuministros() && esExploradorDeFormacion(aliado)
                && buscarSuministrosSinRomperFormacion(aliado)) {
            return;
        }
        Posicion jugador = juego.getJugador().getPosicion();
        int separacion = aliado.getPosicion().distanciaManhattan(jugador);
        if (juego.getFormacionAliada() == FormacionAliada.DEFENSIVA) {
            cambiarSituacion(aliado, SituacionAliado.PROTEGIENDO);
            if (amenaza != null && puedeAliadoAtacarA(aliado, amenaza)) {
                atacarEnFormacion(aliado, amenaza);
            } else if (separacion > 1) {
                moverAliadoHaciaObjetivo(aliado, jugador);
            } else if (amenaza != null
                    && amenaza.getPosicion().distanciaManhattan(jugador) <= 2) {
                moverAliadoHaciaObjetivo(aliado, amenaza.getPosicion());
            }
            return;
        }
        cambiarSituacion(aliado, amenaza == null
                ? SituacionAliado.ACOMPANANDO : SituacionAliado.EN_COMBATE);
        if (separacion > 2) {
            moverAliadoHaciaObjetivo(aliado, jugador);
        } else if (amenaza == null) {
            if (separacion > 1) {
                moverAliadoHaciaObjetivo(aliado, jugador);
            }
        } else if (puedeAliadoAtacarA(aliado, amenaza)) {
            atacarEnFormacion(aliado, amenaza);
        } else {
            moverAliadoHaciaObjetivo(aliado, amenaza.getPosicion());
        }
    }

    private void atacarEnFormacion(Aliado aliado, Enemigo enemigo) {
        marcarCombate(aliado, true);
        if (!debeAliadoAtacarConRadar(aliado, enemigo)) {
            return;
        }
        SistemaCombate.atacar(juego, aliado, enemigo, random);
        eliminarEnemigoDerrotado(aliado, enemigo);
    }

    private boolean puedeAliadoAtacarA(Aliado aliado, Enemigo enemigo) {
        int distancia = aliado.getPosicion().distanciaManhattan(enemigo.getPosicion());
        boolean alcance = aliado.getArmasEquipadas().isEmpty()
                ? distancia <= 1 : aliado.puedeAtacarA(distancia);
        return alcance && juego.getMapa().hayLineaAtaque(
                aliado.getPosicion(), enemigo.getPosicion());
    }

    private Enemigo buscarAmenazaDeFormacion(Aliado aliado) {
        Posicion jugador = juego.getJugador().getPosicion();
        return juego.getEnemigos().stream().filter(enemigo -> enemigo.getSalud() > 0)
                .filter(enemigo -> SistemaIluminacion.hayLuz(juego, enemigo.getPosicion()))
                .filter(enemigo -> juego.getFormacionAliada() == FormacionAliada.OFENSIVA
                        || enemigo.getPosicion().distanciaManhattan(jugador) <= 3
                        || enemigo.getPosicion().distanciaManhattan(aliado.getPosicion()) <= 2)
                .min(java.util.Comparator.comparingInt(enemigo ->
                        enemigo.getPosicion().distanciaManhattan(aliado.getPosicion())))
                .orElse(null);
    }

    private boolean hayEscasezSuministros() {
        long aliadosVivos = juego.getAliados().stream().filter(aliado -> aliado.getSalud() > 0).count();
        long minimo = Math.max(1, (aliadosVivos + 1) / 2);
        long botiquines = contarSuministros(Botiquin.class);
        long energeticos = contarSuministros(ToritoRojo.class);
        return botiquines < minimo || energeticos < minimo;
    }

    private long contarSuministros(Class<? extends Objeto> tipo) {
        long total = juego.getJugador().getMochila().getObjetos().stream().filter(tipo::isInstance).count();
        for (Aliado aliado : juego.getAliados()) {
            total += aliado.getMochila().getObjetos().stream().filter(tipo::isInstance).count();
        }
        return total;
    }

    private boolean esExploradorDeFormacion(Aliado candidato) {
        if (candidato.getPosicion().distanciaManhattan(juego.getJugador().getPosicion()) > 2) {
            return false;
        }
        Aliado mejor = juego.getAliados().stream().filter(aliado -> aliado.getSalud() > 0)
                .max(java.util.Comparator.comparingDouble(this::estadoRelativo)).orElse(null);
        return candidato == mejor && mejor != null && estadoRelativo(mejor) >= 1.30;
    }

    private double estadoRelativo(Aliado aliado) {
        double salud = (double) aliado.getSalud() / Math.max(1, aliado.getSaludMaxima());
        double energia = (double) aliado.getEnergia() / Math.max(1, aliado.getEnergiaMaxima());
        return salud + energia;
    }

    private boolean buscarSuministrosSinRomperFormacion(Aliado aliado) {
        Posicion destino = buscarCeldaDeSuministrosEnFormacion(aliado);
        if (destino == null) {
            return false;
        }
        cambiarSituacion(aliado, SituacionAliado.BUSCANDO_SUMINISTROS);
        moverAliadoHaciaObjetivo(aliado, destino);
        juego.getConsola().imprimirInfo(aliado.getNombre()
                + " explora suministros sin alejarse de la formacion.");
        return true;
    }

    private boolean evacuarAliadoSiTieneLaSalidaAlAlcance(Aliado aliado) {
        Posicion salida = juego.getMapa().getObjetivo();
        if (calcularDistanciaRutaAliado(aliado.getPosicion(), salida) != 1) {
            return false;
        }
        Posicion origen = aliado.getPosicion();
        moverAliadoHaciaObjetivo(aliado, salida);
        if (aliado.getPosicion().equals(origen)) {
            return false;
        }
        juego.getConsola().imprimirInfo(
                aliado.getNombre() + " prioriza la salida y alcanza la casilla final.");
        return extraerAliadoSiProcede(aliado);
    }

    private Posicion buscarCeldaDeSuministrosEnFormacion(Aliado aliado) {
        Posicion jugador = juego.getJugador().getPosicion();
        ArrayDeque<Posicion> pendientes = new ArrayDeque<>();
        Set<Posicion> visitadas = new HashSet<>();
        pendientes.add(aliado.getPosicion());
        visitadas.add(aliado.getPosicion());
        while (!pendientes.isEmpty()) {
            Posicion actual = pendientes.removeFirst();
            if (!actual.equals(aliado.getPosicion()) && !actual.equals(jugador)
                    && jugador.distanciaManhattan(actual) <= 3) {
                boolean suministroConocido = juego.isCeldaInspeccionada(aliado, actual)
                        && juego.getMapa().getCelda(actual).getObjetos().stream()
                                .anyMatch(objeto -> objeto instanceof Botiquin || objeto instanceof ToritoRojo);
                if (suministroConocido || !juego.isCeldaInspeccionada(aliado, actual)) {
                    return actual;
                }
            }
            for (Direccion direccion : Direccion.values()) {
                Posicion candidata = actual.mover(direccion);
                if (visitadas.contains(candidata) || jugador.distanciaManhattan(candidata) > 3
                        || !juego.getMapa().esTransitable(candidata)) {
                    continue;
                }
                visitadas.add(candidata);
                pendientes.addLast(candidata);
            }
        }
        return null;
    }

    private boolean enemigoDetectaFormacion(Enemigo enemigo) {
        if (juego.getFormacionAliada() == FormacionAliada.SIN_FORMACION) {
            return false;
        }
        if (vePersonaje(enemigo, juego.getJugador())) {
            return true;
        }
        return juego.getAliados().stream().filter(aliado -> aliado.getSalud() > 0)
                .anyMatch(aliado -> vePersonaje(enemigo, aliado));
    }

    private boolean vePersonaje(Enemigo enemigo, Personaje personaje) {
        return enemigo.getPosicion().distanciaManhattan(personaje.getPosicion()) <= enemigo.getRangoVision()
                && juego.getMapa().hayLineaAtaque(enemigo.getPosicion(), personaje.getPosicion());
    }

    private Personaje seleccionarObjetivoTactico(Enemigo enemigo) {
        List<Personaje> visibles = new ArrayList<>();
        if (vePersonaje(enemigo, juego.getJugador())) {
            visibles.add(juego.getJugador());
        }
        juego.getAliados().stream().filter(aliado -> aliado.getSalud() > 0)
                .filter(aliado -> vePersonaje(enemigo, aliado)).forEach(visibles::add);
        if (visibles.isEmpty()) {
            return juego.getJugador();
        }
        if (juego.getFormacionAliada() == FormacionAliada.DEFENSIVA) {
            return visibles.stream().min(java.util.Comparator.comparingDouble(personaje ->
                    (double) personaje.getSalud() / Math.max(1, personaje.getSaludMaxima()))).orElseThrow();
        }
        return visibles.stream().min(java.util.Comparator.comparingInt(personaje ->
                enemigo.getPosicion().distanciaManhattan(personaje.getPosicion()))).orElseThrow();
    }

    private int estimarRiesgoRecibido(Aliado aliado) {
        int defensa = aliado.getArmaduraEquipada() != null ? aliado.getArmaduraEquipada().getDefensa() : 0;
        int golpeEstimado = Math.max(1, 4 - defensa);
        int riesgo = 0;
        for (Enemigo enemigo : juego.getEnemigos()) {
            if (enemigo.getSalud() > 0
                    && enemigo.getPosicion().distanciaManhattan(aliado.getPosicion()) <= enemigo.getRangoVision()
                    && juego.getMapa().hayLineaAtaque(enemigo.getPosicion(), aliado.getPosicion())) {
                riesgo += golpeEstimado;
            }
        }
        return riesgo;
    }

    private Enemigo buscarEnemigoMasCercano(Aliado aliado) {
        Enemigo mejor = null;
        int mejorDistancia = Integer.MAX_VALUE;
        for (Enemigo enemigo : juego.getEnemigos()) {
            if (enemigo.getSalud() <= 0) {
                continue;
            }
            int distancia = aliado.getPosicion().distanciaManhattan(enemigo.getPosicion());
            if (distancia < mejorDistancia) {
                mejorDistancia = distancia;
                mejor = enemigo;
            }
        }
        return mejor;
    }

    private void moverAliadoHaciaObjetivo(Aliado aliado, Posicion objetivo) {
        Posicion origen = aliado.getPosicion();
        Direccion siguiente = buscarPrimerPasoAliado(origen, objetivo);
        if (siguiente == null) {
            return;
        }
        try {
            juego.getMapa().getCelda(origen).quitarAliado(aliado);
            aliado.mover(siguiente, juego);
            juego.getMapa().getCelda(aliado.getPosicion()).agregarAliado(aliado);
            GestorSonido.reproducir(EventoSonido.MOVIMIENTO,
                    aliado.getPosicion(), juego.getJugador().getPosicion());
        } catch (Exception e) {
            juego.getMapa().getCelda(origen).agregarAliado(aliado);
        }
    }

    private boolean hayRescateEnergiaPosible() {
        for (Aliado aliado : juego.getAliados()) {
            if (aliado.getSalud() <= 0) {
                continue;
            }
            boolean puedeRecuperarVida = puedeAcudirSinPeligro(aliado)
                    || aliado.getMochila().getObjetos().stream().anyMatch(Botiquin.class::isInstance);
            if (!puedeRecuperarVida) {
                continue;
            }
            int distanciaJugador = calcularDistanciaRutaAliado(
                    aliado.getPosicion(), juego.getJugador().getPosicion());
            if (distanciaJugador < 0) {
                continue;
            }
            int coste = aliado.estimarCosteMovimiento();
            int reserva = Math.max(coste * 2, (int) Math.ceil(aliado.getEnergiaMaxima() * 0.15));
            int energiaEntrega = Math.max(0, distanciaJugador - 1) * coste + reserva;
            List<ToritoRojo> toritos = aliado.getMochila().getObjetos().stream()
                    .filter(ToritoRojo.class::isInstance)
                    .map(ToritoRojo.class::cast)
                    .toList();
            int energiaPotencial = aliado.getEnergia();
            if (toritos.size() > 1) {
                energiaPotencial += toritos.stream().mapToInt(ToritoRojo::getEnergiaTurno).max().orElse(0);
            }
            if (!toritos.isEmpty() && energiaPotencial >= energiaEntrega) {
                return true;
            }

            Posicion suministro = buscarObjetoAccesibleMasCercano(aliado, ToritoRojo.class);
            if (suministro == null) {
                continue;
            }
            int hastaSuministro = calcularDistanciaRutaAliado(aliado.getPosicion(), suministro);
            int suministroAJugador = calcularDistanciaRutaAliado(suministro, juego.getJugador().getPosicion());
            if (hastaSuministro >= 0 && suministroAJugador >= 0
                    && aliado.getEnergia() >= (hastaSuministro + Math.max(0, suministroAJugador - 1))
                            * coste + reserva) {
                return true;
            }
        }
        return false;
    }

    private Posicion buscarObjetoAccesibleMasCercano(Aliado aliado, Class<? extends Objeto> tipo) {
        Posicion origen = aliado.getPosicion();
        ArrayDeque<Posicion> pendientes = new ArrayDeque<>();
        Set<Posicion> visitadas = new HashSet<>();
        pendientes.add(origen);
        visitadas.add(origen);
        while (!pendientes.isEmpty()) {
            Posicion actual = pendientes.removeFirst();
            boolean contiene = juego.isCeldaInspeccionada(aliado, actual)
                    && juego.getMapa().getCelda(actual).getObjetos().stream().anyMatch(tipo::isInstance);
            if (contiene) {
                return actual;
            }
            for (Direccion direccion : Direccion.values()) {
                Posicion candidata = actual.mover(direccion);
                if (visitadas.contains(candidata) || !juego.getMapa().esTransitable(candidata)) {
                    continue;
                }
                visitadas.add(candidata);
                pendientes.addLast(candidata);
            }
        }
        return null;
    }

    private Posicion buscarCeldaSinInspeccionarMasCercana(Aliado aliado) {
        Posicion origen = aliado.getPosicion();
        ArrayDeque<Posicion> pendientes = new ArrayDeque<>();
        Set<Posicion> visitadas = new HashSet<>();
        pendientes.add(origen);
        visitadas.add(origen);
        while (!pendientes.isEmpty()) {
            Posicion actual = pendientes.removeFirst();
            if (!actual.equals(origen) && !juego.isCeldaInspeccionada(aliado, actual)) {
                return actual;
            }
            for (Direccion direccion : Direccion.values()) {
                Posicion candidata = actual.mover(direccion);
                if (visitadas.contains(candidata) || !juego.getMapa().esTransitable(candidata)) {
                    continue;
                }
                visitadas.add(candidata);
                pendientes.addLast(candidata);
            }
        }
        return null;
    }

    private int calcularDistanciaRutaAliado(Posicion origen, Posicion objetivo) {
        if (origen.equals(objetivo)) {
            return 0;
        }
        ArrayDeque<Posicion> pendientes = new ArrayDeque<>();
        Map<Posicion, Integer> distancias = new HashMap<>();
        pendientes.add(origen);
        distancias.put(origen, 0);
        while (!pendientes.isEmpty()) {
            Posicion actual = pendientes.removeFirst();
            int distancia = distancias.get(actual);
            for (Direccion direccion : Direccion.values()) {
                Posicion candidata = actual.mover(direccion);
                if (distancias.containsKey(candidata) || !juego.getMapa().esTransitable(candidata)) {
                    continue;
                }
                if (candidata.equals(objetivo)) {
                    return distancia + 1;
                }
                distancias.put(candidata, distancia + 1);
                pendientes.addLast(candidata);
            }
        }
        return -1;
    }

    private Direccion buscarPrimerPasoAliado(Posicion origen, Posicion objetivo) {
        if (origen.equals(objetivo)) {
            return null;
        }
        ArrayDeque<Posicion> pendientes = new ArrayDeque<>();
        Map<Posicion, Posicion> anterior = new HashMap<>();
        Map<Posicion, Direccion> direccionEntrada = new HashMap<>();
        pendientes.add(origen);
        anterior.put(origen, null);

        while (!pendientes.isEmpty() && !anterior.containsKey(objetivo)) {
            Posicion actual = pendientes.removeFirst();
            for (Direccion direccion : Direccion.values()) {
                Posicion candidata = actual.mover(direccion);
                if (anterior.containsKey(candidata) || !juego.getMapa().esTransitable(candidata)) {
                    continue;
                }
                anterior.put(candidata, actual);
                direccionEntrada.put(candidata, direccion);
                pendientes.addLast(candidata);
            }
        }
        if (!anterior.containsKey(objetivo)) {
            return null;
        }
        Posicion paso = objetivo;
        while (anterior.get(paso) != null && !anterior.get(paso).equals(origen)) {
            paso = anterior.get(paso);
        }
        return direccionEntrada.get(paso);
    }

    private Direccion buscarPrimerPasoEnemigo(Posicion origen, Posicion objetivo) {
        if (origen.equals(objetivo)) {
            return null;
        }
        ArrayDeque<Posicion> pendientes = new ArrayDeque<>();
        Map<Posicion, Posicion> anterior = new HashMap<>();
        Map<Posicion, Direccion> direccionEntrada = new HashMap<>();
        pendientes.add(origen);
        anterior.put(origen, null);

        while (!pendientes.isEmpty() && !anterior.containsKey(objetivo)) {
            Posicion actual = pendientes.removeFirst();
            for (Direccion direccion : Direccion.values()) {
                Posicion candidata = actual.mover(direccion);
                if (anterior.containsKey(candidata) || !juego.getMapa().esTransitable(candidata)) {
                    continue;
                }
                anterior.put(candidata, actual);
                direccionEntrada.put(candidata, direccion);
                pendientes.addLast(candidata);
            }
        }
        if (!anterior.containsKey(objetivo)) {
            return null;
        }
        Posicion paso = objetivo;
        while (anterior.get(paso) != null && !anterior.get(paso).equals(origen)) {
            paso = anterior.get(paso);
        }
        return direccionEntrada.get(paso);
    }

    private void cambiarSituacion(Aliado aliado, SituacionAliado situacion) {
        Map<Aliado, SituacionAliado> estados = new HashMap<>(situacionesAliados);
        estados.put(Validaciones.noNulo(aliado, "Aliado"),
                Validaciones.noNulo(situacion, "Situacion aliada"));
        setSituacionesAliados(estados);
    }

    private void marcarCombate(Aliado aliado, boolean enCombate) {
        Map<Aliado, Boolean> combates = new HashMap<>(aliadosEnCombate);
        combates.put(Validaciones.noNulo(aliado, "Aliado"), enCombate);
        setAliadosEnCombate(combates);
    }

    private SituacionAliado obtenerSituacionAliado(Aliado aliado) {
        if (juego.estaAliadoExtraido(aliado)) {
            return SituacionAliado.EVACUADO;
        }
        if (aliado.getSalud() <= 0) {
            return SituacionAliado.CAIDO;
        }
        return situacionesAliados.getOrDefault(aliado, SituacionAliado.ACTIVO);
    }

    private boolean estaAliadoEnCombate(Aliado aliado) {
        return aliado.getSalud() > 0
                && !juego.estaAliadoExtraido(aliado)
                && aliadosEnCombate.getOrDefault(aliado, false);
    }

    private String listarObjetos(Aliado aliado) {
        if (aliado.getMochila().getObjetos().isEmpty()) {
            return "ninguno";
        }
        StringJoiner nombres = new StringJoiner(", ");
        aliado.getMochila().getObjetos().forEach(objeto -> nombres.add(objeto.getNombre()));
        return nombres.toString();
    }

    private String listarEquipo(Aliado aliado) {
        List<String> equipo = new ArrayList<>();
        aliado.getArmasEquipadas().forEach(arma -> equipo.add("arma " + arma.getNombre()));
        if (aliado.getArmaduraEquipada() != null) {
            equipo.add("armadura " + aliado.getArmaduraEquipada().getNombre());
        }
        if (aliado.getBinocularEquipado() != null) {
            equipo.add("binocular " + aliado.getBinocularEquipado().getNombre());
        }
        return equipo.isEmpty() ? "ninguno" : String.join(", ", equipo);
    }

    private enum SituacionAliado {
        ACTIVO("ACTIVO"),
        ACOMPANANDO("ACOMPANANDO AL JUGADOR"),
        ACUDIENDO("ACUDIENDO A LA LLAMADA"),
        PROTEGIENDO("PROTEGIENDO AL JUGADOR"),
        ASISTIENDO_JUGADOR("ASISTIENDO AL JUGADOR"),
        ASISTIENDO_ALIADO("ASISTIENDO A OTRO ALIADO"),
        REABASTECIENDOSE("REPONIENDO SU VIDA O ENERGIA"),
        BUSCANDO_SUMINISTROS("BUSCANDO SUMINISTROS"),
        EN_COMBATE("EN COMBATE"),
        FUERA_DE_COMBATE("FUERA DE COMBATE"),
        EN_ESPERA_POR_RIESGO("EN ESPERA: VIDA EN PELIGRO"),
        EN_ESPERA_POR_RECURSOS("EN ESPERA: RECURSOS INSUFICIENTES"),
        SIN_RUTA("EN ESPERA: SIN RUTA AL JUGADOR"),
        EVACUADO("EVACUADO: LLEGO A LA SALIDA"),
        CAIDO("CAIDO: FUERA DE COMBATE");

        private final String etiqueta;

        SituacionAliado(String etiqueta) {
            this.etiqueta = etiqueta;
        }
    }
}
