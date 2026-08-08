package com.legendoftecla.commands;

import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.exceptions.JuegoException;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;


/**
 * Representa la entidad ComandoUsar del juego.
 */
public class ComandoUsar implements Comando {
    private CommandContext context;
    private String nombreObjeto;

    /**
     * Ejecuta ComandoUsar.
      * @param context valor de {@code context}
      * @param nombreObjeto valor de {@code nombreObjeto}
     */
    public ComandoUsar(CommandContext context, String nombreObjeto) {
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
        Objeto obj = context.getJuego().getJugador().getMochila().quitarPorNombre(nombreObjeto);
        if (obj == null) {
            throw new ComandoException("No tienes ese objeto en la mochila.");
        }
        try {
            obj.usar(context.getJuego().getJugador());
            if (!obj.isConsumible()) {
                context.getJuego().getJugador().getMochila().guardar(obj);
            }
            context.getJuego().getConsola().imprimir("Usas " + obj.getNombre() + ".");
        } catch (JuegoException e) {
            context.getJuego().getJugador().getMochila().guardar(obj);
            throw new ComandoException(e.getMessage());
        }
    }
}

