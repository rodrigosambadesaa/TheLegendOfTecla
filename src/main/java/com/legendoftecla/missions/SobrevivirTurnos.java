package com.legendoftecla.missions;
import com.legendoftecla.model.world.Juego;
/** Supervivencia durante una cantidad de turnos. */
public final class SobrevivirTurnos implements ObjetivoMision {
    private final int turnos;
    public SobrevivirTurnos(int turnos) {
        if (turnos < 1) throw new IllegalArgumentException("Turnos invalidos");
        this.turnos = turnos;
    }
    public boolean completado(Juego juego) { return juego.getPasos() >= turnos && !juego.jugadorMuerto(); }
    public String descripcion() { return "Sobrevivir " + turnos + " turnos"; }
    public int getTurnos() { return turnos; }
}
