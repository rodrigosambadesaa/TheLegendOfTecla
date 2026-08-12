package com.legendoftecla.progression;
/** Nivel minimo y prerrequisito opcional. */
public record RequisitoHabilidad(int nivelMinimo, String habilidadPrevia) {
    public RequisitoHabilidad {
        if (nivelMinimo < 1) throw new IllegalArgumentException("Nivel invalido");
    }
}
