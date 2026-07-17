package com.legendoftecla.model.characters;

import com.legendoftecla.model.world.Posicion;


/**
 * Representa la entidad LightFloater del juego.
 */
public final class LightFloater extends Floater {
    /**
     * Ejecuta LightFloater.
     */
    public LightFloater(String nombre, Posicion posicion, Mochila mochila, int visionBase) {
        super(nombre, 60, 90, posicion, mochila, visionBase + 1);
    }
}

