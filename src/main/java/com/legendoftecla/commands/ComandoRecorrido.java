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
    private static final int MAXIMO_PUNTOS_MOSTRADOS = 100;

    public void ejecutar() {
        var jugador = context.getJuego().getJugador();
        int total = jugador.getLongitudRecorrido();
        var recientes = jugador.getRecorridoReciente(MAXIMO_PUNTOS_MOSTRADOS);
        StringBuilder sb = new StringBuilder("Recorrido: ");
        if (total > recientes.size()) {
            sb.append("(ultimas ").append(recientes.size()).append(" de ")
                    .append(total).append(" posiciones) ");
        }
        recientes.forEach(p -> sb.append(p).append(" "));
        context.getJuego().getConsola().imprimir(sb.toString());
    }
}

