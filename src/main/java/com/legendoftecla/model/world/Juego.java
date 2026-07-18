package com.legendoftecla.model.world;

import com.legendoftecla.console.Consola;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Jugador;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa la entidad Juego del juego.
 */
public class Juego {
    private final Consola consola;
    private final Mapa mapa;
    private final Jugador jugador;
    private final List<Enemigo> enemigos;
    private final List<Aliado> aliados;
    private final List<Aliado> aliadosRegistrados;
    private final List<Aliado> aliadosExtraidosDetalle;
    private final int pasosMaximos;
    private int aliadosIniciales;
    private int aliadosExtraidos;
    private int pasos;
    private boolean solicitudAyudaAliados;

    /**
     * Ejecuta Juego.
      * @param consola valor de {@code consola}
      * @param jugador valor de {@code jugador}
      * @param mapa valor de {@code mapa}
      * @param pasosMaximos valor de {@code pasosMaximos}
     */
    public Juego(Consola consola, Mapa mapa, Jugador jugador, int pasosMaximos) {
        this.consola = consola;
        this.mapa = mapa;
        this.jugador = jugador;
        this.pasosMaximos = pasosMaximos;
        this.enemigos = new ArrayList<>();
        this.aliados = new ArrayList<>();
        this.aliadosRegistrados = new ArrayList<>();
        this.aliadosExtraidosDetalle = new ArrayList<>();
        this.aliadosIniciales = 0;
        this.aliadosExtraidos = 0;
        this.pasos = 0;
        this.solicitudAyudaAliados = false;
    }

    /**
     * Ejecuta getConsola.
      * @return resultado de la operacion
     */
    public Consola getConsola() {
        return consola;
    }

    /**
     * Ejecuta getMapa.
      * @return resultado de la operacion
     */
    public Mapa getMapa() {
        return mapa;
    }

    /**
     * Ejecuta getJugador.
      * @return resultado de la operacion
     */
    public Jugador getJugador() {
        return jugador;
    }

    /**
     * Ejecuta getPasos.
      * @return resultado de la operacion
     */
    public int getPasos() {
        return pasos;
    }

    /**
     * Ejecuta getPasosMaximos.
      * @return resultado de la operacion
     */
    public int getPasosMaximos() {
        return pasosMaximos;
    }

    /**
     * Ejecuta registrarPaso.
     */
    public void registrarPaso() {
        pasos++;
    }

    /**
     * Ejecuta agregarEnemigo.
      * @param enemigo valor de {@code enemigo}
     */
    public void agregarEnemigo(Enemigo enemigo) {
        enemigos.add(enemigo);
    }

    /**
     * Ejecuta getEnemigos.
      * @return resultado de la operacion
     */
    public List<Enemigo> getEnemigos() {
        return enemigos;
    }

    /**
     * Ejecuta agregarAliado.
      * @param aliado valor de {@code aliado}
     */
    public void agregarAliado(Aliado aliado) {
        aliados.add(aliado);
        aliadosRegistrados.add(aliado);
        aliadosIniciales++;
    }

    /**
     * Ejecuta getAliados.
      * @return resultado de la operacion
     */
    public List<Aliado> getAliados() {
        return aliados;
    }

    /**
     * Obtiene todos los aliados que participaron en la partida, incluidos los evacuados.
     *
     * @return vista inmutable del historial completo de aliados
     */
    public List<Aliado> getAliadosRegistrados() {
        return Collections.unmodifiableList(aliadosRegistrados);
    }

    /**
     * Indica si un aliado llego con vida a la salida del mapa.
     *
     * @param aliado aliado cuyo estado se consulta
     * @return {@code true} cuando el aliado ya fue evacuado
     */
    public boolean estaAliadoExtraido(Aliado aliado) {
        return aliadosExtraidosDetalle.contains(aliado);
    }

    /**
     * Ejecuta getAliadosIniciales.
      * @return resultado de la operacion
     */
    public int getAliadosIniciales() {
        return aliadosIniciales;
    }

    /**
     * Ejecuta getAliadosExtraidos.
      * @return resultado de la operacion
     */
    public int getAliadosExtraidos() {
        return aliadosExtraidos;
    }

    /**
     * Ejecuta extraerAliado.
      * @param aliado valor de {@code aliado}
      * @return resultado de la operacion
     */
    public boolean extraerAliado(Aliado aliado) {
        if (aliado == null || aliado.getSalud() <= 0) {
            return false;
        }
        if (!aliados.remove(aliado)) {
            return false;
        }
        aliadosExtraidosDetalle.add(aliado);
        aliadosExtraidos++;
        return true;
    }

    /** Registra una orden para que los aliados acudan a ayudar al jugador. */
    public void solicitarAyudaAliados() {
        solicitudAyudaAliados = true;
    }

    /**
     * Consume la orden de ayuda pendiente para que el motor la active una sola vez.
     *
     * @return {@code true} si habia una solicitud pendiente
     */
    public boolean consumirSolicitudAyudaAliados() {
        boolean pendiente = solicitudAyudaAliados;
        solicitudAyudaAliados = false;
        return pendiente;
    }

    /**
     * Ejecuta jugadorGano.
      * @return resultado de la operacion
     */
    public boolean jugadorGano() {
        if (!jugador.getPosicion().equals(mapa.getObjetivo())) {
            return false;
        }
        if (aliadosIniciales <= 0) {
            return true;
        }
        return aliadosExtraidos == aliadosIniciales;
    }

    /**
     * Ejecuta jugadorMuerto.
      * @return resultado de la operacion
     */
    public boolean jugadorMuerto() {
        return jugador.getSalud() <= 0 || jugador.getEnergia() <= 0;
    }

    /**
     * Ejecuta excedioPasos.
      * @return resultado de la operacion
     */
    public boolean excedioPasos() {
        return pasos > pasosMaximos;
    }
}
