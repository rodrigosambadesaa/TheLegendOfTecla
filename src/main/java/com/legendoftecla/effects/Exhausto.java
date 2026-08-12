package com.legendoftecla.effects;
import com.legendoftecla.model.characters.Personaje;
/** Penalizacion energetica que desaparece al descansar. */
public final class Exhausto implements EfectoEstado {
    public TipoEstado tipo() { return TipoEstado.EXHAUSTO; }
    public int duracionInicial() { return 6; }
    public void alAplicar(Personaje personaje) {
        personaje.setPenalizacionEnergiaSiguienteTurno(0.5);
    }
    public void alEliminar(Personaje personaje) {
        personaje.setPenalizacionEnergiaSiguienteTurno(0.0);
    }
}
