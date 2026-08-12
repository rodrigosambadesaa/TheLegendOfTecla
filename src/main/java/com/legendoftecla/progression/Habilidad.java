package com.legendoftecla.progression;
/** Nodo extensible de un arbol de habilidades. */
public record Habilidad(String id, String nombre, RequisitoHabilidad requisito,
        EfectoHabilidad efecto) {
    public Habilidad {
        java.util.Objects.requireNonNull(id, "ID");
        java.util.Objects.requireNonNull(nombre, "Nombre");
        java.util.Objects.requireNonNull(requisito, "Requisito");
        java.util.Objects.requireNonNull(efecto, "Efecto");
    }
}
