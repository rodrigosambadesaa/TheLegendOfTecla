package com.legendoftecla.effects;
import com.legendoftecla.model.characters.Personaje;
/** Veneno acumulable de dano constante por carga. */
public final class Envenenado implements EfectoEstado {
    public TipoEstado tipo() { return TipoEstado.ENVENENADO; }
    public int duracionInicial() { return 4; }
    public boolean acumulable() { return true; }
    public void alFinTurno(Personaje personaje, int acumulaciones) {
        personaje.recibirDanio(2 * acumulaciones);
    }
}
