package com.legendoftecla.effects;

import java.util.Objects;

/** Vista persistible de un estado aplicado. */
public record EstadoActivo(TipoEstado tipo, int turnosRestantes, int acumulaciones) {
    /** Valida el estado serializable. */
    public EstadoActivo {
        Objects.requireNonNull(tipo, "Tipo");
        if (turnosRestantes < 1 || acumulaciones < 1) {
            throw new IllegalArgumentException("Duracion y acumulaciones deben ser positivas.");
        }
    }
}
