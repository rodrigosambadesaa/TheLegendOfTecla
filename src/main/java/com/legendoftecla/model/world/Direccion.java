package com.legendoftecla.model.world;


/**
 * Representa la entidad Direccion del juego.
 */
public enum Direccion {
    NORTE(-1, 0),
    SUR(1, 0),
    ESTE(0, 1),
    OESTE(0, -1);

    private final int deltaFila;
    private final int deltaColumna;

    Direccion(int deltaFila, int deltaColumna) {
        this.deltaFila = deltaFila;
        this.deltaColumna = deltaColumna;
    }

    /**
     * Ejecuta getDeltaFila.
     */
    public int getDeltaFila() {
        return deltaFila;
    }

    /**
     * Ejecuta getDeltaColumna.
     */
    public int getDeltaColumna() {
        return deltaColumna;
    }

    /**
     * Ejecuta desdeTexto.
     */
    public static Direccion desdeTexto(String texto) {
        return switch (texto.toLowerCase()) {
            case "n", "norte" -> NORTE;
            case "s", "sur" -> SUR;
            case "e", "este" -> ESTE;
            case "o", "oeste" -> OESTE;
            default -> null;
        };
    }
}

