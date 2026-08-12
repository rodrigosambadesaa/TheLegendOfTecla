package com.legendoftecla.persistence;

import java.util.Set;

/** Progreso versionado de una campaña; las misiones se cargan desde su definición. */
public record CampanaGuardada(int version, String campanaId, int indice,
        int nivel, int experiencia, Set<String> habilidades) {
    public static final int VERSION_ACTUAL = 1;
}
