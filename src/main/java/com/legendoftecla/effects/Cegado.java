package com.legendoftecla.effects;
/** Penaliza temporalmente la precision. */
public final class Cegado implements EfectoEstado {
    public TipoEstado tipo() { return TipoEstado.CEGADO; }
    public int duracionInicial() { return 2; }
    public double multiplicadorPrecision() { return 0.55; }
    public double multiplicadorVision() { return 0.55; }
}
