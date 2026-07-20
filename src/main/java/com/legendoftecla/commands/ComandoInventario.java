package com.legendoftecla.commands;

import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.validation.Validaciones;


/**
 * Representa la entidad ComandoInventario del juego.
 */
public class ComandoInventario implements Comando {
    private CommandContext context;

    /**
     * Ejecuta ComandoInventario.
      * @param context valor de {@code context}
     */
    public ComandoInventario(CommandContext context) {
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
        var mochila = context.getJuego().getJugador().getMochila();
        context.getJuego().getConsola()
                .imprimirInfo("Mochila: peso " + String.format("%.2f", mochila.getPesoActual()) + "/"
                        + mochila.getPesoMax() + " kg, espacio restante " + mochila.getEspacioRestante());
        for (Objeto objeto : mochila.getObjetos()) {
            context.getJuego().getConsola().imprimirExito("- " + objeto);
        }
        if (mochila.getObjetos().isEmpty()) {
            context.getJuego().getConsola().imprimirAdvertencia("(vacia)");
        }
    }
}

