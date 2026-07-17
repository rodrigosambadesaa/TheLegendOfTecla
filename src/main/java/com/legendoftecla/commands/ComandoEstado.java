package com.legendoftecla.commands;

import com.legendoftecla.model.characters.Jugador;


/**
 * Representa la entidad ComandoEstado del juego.
 */
public final class ComandoEstado {
    private ComandoEstado() {
    }

    /**
     * Ejecuta imprimirEstado.
      * @param context valor de {@code context}
     */
    public static void imprimirEstado(CommandContext context) {
        Jugador jugador = context.getJuego().getJugador();
        context.getJuego().getConsola()
                .imprimirEstado(jugador.getNombre() + " [salud(" + jugador.getSalud() + ") energia("
                        + jugador.getEnergia()
                        + ")] pasos " + context.getJuego().getPasos() + "/" + context.getJuego().getPasosMaximos());
    }
}

