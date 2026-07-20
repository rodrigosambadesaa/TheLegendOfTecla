package com.legendoftecla.model.characters;

import com.legendoftecla.model.world.Posicion;


/**
 * Representa la entidad Zapador del juego.
 */
public final class Zapador extends Jugador {
    /**
     * Ejecuta Zapador.
      * @param mochila valor de {@code mochila}
      * @param nombre valor de {@code nombre}
      * @param posicion valor de {@code posicion}
      * @param visionBase valor de {@code visionBase}
     */
    public Zapador(String nombre, Posicion posicion, Mochila mochila, int visionBase) {
        super(nombre, 105, 95, posicion, mochila, visionBase);
    }

    @Override
    /**
     * Ejecuta aplicarModificadorDanio.
     */
    protected int aplicarModificadorDanio(int base, Personaje objetivo) {
        int distancia = getPosicion().distanciaManhattan(objetivo.getPosicion());
        if (distancia > 2) {
            return Math.max(1, (int) Math.ceil(base * 0.05));
        }
        return base;
    }
}

