package com.legendoftecla.commands;

import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;


/**
 * Representa la entidad ComandoTirar del juego.
 */
public class ComandoTirar implements Comando {
    private CommandContext context;
    private String nombreObjeto;

    /**
     * Ejecuta ComandoTirar.
      * @param context valor de {@code context}
      * @param nombreObjeto valor de {@code nombreObjeto}
     */
    public ComandoTirar(CommandContext context, String nombreObjeto) {
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

