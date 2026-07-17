package com.legendoftecla.exceptions;


/**
 * Representa la entidad JuegoException del juego.
 */
public class JuegoException extends Exception {
    /**
     * Ejecuta JuegoException.
      * @param message valor de {@code message}
     */
    public JuegoException(String message) {
        super(message);
    }
}

