package com.legendoftecla.commands;

import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.inventory.*;
import com.legendoftecla.model.items.*;
import java.util.List;

/** Lista o ejecuta recetas predeterminadas. */
public final class ComandoFabricar implements Comando {
    private final CommandContext contexto;
    private final String receta;
    public ComandoFabricar(CommandContext contexto, String receta) {
        this.contexto = contexto; this.receta = receta;
    }
    @Override public void ejecutar() throws ComandoException {
        SistemaFabricacion sistema = recetas();
        if (receta == null) {
            sistema.recetas().forEach(r -> contexto.getJuego().getConsola().imprimirInfo(
                    r.nombre() + ": " + r.ingredientes()));
            return;
        }
        ResultadoFabricacion resultado = sistema.fabricar(receta,
                contexto.getJuego().getJugador().getMochila());
        if (!resultado.exito()) throw new ComandoException(resultado.mensaje());
        contexto.getJuego().getConsola().imprimirExito(resultado.mensaje());
    }
    private SistemaFabricacion recetas() {
        SistemaFabricacion sistema = new SistemaFabricacion();
        sistema.registrar(new Receta("mina", List.of(new Ingrediente("Componentes", 1),
                new Ingrediente("Explosivo", 1)), () -> new Explosivo("Mina", "Fabricada", 2)));
        sistema.registrar(new Receta("botiquin", List.of(new Ingrediente("Vendas", 1),
                new Ingrediente("Medicamento", 1)), () -> new Botiquin("Botiquin", "Fabricado", 1, 20)));
        sistema.registrar(new Receta("antorcha", List.of(new Ingrediente("Combustible", 1),
                new Ingrediente("Trapo", 1)), () -> new Linterna("Antorcha", "Fabricada", 1, 3)));
        return sistema;
    }
}
