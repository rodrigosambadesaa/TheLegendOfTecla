package com.legendoftecla.commands;

import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.world.Direccion;


/**
 * Representa la entidad ComandoMover del juego.
 */
public class ComandoMover implements Comando {
    private final CommandContext context;
    private final Direccion direccion;

    /**
     * Ejecuta ComandoMover.
     */
    public ComandoMover(CommandContext context, Direccion direccion) {
        this.context = context;
        this.direccion = direccion;
    }

    @Override
    /**
     * Ejecuta ejecutar.
     */
    public void ejecutar() throws ComandoException {
        try {
            context.getJuego().getJugador().mover(direccion, context.getJuego());
            context.getJuego().registrarPaso();
            context.getJuego().getJugador().registrarPosicion();
            context.getJuego().getConsola().imprimir("Te mueves a " + direccion + ".");
        } catch (AccionInvalidaException e) {
            throw new ComandoException(e.getMessage());
        }
    }
}

