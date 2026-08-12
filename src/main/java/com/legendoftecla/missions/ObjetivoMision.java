package com.legendoftecla.missions;
import com.legendoftecla.model.world.Juego;
/** Condicion extensible de una mision. */
public interface ObjetivoMision {
    boolean completado(Juego juego);
    String descripcion();
}
