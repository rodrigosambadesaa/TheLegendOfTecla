package com.legendoftecla.missions;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
/** Extincion de una celda concreta. */
public final class ApagarIncendio implements ObjetivoMision {
    private final Posicion posicion;
    public ApagarIncendio(Posicion posicion) { this.posicion = posicion; }
    public boolean completado(Juego juego) {
        return !juego.getMapa().getCelda(posicion).estaArdiendo();
    }
    public String descripcion() { return "Apagar el incendio en " + posicion; }
    public Posicion getPosicion() { return posicion; }
}
