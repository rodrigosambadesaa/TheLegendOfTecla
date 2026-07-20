package com.legendoftecla.commands;

import com.legendoftecla.model.world.Juego;
import com.legendoftecla.validation.Validaciones;


/**
 * Representa la entidad CommandContext del juego.
 */
public class CommandContext {
    private Juego juego;

    /**
     * Ejecuta CommandContext.
      * @param juego valor de {@code juego}
     */
    public CommandContext(Juego juego) {
        setJuego(juego);
    }

    /**
     * Ejecuta getJuego.
      * @return resultado de la operacion
     */
    public Juego getJuego() {
        return juego;
    }

    /** @param juego partida no nula */
    public void setJuego(Juego juego) {
        this.juego = Validaciones.noNulo(juego, "Juego");
    }
}

