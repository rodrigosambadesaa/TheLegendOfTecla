package com.legendoftecla.model.characters;

import com.legendoftecla.model.world.Posicion;


/**
 * Representa la entidad Sectoid del juego.
 */
public final class Sectoid extends Enemigo {
    /**
     * Ejecuta Sectoid.
      * @param mochila valor de {@code mochila}
      * @param nombre valor de {@code nombre}
      * @param posicion valor de {@code posicion}
      * @param visionBase valor de {@code visionBase}
     */
    public Sectoid(String nombre, Posicion posicion, Mochila mochila, int visionBase) {
        super(nombre, 70, 70, posicion, mochila, visionBase);
    }
}

