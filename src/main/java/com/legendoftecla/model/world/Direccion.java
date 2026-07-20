package com.legendoftecla.model.world;


/**
 * Representa la entidad Direccion del juego.
 */
public enum Direccion {
    /**
     * Opcion {@code NORTE}.
     */
    NORTE(-1, 0),
    /**
     * Opcion {@code SUR}.
     */
    SUR(1, 0),
    /**
     * Indica el estado de {@code TE}.
     */
    ESTE(0, 1),
    /**
     * Opcion {@code OESTE}.
     */
    OESTE(0, -1);

    private final int deltaFila;
    private final int deltaColumna;

    Direccion(int deltaFila, int deltaColumna) {
        this.deltaFila = deltaFila;
        this.deltaColumna = deltaColumna;
    }

    /**
     * Ejecuta getDeltaFila.
      * @return resultado de la operacion
     */
    public int getDeltaFila() {
        return deltaFila;
    }

    /**
     * Ejecuta getDeltaColumna.
      * @return resultado de la operacion
     */
    public int getDeltaColumna() {
        return deltaColumna;
    }

    /**
     * Ejecuta desdeTexto.
      * @param texto valor de {@code texto}
      * @return resultado de la operacion
     */
    public static Direccion desdeTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return switch (texto.toLowerCase()) {
            case "n", "norte" -> NORTE;
            case "s", "sur" -> SUR;
            case "e", "este" -> ESTE;
            case "o", "oeste" -> OESTE;
            default -> null;
        };
    }
}

