package com.legendoftecla.model.characters;

import com.legendoftecla.model.world.Posicion;


/**
 * Representa la entidad Floater del juego.
 */
public abstract class Floater extends Enemigo {
    /**
     * Ejecuta Floater.
     */
    protected Floater(String nombre, int salud, int energia, Posicion posicion, Mochila mochila, int visionBase) {
        super(nombre, salud, energia, posicion, mochila, visionBase);
    }
}

