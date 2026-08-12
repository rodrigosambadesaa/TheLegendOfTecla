package com.legendoftecla.inventory;

import com.legendoftecla.model.items.Botiquin;
import com.legendoftecla.model.items.Componente;
import com.legendoftecla.model.items.Explosivo;
import com.legendoftecla.model.items.Linterna;

import java.util.List;

/** Construye el pequeño catálogo predeterminado sin acoplarlo a los comandos. */
public final class CatalogoRecetas {
    private CatalogoRecetas() { }

    /** @return sistema nuevo con recetas base extensibles */
    public static SistemaFabricacion predeterminado() {
        SistemaFabricacion sistema = new SistemaFabricacion();
        sistema.registrar(new Receta("mina", List.of(
                new Ingrediente("Componentes", 1),
                new Ingrediente("Explosivo", 1)),
                () -> new Explosivo("Mina", "Fabricada", 2)));
        sistema.registrar(new Receta("botiquin", List.of(
                new Ingrediente("Vendas", 1),
                new Ingrediente("Medicamento", 1)),
                () -> new Botiquin("Botiquin", "Fabricado", 1, 20)));
        sistema.registrar(new Receta("antorcha", List.of(
                new Ingrediente("Combustible", 1),
                new Ingrediente("Trapo", 1)),
                () -> new Linterna("Antorcha", "Fabricada", 1, 3)));
        sistema.registrar(new Receta("kit reparacion", List.of(
                new Ingrediente("Piezas", 2)),
                () -> new Componente("Kit reparacion",
                        "Permite reparar equipamiento en futuras recetas", 1)));
        return sistema;
    }
}
