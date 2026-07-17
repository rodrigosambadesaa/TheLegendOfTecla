package com.legendoftecla.model.characters;

import com.legendoftecla.model.world.Posicion;


/**
 * Representa la entidad HeavyFloater del juego.
 */
public final class HeavyFloater extends Floater {
    /**
     * Ejecuta HeavyFloater.
     */
    public HeavyFloater(String nombre, Posicion posicion, Mochila mochila, int visionBase) {
        super(nombre, 110, 60, posicion, mochila, visionBase);
    }

    @Override
    /**
     * Ejecuta calcularCosteMovimiento.
     */
    protected int calcularCosteMovimiento() {
        return (int) Math.ceil(super.calcularCosteMovimiento() * 1.3);
    }
}

