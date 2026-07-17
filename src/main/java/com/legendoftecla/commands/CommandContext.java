package com.legendoftecla.commands;

import com.legendoftecla.model.world.Juego;


/**
 * Representa la entidad CommandContext del juego.
 */
public class CommandContext {
    private final Juego juego;

    /**
     * Ejecuta CommandContext.
      * @param juego valor de {@code juego}
     */
    public CommandContext(Juego juego) {
        this.juego = juego;
    }

    /**
     * Ejecuta getJuego.
      * @return resultado de la operacion
     */
    public Juego getJuego() {
        return juego;
    }
}

