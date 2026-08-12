package com.legendoftecla.commands;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.elements.Interruptor;
import com.legendoftecla.model.elements.Terminal;
import com.legendoftecla.model.elements.Puerta;
import com.legendoftecla.model.elements.ElementoMapa;
import com.legendoftecla.events.RuidoGenerado;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Posicion;
/** Hackea terminales o activa interruptores cercanos. */
public final class ComandoTerminal implements Comando {
    private final CommandContext contexto; private final boolean terminal;
    public ComandoTerminal(CommandContext contexto, boolean terminal) {
        this.contexto = contexto; this.terminal = terminal;
    }
    public void ejecutar() throws ComandoException {
        Posicion origen = contexto.getJuego().getJugador().getPosicion();
        for (Posicion p : posiciones(origen)) {
            if (!contexto.getJuego().getMapa().estaDentro(p)) continue;
            for (var elemento : contexto.getJuego().getMapa().getCelda(p).getElementos()) {
                if (terminal && elemento instanceof Terminal t) {
                    if (!t.hackear(contexto.getJuego().getJugador().getVisionBase() + 3)) {
                        throw new ComandoException("El hackeo ha fallado.");
                    }
                    aplicarObjetivo(t.getObjetivoId());
                    ruido(p, "terminal");
                    contexto.getJuego().getConsola().imprimirExito(
                            "Terminal " + t.getId() + " hackeado.");
                    return;
                }
                if (!terminal && elemento instanceof Interruptor i) {
                    i.activar();
                    aplicarObjetivo(i.getObjetivoId());
                    ruido(p, "interruptor");
                    contexto.getJuego().getConsola().imprimirExito(
                            "Interruptor " + i.getId() + " activado.");
                    return;
                }
            }
        }
        throw new ComandoException(terminal ? "No hay terminal cercano." : "No hay interruptor cercano.");
    }
    private java.util.List<Posicion> posiciones(Posicion origen) {
        java.util.List<Posicion> resultado = new java.util.ArrayList<>(); resultado.add(origen);
        for (Direccion direccion : Direccion.values()) resultado.add(origen.mover(direccion));
        return resultado;
    }
    private void aplicarObjetivo(String objetivoId) {
        if (objetivoId == null || objetivoId.isBlank()) return;
        ElementoMapa objetivo = buscarPorId(objetivoId);
        if (objetivo instanceof Puerta puerta) puerta.desbloquearPorTerminal();
        if (objetivo instanceof Interruptor interruptor) interruptor.setActivo(true);
    }
    private ElementoMapa buscarPorId(String id) {
        for (int f = 0; f < contexto.getJuego().getMapa().getFilas(); f++) {
            for (int c = 0; c < contexto.getJuego().getMapa().getColumnas(); c++) {
                for (ElementoMapa elemento : contexto.getJuego().getMapa()
                        .getCelda(new Posicion(f, c)).getElementos()) {
                    if (elemento.getId().equals(id)) return elemento;
                }
            }
        }
        return null;
    }
    private void ruido(Posicion posicion, String causa) {
        contexto.getJuego().publicarEvento(new RuidoGenerado(
                contexto.getJuego().getBusEventos().ahora(), posicion, 2, causa));
    }
}
