package com.legendoftecla.exceptions;

/**
 * Indica que la entrada estandar se cerro y la sesion de consola debe terminar.
 */
public class FinEntradaException extends RuntimeException {
    public FinEntradaException() {
        super("La entrada estandar esta cerrada.");
    }
}
