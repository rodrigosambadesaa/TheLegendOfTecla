package com.legendoftecla.commands;

import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;


/**
 * Representa la entidad ComandoCoger del juego.
 */
public class ComandoCoger implements Comando {
    private CommandContext context;
    private String nombreObjeto;

    /**
     * Ejecuta ComandoCoger.
      * @param context valor de {@code context}
      * @param nombreObjeto valor de {@code nombreObjeto}
     */
    public ComandoCoger(CommandContext context, String nombreObjeto) {
        setContext(context);
        setNombreObjeto(nombreObjeto);
    }

    /** @return contexto de ejecucion */
    public CommandContext getContext() { return context; }
    /** @param context contexto no nulo */
    public void setContext(CommandContext context) { this.context = Validaciones.noNulo(context, "Contexto"); }
    /** @return nombre del objeto */
    public String getNombreObjeto() { return nombreObjeto; }
    /** @param nombreObjeto nombre obligatorio y acotado */
    public void setNombreObjeto(String nombreObjeto) {
        this.nombreObjeto = Validaciones.textoObligatorio(
                nombreObjeto, "Nombre del objeto", Limites.TEXTO_CORTO);
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

