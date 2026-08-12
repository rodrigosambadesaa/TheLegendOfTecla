package com.legendoftecla.effects;
/** Hace perder la siguiente accion. */
public final class Aturdido implements EfectoEstado {
    public TipoEstado tipo() { return TipoEstado.ATURDIDO; }
    public int duracionInicial() { return 1; }
    public boolean renuevaDuracion() { return false; }
    public boolean bloqueaAccion() { return true; }
}
