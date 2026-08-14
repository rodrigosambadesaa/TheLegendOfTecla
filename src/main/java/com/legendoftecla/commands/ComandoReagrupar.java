package com.legendoftecla.commands;

import com.legendoftecla.constants.FormacionAliada;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.validation.Validaciones;

/** Ordena a los aliados acompañar al jugador en una formación táctica. */
public final class ComandoReagrupar implements Comando {
    private final CommandContext context;
    private final FormacionAliada formacion;

    /**
     * @param context contexto de la partida
     * @param formacion estrategia solicitada
     */
    public ComandoReagrupar(CommandContext context, FormacionAliada formacion) {
        this.context = Validaciones.noNulo(context, "Contexto");
        this.formacion = Validaciones.noNulo(formacion, "Formacion");
    }

    /** @return contexto de ejecución */
    public CommandContext getContext() {
        return context;
    }

    /** @return formación seleccionada */
    public FormacionAliada getFormacion() {
        return formacion;
    }

    @Override
    public void ejecutar() throws ComandoException {
        boolean hayAliadosVivos = context.getJuego().getAliados().stream()
                .anyMatch(aliado -> aliado.getSalud() > 0);
        if (!hayAliadosVivos) {
            throw new ComandoException("No hay aliados disponibles para reagruparse.");
        }
        context.getJuego().setFormacionAliada(formacion);
        if (formacion == FormacionAliada.SIN_FORMACION) {
            context.getJuego().getConsola().imprimirInfo(
                    "Formacion rota: los aliados recuperan su comportamiento autonomo.");
        } else {
            context.getJuego().getConsola().imprimirInfo("Formacion " + formacion.getEtiqueta()
                    + ": los aliados acompañaran al jugador y adaptaran sus prioridades.");
        }
    }
}
