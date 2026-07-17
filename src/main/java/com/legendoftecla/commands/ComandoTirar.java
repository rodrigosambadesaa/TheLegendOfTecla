package com.legendoftecla.commands;

import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.world.Celda;


/**
 * Representa la entidad ComandoTirar del juego.
 */
public class ComandoTirar implements Comando {
    private final CommandContext context;
    private final String nombreObjeto;

    /**
     * Ejecuta ComandoTirar.
      * @param context valor de {@code context}
      * @param nombreObjeto valor de {@code nombreObjeto}
     */
    public ComandoTirar(CommandContext context, String nombreObjeto) {
        this.context = context;
        this.nombreObjeto = nombreObjeto;
    }

    @Override
    /**
     * Ejecuta ejecutar.
     */
    public void ejecutar() throws ComandoException {
        try {
            Objeto obj = context.getJuego().getJugador().tirar(nombreObjeto);
            Celda celda = context.getJuego().getMapa().getCelda(context.getJuego().getJugador().getPosicion());
            celda.agregarObjeto(obj);
            context.getJuego().getConsola().imprimir("Has tirado " + obj.getNombre() + ".");
        } catch (AccionInvalidaException e) {
            throw new ComandoException(e.getMessage());
        }
    }
}

