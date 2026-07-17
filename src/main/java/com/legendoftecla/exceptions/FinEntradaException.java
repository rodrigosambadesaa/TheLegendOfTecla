package com.legendoftecla.exceptions;

/**
 * Indica que la entrada estandar se cerro y la sesion de consola debe terminar.
 */
public class FinEntradaException extends RuntimeException {
    /**
     * Crea una instancia de {@code FinEntradaException}.
     */
    public FinEntradaException() {
        super("La entrada estandar esta cerrada.");
    }
}
