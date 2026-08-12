package com.legendoftecla.missions;
import com.legendoftecla.model.world.Juego;
/** Objetivo de alcanzar la posicion de salida. */
public final class AlcanzarSalida implements ObjetivoMision {
    public boolean completado(Juego juego) {
        return juego.getJugador().getPosicion().equals(juego.getMapa().getObjetivo());
    }
    public String descripcion() { return "Alcanzar la salida"; }
}
