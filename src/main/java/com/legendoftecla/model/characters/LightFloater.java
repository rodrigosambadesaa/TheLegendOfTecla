package com.legendoftecla.model.characters;

import com.legendoftecla.model.world.Posicion;


/**
 * Representa la entidad LightFloater del juego.
 */
public final class LightFloater extends Floater {
    /**
     * Ejecuta LightFloater.
      * @param mochila valor de {@code mochila}
      * @param nombre valor de {@code nombre}
      * @param posicion valor de {@code posicion}
      * @param visionBase valor de {@code visionBase}
     */
    public LightFloater(String nombre, Posicion posicion, Mochila mochila, int visionBase) {
        super(nombre, 60, 90, posicion, mochila, visionBase + 1);
    }
}

