package com.legendoftecla.model.characters;

import com.legendoftecla.model.world.Posicion;


/**
 * Representa la entidad Francotirador del juego.
 */
public final class Francotirador extends Jugador {
    /**
     * Ejecuta Francotirador.
      * @param mochila valor de {@code mochila}
      * @param nombre valor de {@code nombre}
      * @param posicion valor de {@code posicion}
      * @param visionBase valor de {@code visionBase}
     */
    public Francotirador(String nombre, Posicion posicion, Mochila mochila, int visionBase) {
        super(nombre, 100, 100, posicion, mochila, visionBase + 1);
    }

    @Override
    /**
     * Ejecuta aplicarModificadorDanio.
     */
    protected int aplicarModificadorDanio(int base, Personaje objetivo) {
        int distancia = Math.max(1, posicion.distanciaManhattan(objetivo.getPosicion()));
        double factor = Math.pow(distancia, 1.2);
        return (int) Math.ceil(base + factor);
    }

    @Override
    /**
     * Estima el coste de movimiento propio del francotirador.
     */
    public int estimarCosteMovimiento() {
        return Math.max(1, super.estimarCosteMovimiento() - 1);
    }
}

