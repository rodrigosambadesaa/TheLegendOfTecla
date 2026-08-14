package com.legendoftecla.commands;

import com.legendoftecla.model.world.Juego;
import com.legendoftecla.validation.Validaciones;

import java.util.Random;


/**
 * Representa la entidad CommandContext del juego.
 */
public class CommandContext {
    private Juego juego;
    private Random random;

    /**
     * Ejecuta CommandContext.
      * @param juego valor de {@code juego}
     */
    public CommandContext(Juego juego) {
        setJuego(juego);
        setRandom(new Random());
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

    /** @return fuente aleatoria compartida por los comandos de la partida */
    public Random getRandom() {
        return random;
    }

    /** @param random fuente aleatoria inyectable para partidas reproducibles */
    public void setRandom(Random random) {
        this.random = Validaciones.noNulo(random, "Generador aleatorio de comandos");
    }
}

