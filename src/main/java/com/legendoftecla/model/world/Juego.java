package com.legendoftecla.model.world;

import com.legendoftecla.console.Consola;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Jugador;

import java.util.ArrayList;
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
    private final int pasosMaximos;
    private int aliadosIniciales;
    private int aliadosExtraidos;
    private int pasos;

    /**
     * Ejecuta Juego.
     */
    public Juego(Consola consola, Mapa mapa, Jugador jugador, int pasosMaximos) {
        this.consola = consola;
        this.mapa = mapa;
        this.jugador = jugador;
        this.pasosMaximos = pasosMaximos;
        this.enemigos = new ArrayList<>();
        this.aliados = new ArrayList<>();
        this.aliadosIniciales = 0;
        this.aliadosExtraidos = 0;
        this.pasos = 0;
    }

    /**
     * Ejecuta getConsola.
     */
    public Consola getConsola() {
        return consola;
    }

    /**
     * Ejecuta getMapa.
     */
    public Mapa getMapa() {
        return mapa;
    }

    /**
     * Ejecuta getJugador.
     */
    public Jugador getJugador() {
        return jugador;
    }

    /**
     * Ejecuta getPasos.
     */
    public int getPasos() {
        return pasos;
    }

    /**
     * Ejecuta getPasosMaximos.
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
     */
    public void agregarEnemigo(Enemigo enemigo) {
        enemigos.add(enemigo);
    }

    /**
     * Ejecuta getEnemigos.
     */
    public List<Enemigo> getEnemigos() {
        return enemigos;
    }

    /**
     * Ejecuta agregarAliado.
     */
    public void agregarAliado(Aliado aliado) {
        aliados.add(aliado);
        aliadosIniciales++;
    }

    /**
     * Ejecuta getAliados.
     */
    public List<Aliado> getAliados() {
        return aliados;
    }

    /**
     * Ejecuta getAliadosIniciales.
     */
    public int getAliadosIniciales() {
        return aliadosIniciales;
    }

    /**
     * Ejecuta getAliadosExtraidos.
     */
    public int getAliadosExtraidos() {
        return aliadosExtraidos;
    }

    /**
     * Ejecuta extraerAliado.
     */
    public boolean extraerAliado(Aliado aliado) {
        if (aliado == null || aliado.getSalud() <= 0) {
            return false;
        }
        if (!aliados.remove(aliado)) {
            return false;
        }
        aliadosExtraidos++;
        return true;
    }

    /**
     * Ejecuta jugadorGano.
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
     */
    public boolean jugadorMuerto() {
        return jugador.getSalud() <= 0 || jugador.getEnergia() <= 0;
    }

    /**
     * Ejecuta excedioPasos.
     */
    public boolean excedioPasos() {
        return pasos > pasosMaximos;
    }
}
