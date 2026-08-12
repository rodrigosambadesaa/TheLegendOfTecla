package com.legendoftecla.inventory;
import com.legendoftecla.model.items.Objeto;
import java.util.List;
import java.util.function.Supplier;
/** Receta extensible con fabrica de resultado. */
public record Receta(String nombre, List<Ingrediente> ingredientes,
        Supplier<Objeto> fabrica) {
    public Receta {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre invalido");
        ingredientes = List.copyOf(ingredientes);
        java.util.Objects.requireNonNull(fabrica, "Fabrica");
    }
}
