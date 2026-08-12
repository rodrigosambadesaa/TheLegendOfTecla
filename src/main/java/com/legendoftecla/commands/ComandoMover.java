package com.legendoftecla.commands;

import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.validation.Validaciones;
import com.legendoftecla.events.PersonajeMovido;
import com.legendoftecla.events.RuidoGenerado;


/**
 * Representa la entidad ComandoMover del juego.
 */
public class ComandoMover implements Comando {
    private CommandContext context;
    private Direccion direccion;

    /**
     * Ejecuta ComandoMover.
      * @param context valor de {@code context}
      * @param direccion valor de {@code direccion}
     */
    public ComandoMover(CommandContext context, Direccion direccion) {
        setContext(context);
        setDireccion(direccion);
    }

    /** @return contexto de ejecucion */
    public CommandContext getContext() { return context; }
    /** @param context contexto no nulo */
    public void setContext(CommandContext context) {
        this.context = Validaciones.noNulo(context, "Contexto");
    }
    /** @return direccion del movimiento */
    public Direccion getDireccion() { return direccion; }
    /** @param direccion direccion no nula */
    public void setDireccion(Direccion direccion) {
        this.direccion = Validaciones.noNulo(direccion, "Direccion");
    }

    @Override
    /**
     * Ejecuta ejecutar.
     */
    public void ejecutar() throws ComandoException {
        try {
            var origen = context.getJuego().getJugador().getPosicion();
            context.getJuego().getJugador().mover(direccion, context.getJuego());
            context.getJuego().registrarPaso();
            context.getJuego().getJugador().registrarPosicion();
            context.getJuego().getConsola().imprimir("Te mueves a " + direccion + ".");
            var destino = context.getJuego().getJugador().getPosicion();
            context.getJuego().publicarEvento(new PersonajeMovido(
                    context.getJuego().getBusEventos().ahora(),
                    context.getJuego().getJugador().getNombre(), origen, destino));
            context.getJuego().publicarEvento(new RuidoGenerado(
                    context.getJuego().getBusEventos().ahora(), destino, 1, "movimiento"));
        } catch (AccionInvalidaException e) {
            throw new ComandoException(e.getMessage());
        }
    }
}

