package com.legendoftecla.effects;
/** Reduce precision y favorece estados de huida en la IA. */
public final class Asustado implements EfectoEstado {
    public TipoEstado tipo() { return TipoEstado.ASUSTADO; }
    public int duracionInicial() { return 3; }
    public double multiplicadorPrecision() { return 0.75; }
}
