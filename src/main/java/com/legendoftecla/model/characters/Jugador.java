package com.legendoftecla.model.characters;

import com.legendoftecla.model.world.Posicion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Representa la entidad Jugador del juego.
 */
public abstract class Jugador extends Personaje {
    private final List<Posicion> recorrido;

    /**
     * Ejecuta Jugador.
     */
    protected Jugador(String nombre, int salud, int energia, Posicion posicion, Mochila mochila, int visionBase) {
        super(nombre, salud, energia, posicion, mochila, visionBase);
        this.recorrido = new ArrayList<>();
        this.recorrido.add(posicion);
    }

    /**
     * Ejecuta registrarPosicion.
     */
    public void registrarPosicion() {
        recorrido.add(posicion);
    }

    /**
     * Ejecuta getRecorrido.
     */
    public List<Posicion> getRecorrido() {
        return Collections.unmodifiableList(recorrido);
    }
}

