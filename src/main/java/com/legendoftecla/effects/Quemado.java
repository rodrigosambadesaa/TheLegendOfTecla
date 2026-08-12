package com.legendoftecla.effects;
import com.legendoftecla.model.characters.Personaje;
/** Dano por fuego al inicio de cada turno. */
public final class Quemado implements EfectoEstado {
    private final int turnos;
    private final int dano;
    public Quemado() { this(3, 4); }
    public Quemado(int turnos, int dano) {
        if (turnos < 1 || dano < 1) throw new IllegalArgumentException("Fuego invalido");
        this.turnos = turnos; this.dano = dano;
    }
    public TipoEstado tipo() { return TipoEstado.QUEMADO; }
    public int duracionInicial() { return turnos; }
    public boolean acumulable() { return true; }
    public void alInicioTurno(Personaje personaje, int acumulaciones) {
        personaje.recibirDanio(dano * acumulaciones);
    }
}
