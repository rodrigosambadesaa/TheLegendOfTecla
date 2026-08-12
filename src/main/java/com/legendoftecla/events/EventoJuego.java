package com.legendoftecla.events;

import java.time.Instant;

/** Contrato comun de todos los hechos observables del dominio. */
public interface EventoJuego {
    /** @return instante en el que ocurrio el hecho */
    Instant instante();
}
