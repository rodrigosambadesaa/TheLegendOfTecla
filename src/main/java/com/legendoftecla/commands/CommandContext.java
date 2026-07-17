package com.legendoftecla.commands;

import com.legendoftecla.model.world.Juego;


/**
 * Representa la entidad CommandContext del juego.
 */
public class CommandContext {
    private final Juego juego;

    /**
     * Ejecuta CommandContext.
     */
    public CommandContext(Juego juego) {
        this.juego = juego;
    }

    /**
     * Ejecuta getJuego.
     */
    public Juego getJuego() {
        return juego;
    }
}

