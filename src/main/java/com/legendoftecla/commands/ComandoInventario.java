package com.legendoftecla.commands;

import com.legendoftecla.model.items.Objeto;


/**
 * Representa la entidad ComandoInventario del juego.
 */
public class ComandoInventario implements Comando {
    private final CommandContext context;

    /**
     * Ejecuta ComandoInventario.
     */
    public ComandoInventario(CommandContext context) {
        this.context = context;
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

