package com.legendoftecla.commands;

import com.legendoftecla.validation.Validaciones;

/**
 * Representa la entidad ComandoRecorrido del juego.
 */
public class ComandoRecorrido implements Comando {
    private CommandContext context;

    /**
     * Ejecuta ComandoRecorrido.
      * @param context valor de {@code context}
     */
    public ComandoRecorrido(CommandContext context) {
        setContext(context);
    }

    /** @return contexto de ejecucion */
    public CommandContext getContext() { return context; }
    /** @param context contexto no nulo */
    public void setContext(CommandContext context) {
        this.context = Validaciones.noNulo(context, "Contexto");
    }

    @Override
    /**
     * Ejecuta ejecutar.
     */
    public void ejecutar() {
        StringBuilder sb = new StringBuilder("Recorrido: ");
        context.getJuego().getJugador().getRecorrido().forEach(p -> sb.append(p).append(" "));
        context.getJuego().getConsola().imprimir(sb.toString());
    }
}

