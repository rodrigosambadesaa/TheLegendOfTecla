package com.legendoftecla.commands;

import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.inventory.CatalogoRecetas;
import com.legendoftecla.inventory.ResultadoFabricacion;
import com.legendoftecla.inventory.SistemaFabricacion;

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
        return CatalogoRecetas.predeterminado();
    }
}
