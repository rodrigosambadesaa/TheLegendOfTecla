package com.legendoftecla.model.characters;

import com.legendoftecla.model.world.Posicion;


/**
 * Representa la entidad Sectoid del juego.
 */
public final class Sectoid extends Enemigo {
    /**
     * Ejecuta Sectoid.
     */
    public Sectoid(String nombre, Posicion posicion, Mochila mochila, int visionBase) {
        super(nombre, 70, 70, posicion, mochila, visionBase);
    }
}

