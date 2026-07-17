package com.legendoftecla.commands;

import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.world.Celda;


/**
 * Representa la entidad ComandoCoger del juego.
 */
public class ComandoCoger implements Comando {
    private final CommandContext context;
    private final String nombreObjeto;

    /**
     * Ejecuta ComandoCoger.
      * @param context valor de {@code context}
      * @param nombreObjeto valor de {@code nombreObjeto}
     */
    public ComandoCoger(CommandContext context, String nombreObjeto) {
        this.context = context;
        this.nombreObjeto = nombreObjeto;
    }

    @Override
    /**
     * Ejecuta ejecutar.
     */
    public void ejecutar() throws ComandoException {
        Celda celda = context.getJuego().getMapa().getCelda(context.getJuego().getJugador().getPosicion());
        Objeto objeto = celda.quitarObjetoPorNombre(nombreObjeto);
        if (objeto == null) {
            throw new ComandoException("No existe ese objeto en la celda.");
        }
        try {
            context.getJuego().getJugador().coger(objeto);
            context.getJuego().getConsola().imprimir("Recoges " + objeto.getNombre() + ".");
        } catch (AccionInvalidaException e) {
            celda.agregarObjeto(objeto);
            throw new ComandoException(e.getMessage());
        }
    }
}

