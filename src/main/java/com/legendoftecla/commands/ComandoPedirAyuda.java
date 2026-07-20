package com.legendoftecla.commands;

import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.validation.Validaciones;

/** Solicita apoyo, suministros y cobertura a los aliados vivos. */
public final class ComandoPedirAyuda implements Comando {
    private CommandContext context;

    /**
     * Crea la orden de ayuda aliada.
     *
     * @param context contexto de la partida
     */
    public ComandoPedirAyuda(CommandContext context) {
        setContext(context);
    }

    /** @return contexto de ejecucion */
    public CommandContext getContext() { return context; }
    /** @param context contexto no nulo */
    public void setContext(CommandContext context) {
        this.context = Validaciones.noNulo(context, "Contexto");
    }

    @Override
    public void ejecutar() throws ComandoException {
        boolean hayAliadosVivos = context.getJuego().getAliados().stream()
                .anyMatch(aliado -> aliado.getSalud() > 0);
        if (!hayAliadosVivos) {
            throw new ComandoException("No hay aliados disponibles para responder.");
        }
        context.getJuego().solicitarAyudaAliados();
        context.getJuego().getConsola().imprimirInfo(
                "Pides ayuda: los aliados seguros acudiran y priorizaran tus necesidades.");
    }
}
