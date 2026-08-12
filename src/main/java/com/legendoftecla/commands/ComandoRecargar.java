package com.legendoftecla.commands;

import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.events.ArmaRecargada;
import com.legendoftecla.inventory.ResultadoRecarga;
import com.legendoftecla.inventory.ServicioRecarga;

/** Recarga un arma equipada usando paquetes compatibles de la mochila. */
public final class ComandoRecargar implements Comando {
    private final CommandContext contexto;
    private final String nombreArma;
    public ComandoRecargar(CommandContext contexto, String nombreArma) {
        this.contexto = contexto; this.nombreArma = nombreArma;
    }
    @Override public void ejecutar() throws ComandoException {
        try {
            ResultadoRecarga resultado = new ServicioRecarga().recargar(
                    contexto.getJuego().getJugador(), nombreArma);
            contexto.getJuego().publicarEvento(new ArmaRecargada(
                    contexto.getJuego().getBusEventos().ahora(),
                    contexto.getJuego().getJugador().getNombre(),
                    resultado.arma().getNombre(), resultado.cantidad(),
                    contexto.getJuego().getJugador().getPosicion()));
            contexto.getJuego().getConsola().imprimirExito(
                    "Recargadas " + resultado.cantidad() + " unidades. "
                            + resultado.arma().estadoArma());
        } catch (AccionInvalidaException error) {
            throw new ComandoException(error.getMessage());
        }
    }
}
