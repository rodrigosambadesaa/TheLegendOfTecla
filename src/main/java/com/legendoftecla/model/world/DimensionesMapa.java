package com.legendoftecla.model.world;

/**
 * Tamano de mapa configurable por el jugador.
 */
public record DimensionesMapa(int filas, int columnas) {
    public DimensionesMapa {
        if (filas < 3 || columnas < 3) {
            throw new IllegalArgumentException("El mapa debe tener al menos 3x3.");
        }
    }
}