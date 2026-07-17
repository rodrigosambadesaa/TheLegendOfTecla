package com.legendoftecla.commands;

import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.exceptions.ComandoException;


/**
 * Representa la entidad ComandoDesequipar del juego.
 */
public class ComandoDesequipar implements Comando {
    private final CommandContext context;
    private final String nombreObjeto;

    /**
     * Ejecuta ComandoDesequipar.
      * @param context valor de {@code context}
      * @param nombreObjeto valor de {@code nombreObjeto}
     */
    public ComandoDesequipar(CommandContext context, String nombreObjeto) {
        this.context = context;
        this.nombreObjeto = nombreObjeto;
    }

    @Override
    /**
     * Ejecuta ejecutar.
     */
    public void ejecutar() throws ComandoException {
        try {
            context.getJuego().getJugador().desequipar(nombreObjeto);
            context.getJuego().getConsola().imprimir("Desequipado: " + nombreObjeto);
        } catch (AccionInvalidaException e) {
            throw new ComandoException(e.getMessage());
        }
    }
}

