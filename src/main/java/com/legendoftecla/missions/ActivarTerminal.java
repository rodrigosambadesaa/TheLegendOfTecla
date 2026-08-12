package com.legendoftecla.missions;
import com.legendoftecla.model.elements.Terminal;
import com.legendoftecla.model.world.Juego;
/** Consulta un terminal de escenario sin acoplarlo al motor. */
public final class ActivarTerminal implements ObjetivoMision {
    private final Terminal terminal;
    public ActivarTerminal(Terminal terminal) {
        this.terminal = java.util.Objects.requireNonNull(terminal, "Terminal");
    }
    public boolean completado(Juego juego) { return terminal.isHackeado(); }
    public String descripcion() { return "Activar terminal " + terminal.getId(); }
    public Terminal getTerminal() { return terminal; }
}
