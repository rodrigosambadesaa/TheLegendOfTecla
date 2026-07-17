package com.legendoftecla.commands;

import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.exceptions.JuegoException;
import com.legendoftecla.model.items.Objeto;


/**
 * Representa la entidad ComandoUsar del juego.
 */
public class ComandoUsar implements Comando {
    private final CommandContext context;
    private final String nombreObjeto;

    /**
     * Ejecuta ComandoUsar.
     */
    public ComandoUsar(CommandContext context, String nombreObjeto) {
        this.context = context;
        this.nombreObjeto = nombreObjeto;
    }

    @Override
    /**
     * Ejecuta ejecutar.
     */
    public void ejecutar() throws ComandoException {
        Objeto obj = context.getJuego().getJugador().getMochila().quitarPorNombre(nombreObjeto);
        if (obj == null) {
            throw new ComandoException("No tienes ese objeto en la mochila.");
        }
        try {
            obj.usar(context.getJuego().getJugador());
            context.getJuego().getConsola().imprimir("Usas " + obj.getNombre() + ".");
        } catch (JuegoException e) {
            context.getJuego().getJugador().getMochila().guardar(obj);
            throw new ComandoException(e.getMessage());
        }
    }
}

