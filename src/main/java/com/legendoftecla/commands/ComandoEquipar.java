package com.legendoftecla.commands;

import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.items.Objeto;


/**
 * Representa la entidad ComandoEquipar del juego.
 */
public class ComandoEquipar implements Comando {
    private final CommandContext context;
    private final String nombreObjeto;

    /**
     * Ejecuta ComandoEquipar.
     */
    public ComandoEquipar(CommandContext context, String nombreObjeto) {
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
            throw new ComandoException("Ese objeto no esta en la mochila.");
        }
        try {
            context.getJuego().getJugador().equipar(obj);
            context.getJuego().getConsola().imprimir("Equipado: " + obj.getNombre());
        } catch (AccionInvalidaException e) {
            context.getJuego().getJugador().getMochila().guardar(obj);
            throw new ComandoException(e.getMessage());
        }
    }
}

