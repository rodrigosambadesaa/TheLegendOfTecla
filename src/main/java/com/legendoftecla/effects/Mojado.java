package com.legendoftecla.effects;
/** Proteccion temporal frente a nuevas quemaduras. */
public final class Mojado implements EfectoEstado {
    private final int turnos;
    public Mojado() { this(3); }
    public Mojado(int turnos) {
        if (turnos < 1) throw new IllegalArgumentException("Duracion invalida");
        this.turnos = turnos;
    }
    public TipoEstado tipo() { return TipoEstado.MOJADO; }
    public int duracionInicial() { return turnos; }
}
