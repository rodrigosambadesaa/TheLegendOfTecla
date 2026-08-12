package com.legendoftecla.commands;
import com.legendoftecla.exceptions.ComandoException;
/** Muestra cargadores de todas las armas equipadas. */
public final class ComandoEstadoArma implements Comando {
    private final CommandContext contexto;
    public ComandoEstadoArma(CommandContext contexto) { this.contexto = contexto; }
    public void ejecutar() throws ComandoException {
        var armas = contexto.getJuego().getJugador().getArmasEquipadas();
        if (armas.isEmpty()) throw new ComandoException("No hay armas equipadas.");
        armas.forEach(arma -> contexto.getJuego().getConsola().imprimirEstado(arma.estadoArma()));
    }
}
