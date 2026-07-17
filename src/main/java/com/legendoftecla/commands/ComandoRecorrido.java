package com.legendoftecla.commands;


/**
 * Representa la entidad ComandoRecorrido del juego.
 */
public class ComandoRecorrido implements Comando {
    private final CommandContext context;

    /**
     * Ejecuta ComandoRecorrido.
      * @param context valor de {@code context}
     */
    public ComandoRecorrido(CommandContext context) {
        this.context = context;
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

