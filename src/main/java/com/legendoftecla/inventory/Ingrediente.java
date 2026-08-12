package com.legendoftecla.inventory;
/** Requisito nominal y cuantificado de una receta. */
public record Ingrediente(String nombre, int cantidad) {
    public Ingrediente {
        if (nombre == null || nombre.isBlank() || cantidad < 1) {
            throw new IllegalArgumentException("Ingrediente invalido");
        }
    }
}
