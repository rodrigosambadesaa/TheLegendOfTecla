package com.legendoftecla.effects;
/** Bonificacion temporal a la precision. */
public final class Inspirado implements EfectoEstado {
    public TipoEstado tipo() { return TipoEstado.INSPIRADO; }
    public int duracionInicial() { return 3; }
    public double multiplicadorPrecision() { return 1.2; }
}
