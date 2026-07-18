package com.legendoftecla.engine;

import com.legendoftecla.commands.CommandContext;
import com.legendoftecla.commands.CommandParser;
import com.legendoftecla.commands.Comando;
import com.legendoftecla.commands.ComandoRecorrido;
import com.legendoftecla.commands.ComandoSalir;
import com.legendoftecla.console.TipoMensaje;
import com.legendoftecla.exceptions.ComandoException;
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
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.model.world.SistemaPuntuacion;

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
    private final Juego juego;
    private final CommandContext contexto;
    private final CommandParser parser;
    private final Random random;
    private final Map<Aliado, SituacionAliado> situacionesAliados;
    private final Map<Aliado, Boolean> aliadosEnCombate;
    private boolean finalizada;
    private SistemaPuntuacion.EstadoFinalPartida estadoFinal;
    private int turnosAyudaAliados;
    private boolean avisoRescateEnergia;

    /**
     * Crea una instancia de {@code MotorPartida}.
      * @param juego valor de {@code juego}
     */
    public MotorPartida(Juego juego) {
        this.juego = juego;
        this.contexto = new CommandContext(juego);
        this.parser = new CommandParser(contexto);
        this.random = new Random();
        this.situacionesAliados = new HashMap<>();
        this.aliadosEnCombate = new HashMap<>();
        juego.getAliadosRegistrados().forEach(aliado ->
                situacionesAliados.put(aliado, SituacionAliado.ACTIVO));
        juego.getAliadosRegistrados().forEach(aliado -> aliadosEnCombate.put(aliado, false));
        this.turnosAyudaAliados = 0;
        this.avisoRescateEnergia = false;
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

    /**
     * Indica el estado de {@code Finalizada}.
      * @return resultado de la operacion
     */
    public boolean isFinalizada() {
        return finalizada;
    }

    /**
     * Obtiene el valor de {@code EstadoFinal}.
      * @return resultado de la operacion
     */
    public SistemaPuntuacion.EstadoFinalPartida getEstadoFinal() {
        return estadoFinal;
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
                    + " | Posicion " + posicion);
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
        try {
            Comando comando = parser.parse(linea == null ? "" : linea);
            comando.ejecutar();
            if (comando instanceof ComandoSalir) {
                finalizar(SistemaPuntuacion.EstadoFinalPartida.SALIDA_MANUAL);
                return false;
            }
            if (juego.consumirSolicitudAyudaAliados()) {
                turnosAyudaAliados = Math.max(TURNOS_AYUDA_MINIMOS,
                        juego.getMapa().getFilas() + juego.getMapa().getColumnas());
            }
            ejecutarTurnoAliados();
            ejecutarTurnoNPC();
            avanzarOrdenAyuda();
        } catch (ComandoException e) {
            juego.getConsola().imprimir("Error de comando: " + e.getMessage(), TipoMensaje.ERROR);
        } catch (Exception e) {
            juego.getConsola().imprimir("Error inesperado: " + e.getMessage(), TipoMensaje.ERROR);
        } finally {
            juego.getJugador().resetTurno();
        }
        evaluarFinNatural();
        return !finalizada;
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
            if (aliado.getSalud() > 0) {
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
                    && jugadorPos.distanciaManhattan(enemigo.getPosicion()) <= vision) {
                visibles.add(enemigo.getPosicion());
            }
        }
        return visibles;
    }

    private void anunciarPartida() {
        juego.getConsola().imprimir("Mapa: " + juego.getMapa().getNombre(), TipoMensaje.INFO);
        juego.getConsola().imprimir(juego.getMapa().getDescripcion(), TipoMensaje.INFO);
        if (!juego.getAliados().isEmpty()) {
            juego.getConsola().imprimir("Aliados desplegados: " + juego.getAliados().size(), TipoMensaje.INFO);
        }
    }

    private void evaluarFinNatural() {
        if (finalizada) {
            return;
        }
        if (juego.getJugador().getEnergia() > 0) {
            avisoRescateEnergia = false;
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
                avisoRescateEnergia = true;
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
        finalizada = true;
        estadoFinal = estado;
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
        for (String linea : puntuacion.formatearDesglose()) {
            juego.getConsola().imprimir(linea, TipoMensaje.INFO);
        }
        new ComandoRecorrido(contexto).ejecutar();
    }

    private void ejecutarTurnoNPC() {
        List<Enemigo> snapshot = List.copyOf(juego.getEnemigos());
        for (Enemigo enemigo : snapshot) {
            if (enemigo.getSalud() <= 0) {
                continue;
            }
            int movimientos = random.nextInt(3);
            for (int i = 0; i < movimientos; i++) {
                int distancia = enemigo.getPosicion().distanciaManhattan(juego.getJugador().getPosicion());
                if (distancia <= enemigo.getRangoVision()
                        && juego.getMapa().hayLineaAtaque(enemigo.getPosicion(), juego.getJugador().getPosicion())) {
                    enemigo.atacar(juego.getJugador());
                    juego.getConsola().imprimir(enemigo.getNombre() + " te ataca.");
                    break;
                }
                Direccion direccion = Direccion.values()[random.nextInt(Direccion.values().length)];
                Posicion origen = enemigo.getPosicion();
                Posicion destino = origen.mover(direccion);
                if (juego.getMapa().esTransitable(destino)) {
                    juego.getMapa().getCelda(origen).quitarEnemigo(enemigo);
                    try {
                        enemigo.mover(direccion, juego);
                        juego.getMapa().getCelda(enemigo.getPosicion()).agregarEnemigo(enemigo);
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
                situacionesAliados.put(aliado, SituacionAliado.CAIDO);
                aliadosEnCombate.put(aliado, false);
                continue;
            }
            aliadosEnCombate.put(aliado, false);
            SituacionAliado anterior = situacionesAliados.getOrDefault(aliado, SituacionAliado.ACTIVO);
            situacionesAliados.put(aliado, anterior == SituacionAliado.EN_COMBATE
                    || anterior == SituacionAliado.FUERA_DE_COMBATE
                            ? SituacionAliado.FUERA_DE_COMBATE
                            : SituacionAliado.ACTIVO);
            if (extraerAliadoSiProcede(aliado)) {
                continue;
            }
            interactuarConObjetos(aliado);
            if (turnosAyudaAliados > 0 && prepararAliadoParaAyuda(aliado)) {
                continue;
            }
            if (asistirJugador(aliado) || asistirAliadoPrioritario(aliado)) {
                continue;
            }
            if (turnosAyudaAliados > 0) {
                if (buscarSuministroNecesarioParaJugador(aliado)) {
                    continue;
                }
                ejecutarOrdenAyuda(aliado);
                continue;
            }
            Enemigo objetivo = buscarEnemigoMasCercano(aliado);
            if (objetivo == null) {
                if (situacionesAliados.get(aliado) != SituacionAliado.FUERA_DE_COMBATE) {
                    situacionesAliados.put(aliado, SituacionAliado.ACOMPANANDO);
                }
                Posicion destino = juego.getJugador().getPosicion().equals(juego.getMapa().getObjetivo())
                        ? juego.getMapa().getObjetivo()
                        : juego.getJugador().getPosicion();
                moverAliadoHaciaObjetivo(aliado, destino);
                extraerAliadoSiProcede(aliado);
                continue;
            }
            situacionesAliados.put(aliado, SituacionAliado.EN_COMBATE);
            aliadosEnCombate.put(aliado, true);
            int distancia = aliado.getPosicion().distanciaManhattan(objetivo.getPosicion());
            if (distancia <= 1) {
                if (!debeAliadoAtacarConRadar(aliado, objetivo)) {
                    continue;
                }
                aliado.atacar(objetivo);
                if (objetivo.getSalud() <= 0) {
                    juego.getMapa().getCelda(objetivo.getPosicion()).quitarEnemigo(objetivo);
                    juego.getConsola().imprimirExito(
                            aliado.getNombre() + " elimina a " + objetivo.getNombre() + ".");
                }
            } else {
                moverAliadoHaciaObjetivo(aliado, objetivo.getPosicion());
            }
        }
    }

    private void ejecutarOrdenAyuda(Aliado aliado) {
        if (!puedeAcudirSinPeligro(aliado)) {
            situacionesAliados.put(aliado, SituacionAliado.EN_ESPERA_POR_RIESGO);
            juego.getConsola().imprimirInfo(
                    aliado.getNombre() + " no acude: su vida correria peligro.");
            return;
        }
        Posicion jugador = juego.getJugador().getPosicion();
        if (aliado.getPosicion().distanciaManhattan(jugador) > 1) {
            situacionesAliados.put(aliado, SituacionAliado.ACUDIENDO);
            moverAliadoHaciaObjetivo(aliado, jugador);
            return;
        }
        Enemigo objetivo = buscarEnemigoCercanoAlJugador();
        if (objetivo == null) {
            situacionesAliados.put(aliado, SituacionAliado.PROTEGIENDO);
            return;
        }
        situacionesAliados.put(aliado, SituacionAliado.EN_COMBATE);
        aliadosEnCombate.put(aliado, true);
        int distancia = aliado.getPosicion().distanciaManhattan(objetivo.getPosicion());
        if (distancia <= 1) {
            if (debeAliadoAtacarConRadar(aliado, objetivo)) {
                aliado.atacar(objetivo);
                eliminarEnemigoDerrotado(aliado, objetivo);
            }
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
            situacionesAliados.put(aliado, SituacionAliado.SIN_RUTA);
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
                situacionesAliados.put(aliado, SituacionAliado.REABASTECIENDOSE);
                return true;
            }
            situacionesAliados.put(aliado, SituacionAliado.EN_ESPERA_POR_RECURSOS);
            juego.getConsola().imprimirAdvertencia(aliado.getNombre()
                    + " aplaza la ayuda: su vida correria peligro y no dispone de botiquin.");
            return true;
        }

        if (aliado.getEnergia() < energiaNecesaria) {
            long toritos = aliado.getMochila().getObjetos().stream().filter(ToritoRojo.class::isInstance).count();
            boolean reservarParaJugador = juego.getJugador().getEnergia() < juego.getJugador().getEnergiaMaxima();
            if ((!reservarParaJugador || toritos > 1) && usarTorito(aliado, aliado)) {
                situacionesAliados.put(aliado, SituacionAliado.REABASTECIENDOSE);
                return true;
            }
            Posicion suministro = buscarObjetoAccesibleMasCercano(aliado, ToritoRojo.class);
            int pasosSuministro = suministro == null
                    ? -1
                    : calcularDistanciaRutaAliado(aliado.getPosicion(), suministro);
            if (pasosSuministro > 0
                    && aliado.getEnergia() >= pasosSuministro * costeMovimiento + costeMovimiento) {
                situacionesAliados.put(aliado, SituacionAliado.BUSCANDO_SUMINISTROS);
                moverAliadoHaciaObjetivo(aliado, suministro);
                return true;
            }
            situacionesAliados.put(aliado, SituacionAliado.EN_ESPERA_POR_RECURSOS);
            juego.getConsola().imprimirAdvertencia(aliado.getNombre()
                    + " aplaza la ayuda: no puede llegar sin agotar su energia.");
            return true;
        }
        return false;
    }

    private boolean buscarSuministroNecesarioParaJugador(Aliado aliado) {
        Personaje jugador = juego.getJugador();
        if (jugador.getSalud() < jugador.getSaludMaxima()
                && aliado.getMochila().getObjetos().stream().noneMatch(Botiquin.class::isInstance)
                && moverAliadoHaciaSuministro(aliado, Botiquin.class)) {
            return true;
        }
        return jugador.getEnergia() < jugador.getEnergiaMaxima()
                && aliado.getMochila().getObjetos().stream().noneMatch(ToritoRojo.class::isInstance)
                && moverAliadoHaciaSuministro(aliado, ToritoRojo.class);
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
        situacionesAliados.put(aliado, SituacionAliado.BUSCANDO_SUMINISTROS);
        moverAliadoHaciaObjetivo(aliado, suministro);
        return true;
    }

    private void interactuarConObjetos(Aliado aliado) {
        List<Objeto> objetos = List.copyOf(juego.getMapa().getCelda(aliado.getPosicion()).getObjetos());
        for (Objeto objeto : objetos) {
            if (objeto instanceof Explosivo || !aliado.getMochila().puedeGuardar(objeto)) {
                continue;
            }
            Objeto recogido = juego.getMapa().getCelda(aliado.getPosicion())
                    .quitarObjetoPorNombre(objeto.getNombre());
            if (recogido == null) {
                continue;
            }
            try {
                aliado.coger(recogido);
                juego.getConsola().imprimirInfo(aliado.getNombre() + " recoge " + recogido.getNombre() + ".");
                equiparSiConviene(aliado, recogido);
            } catch (Exception e) {
                juego.getMapa().getCelda(aliado.getPosicion()).agregarObjeto(recogido);
            }
        }
    }

    private void equiparSiConviene(Aliado aliado, Objeto objeto) {
        boolean necesitaEquipo = objeto instanceof Arma && aliado.getArmasEquipadas().isEmpty()
                || objeto instanceof Armadura && aliado.getArmaduraEquipada() == null;
        if (!necesitaEquipo) {
            return;
        }
        Objeto retirado = aliado.getMochila().quitarPorNombre(objeto.getNombre());
        if (retirado == null) {
            return;
        }
        try {
            aliado.equipar(retirado);
            juego.getConsola().imprimirInfo(aliado.getNombre() + " equipa " + objeto.getNombre() + ".");
        } catch (Exception e) {
            aliado.getMochila().guardar(retirado);
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
        return destinatario.getEnergia() < destinatario.getEnergiaMaxima()
                && usarTorito(donante, destinatario);
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
            situacionesAliados.put(donante, destinatario == juego.getJugador()
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
        juego.getConsola().imprimirExito(aliado.getNombre() + " elimina a " + objetivo.getNombre() + ".");
    }

    private void avanzarOrdenAyuda() {
        if (turnosAyudaAliados <= 0) {
            return;
        }
        turnosAyudaAliados--;
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
            situacionesAliados.put(aliado, SituacionAliado.EVACUADO);
            aliadosEnCombate.put(aliado, false);
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
        for (Objeto objeto : aliado.getMochila().getObjetos()) {
            if (objeto instanceof Binocular || objeto.getNombre().toLowerCase().contains("radar")) {
                return true;
            }
        }
        return false;
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
            boolean contiene = juego.getMapa().getCelda(actual).getObjetos().stream().anyMatch(tipo::isInstance);
            if (contiene) {
                return actual;
            }
            for (Direccion direccion : Direccion.values()) {
                Posicion candidata = actual.mover(direccion);
                if (visitadas.contains(candidata) || !juego.getMapa().esTransitable(candidata)) {
                    continue;
                }
                boolean ocupada = !juego.getMapa().getCelda(candidata).getAliados().isEmpty();
                if (ocupada) {
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
                boolean ocupada = !juego.getMapa().getCelda(candidata).getAliados().isEmpty();
                if (ocupada && !candidata.equals(objetivo)) {
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
                boolean ocupadaPorAliado = !juego.getMapa().getCelda(candidata).getAliados().isEmpty();
                if (ocupadaPorAliado && !candidata.equals(objetivo)) {
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
