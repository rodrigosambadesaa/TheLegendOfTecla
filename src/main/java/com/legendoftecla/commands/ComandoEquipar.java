package com.legendoftecla.commands;

import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;


/**
 * Representa la entidad ComandoEquipar del juego.
 */
public class ComandoEquipar implements Comando {
    private CommandContext context;
    private String nombreObjeto;

    /**
     * Ejecuta ComandoEquipar.
      * @param context valor de {@code context}
      * @param nombreObjeto valor de {@code nombreObjeto}
     */
    public ComandoEquipar(CommandContext context, String nombreObjeto) {
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

