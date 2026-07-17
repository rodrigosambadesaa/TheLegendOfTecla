package com.legendoftecla.exceptions;


/**
 * Representa la entidad ComandoException del juego.
 */
public class ComandoException extends JuegoException {
    /**
     * Ejecuta ComandoException.
      * @param message valor de {@code message}
     */
    public ComandoException(String message) {
        super(message);
    }
}

