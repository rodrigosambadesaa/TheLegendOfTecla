package com.legendoftecla.exceptions;


/**
 * Representa la entidad AccionInvalidaException del juego.
 */
public class AccionInvalidaException extends JuegoException {
    /**
     * Ejecuta AccionInvalidaException.
      * @param message valor de {@code message}
     */
    public AccionInvalidaException(String message) {
        super(message);
    }
}

