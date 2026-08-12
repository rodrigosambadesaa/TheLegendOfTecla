package com.legendoftecla.commands;

import com.legendoftecla.events.PersonajeCurado;
import com.legendoftecla.model.characters.Jugador;
import com.legendoftecla.validation.Validaciones;

/** Permite al jugador consumir el turno para recuperar salud y energia. */
public final class ComandoDescansar implements Comando {
    private static final double PROPORCION_RECUPERADA = 0.10;
    private CommandContext context;

    /** @param context contexto de la partida */
    public ComandoDescansar(CommandContext context) {
        setContext(context);
    }

    /** @return contexto de ejecucion */
    public CommandContext getContext() {
        return context;
    }

    /** @param context contexto no nulo */
    public void setContext(CommandContext context) {
        this.context = Validaciones.noNulo(context, "Contexto");
    }

    @Override
    public void ejecutar() {
        Jugador jugador = context.getJuego().getJugador();
        int saludAnterior = jugador.getSalud();
        int energiaAnterior = jugador.getEnergia();
        jugador.recuperarSalud(Math.max(1,
                (int) Math.ceil(jugador.getSaludMaxima() * PROPORCION_RECUPERADA)));
        jugador.recuperarEnergia(Math.max(1,
                (int) Math.ceil(jugador.getEnergiaMaxima() * PROPORCION_RECUPERADA)));
        int saludRecuperada = jugador.getSalud() - saludAnterior;
        int energiaRecuperada = jugador.getEnergia() - energiaAnterior;
        if (saludRecuperada > 0) {
            context.getJuego().publicarEvento(new PersonajeCurado(
                    context.getJuego().getBusEventos().ahora(), jugador.getNombre(),
                    saludRecuperada, jugador.getPosicion()));
        }
        context.getJuego().getConsola().imprimir(
                "Descansas sin moverte: recuperas " + saludRecuperada
                        + " de salud y " + energiaRecuperada
                        + " de energia. Los enemigos aprovechan para acercarse.");
    }
}
