package com.legendoftecla.model.characters;

import com.legendoftecla.model.world.Posicion;


/**
 * Representa la entidad Aliado del juego.
 */
public final class Aliado extends Personaje {
    /**
     * Ejecuta Aliado.
     */
    public Aliado(String nombre, Posicion posicion, Mochila mochila, int visionBase) {
        super(nombre, 90, 140, posicion, mochila, visionBase);
    }

    @Override
    /**
     * Ejecuta aplicarModificadorDanio.
     */
    protected int aplicarModificadorDanio(int base, Personaje objetivo) {
        int distancia = Math.max(1, posicion.distanciaManhattan(objetivo.getPosicion()));
        if (distancia <= 1) {
            return (int) Math.ceil(base * 1.4);
        }
        return base;
    }
}

