package com.legendoftecla.procedural;
/** Parametros acotados de generacion procedural. */
public record ConfiguracionGeneracion(int filas, int columnas, int peligros,
        int puertas, double densidadMuros) {
    public ConfiguracionGeneracion {
        if (filas < 5 || columnas < 5 || peligros < 0 || puertas < 0
                || densidadMuros < 0 || densidadMuros > 0.65) {
            throw new IllegalArgumentException("Configuracion procedural invalida");
        }
    }
    public static ConfiguracionGeneracion normal(int filas, int columnas) {
        return new ConfiguracionGeneracion(filas, columnas, 3, 2, 0.28);
    }
}
