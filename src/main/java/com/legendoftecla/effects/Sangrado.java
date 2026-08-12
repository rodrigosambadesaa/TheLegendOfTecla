package com.legendoftecla.effects;
import com.legendoftecla.model.characters.Personaje;
/** Dano provocado por desplazarse mientras existe una herida abierta. */
public final class Sangrado implements EfectoEstado {
    public TipoEstado tipo() { return TipoEstado.SANGRADO; }
    public int duracionInicial() { return 5; }
    public void alMover(Personaje personaje, int acumulaciones) {
        personaje.recibirDanio(3);
    }
}
