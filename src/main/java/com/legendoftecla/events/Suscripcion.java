package com.legendoftecla.events;

/** Registro revocable de un listener del bus. */
@FunctionalInterface
public interface Suscripcion extends AutoCloseable {
    /** Elimina el listener; repetir la operacion es seguro. */
    @Override
    void close();
}
