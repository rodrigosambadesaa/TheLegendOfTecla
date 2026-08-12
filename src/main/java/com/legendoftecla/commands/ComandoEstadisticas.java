package com.legendoftecla.commands;

import com.legendoftecla.stats.EstadisticasPartida;

/** Muestra estadisticas y logros de la partida actual en consola o GUI. */
public final class ComandoEstadisticas implements Comando {
    private final CommandContext contexto;

    public ComandoEstadisticas(CommandContext contexto) {
        this.contexto = java.util.Objects.requireNonNull(contexto, "Contexto");
    }

    @Override
    public void ejecutar() {
        EstadisticasPartida e = contexto.getJuego().getEstadisticas();
        String logros = contexto.getJuego().getLogros().getDesbloqueados().isEmpty()
                ? "ninguno"
                : String.join(", ", contexto.getJuego().getLogros().getDesbloqueados());
        contexto.getJuego().getConsola().imprimirEstado(e.resumen()
                + ", recibidos: " + e.getDanoRecibido()
                + ", muertes: " + e.getMuertes()
                + ", incendios: " + e.getIncendiosApagados()
                + ", objetos: " + e.getObjetosUsados()
                + ", trampas: " + e.getTrampasDesactivadas()
                + " | Logros: " + logros);
    }
}
