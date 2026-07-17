package com.legendoftecla.model.world;

/**
 * Tamano de mapa configurable por el jugador.
  * @param columnas valor de {@code columnas}
  * @param filas valor de {@code filas}
 */
public record DimensionesMapa(int filas, int columnas) {
    /**
     * Valida y crea una instancia de {@code DimensionesMapa}.
     */
    public DimensionesMapa {
        if (filas < 3 || columnas < 3) {
            throw new IllegalArgumentException("El mapa debe tener al menos 3x3.");
        }
    }
}
