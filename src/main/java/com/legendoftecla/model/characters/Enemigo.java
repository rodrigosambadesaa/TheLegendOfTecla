package com.legendoftecla.model.characters;

import com.legendoftecla.model.world.Posicion;

/**
 * Representa la entidad Enemigo del juego.
 */
public abstract class Enemigo extends Personaje {
    private static double multiplicadorDanioGlobal = 1.0;

    /**
     * Ejecuta Enemigo.
     */
    protected Enemigo(String nombre, int salud, int energia, Posicion posicion, Mochila mochila, int visionBase) {
        super(nombre, salud, energia, posicion, mochila, visionBase);
    }

    @Override
    /**
     * Ejecuta aplicarModificadorDanio.
     */
    protected int aplicarModificadorDanio(int base, Personaje objetivo) {
        return Math.max(1, (int) Math.round(base * multiplicadorDanioGlobal));
    }

    public static void setMultiplicadorDanioGlobal(double multiplicador) {
        multiplicadorDanioGlobal = Math.max(0.1, multiplicador);
    }
}
