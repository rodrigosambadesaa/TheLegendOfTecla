package com.legendoftecla.commands;

import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;
import com.legendoftecla.audio.EventoSonido;
import com.legendoftecla.audio.GestorSonido;


/**
 * Representa la entidad ComandoDesequipar del juego.
 */
public class ComandoDesequipar implements Comando {
    private CommandContext context;
    private String nombreObjeto;

    /**
     * Ejecuta ComandoDesequipar.
      * @param context valor de {@code context}
      * @param nombreObjeto valor de {@code nombreObjeto}
     */
    public ComandoDesequipar(CommandContext context, String nombreObjeto) {
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
            context.getJuego().getJugador().desequipar(nombreObjeto);
            context.getJuego().getConsola().imprimir("Desequipado: " + nombreObjeto);
            GestorSonido.reproducir(EventoSonido.DESEQUIPAR);
        } catch (AccionInvalidaException e) {
            throw new ComandoException(e.getMessage());
        }
    }
}

