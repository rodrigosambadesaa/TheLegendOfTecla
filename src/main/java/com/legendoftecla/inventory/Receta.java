package com.legendoftecla.inventory;
import com.legendoftecla.model.items.Objeto;
import java.util.List;
import java.util.function.Supplier;
/** Receta extensible con fabrica de resultado. */
public record Receta(String nombre, List<Ingrediente> ingredientes,
        Supplier<Objeto> fabrica) {
    public Receta {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre invalido");
        ingredientes = List.copyOf(java.util.Objects.requireNonNull(
                ingredientes, "Ingredientes"));
        if (ingredientes.isEmpty()) {
            throw new IllegalArgumentException("Una receta necesita ingredientes");
        }
        java.util.Objects.requireNonNull(fabrica, "Fabrica");
    }
}
