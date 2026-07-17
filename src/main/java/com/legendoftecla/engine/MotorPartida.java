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
import com.legendoftecla.model.items.Binocular;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.model.world.SistemaPuntuacion;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Controla una partida completa sin conocer si se muestra en consola o en GUI.
 */
public final class MotorPartida {
    private final Juego juego;
    private final CommandContext contexto;
    private final CommandParser parser;
    private final Random random;
    private boolean finalizada;
    private SistemaPuntuacion.EstadoFinalPartida estadoFinal;

    /**
     * Crea una instancia de {@code MotorPartida}.
      * @param juego valor de {@code juego}
     */
    public MotorPartida(Juego juego) {
        this.juego = juego;
        this.contexto = new CommandContext(juego);
        this.parser = new CommandParser(contexto);
        this.random = new Random();
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
                + "  Pasos " + juego.getPasos() + "/" + juego.getPasosMaximos();
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
            ejecutarTurnoAliados();
            ejecutarTurnoNPC();
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
        if (juego.jugadorGano()) {
            finalizar(SistemaPuntuacion.EstadoFinalPartida.VICTORIA);
        } else if (juego.jugadorMuerto()) {
            finalizar(SistemaPuntuacion.EstadoFinalPartida.MUERTE);
        } else if (juego.excedioPasos()) {
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
                continue;
            }
            if (extraerAliadoSiProcede(aliado)) {
                continue;
            }
            Enemigo objetivo = buscarEnemigoMasCercano(aliado);
            if (objetivo == null) {
                Posicion destino = juego.getJugador().getPosicion().equals(juego.getMapa().getObjetivo())
                        ? juego.getMapa().getObjetivo()
                        : juego.getJugador().getPosicion();
                moverAliadoHaciaObjetivo(aliado, destino);
                extraerAliadoSiProcede(aliado);
                continue;
            }
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

    private boolean extraerAliadoSiProcede(Aliado aliado) {
        if (!aliado.getPosicion().equals(juego.getMapa().getObjetivo())) {
            return false;
        }
        juego.getMapa().getCelda(aliado.getPosicion()).quitarAliado(aliado);
        if (juego.extraerAliado(aliado)) {
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
        Direccion mejor = null;
        int mejorDistancia = origen.distanciaManhattan(objetivo);
        for (Direccion direccion : Direccion.values()) {
            Posicion candidata = origen.mover(direccion);
            if (!juego.getMapa().esTransitable(candidata)
                    || !juego.getMapa().getCelda(candidata).getAliados().isEmpty()) {
                continue;
            }
            int distancia = candidata.distanciaManhattan(objetivo);
            if (distancia < mejorDistancia) {
                mejorDistancia = distancia;
                mejor = direccion;
            }
        }
        if (mejor == null) {
            return;
        }
        try {
            juego.getMapa().getCelda(origen).quitarAliado(aliado);
            aliado.mover(mejor, juego);
            juego.getMapa().getCelda(aliado.getPosicion()).agregarAliado(aliado);
        } catch (Exception e) {
            juego.getMapa().getCelda(origen).agregarAliado(aliado);
        }
    }
}
