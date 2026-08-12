package com.legendoftecla.ai;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
/** Instantanea de percepcion y evaluacion tactica. */
public record ContextoIA(Enemigo enemigo, Juego juego, boolean veJugador,
        Posicion ultimoRuido, boolean aliadoHerido, boolean armaVacia) {
    public ContextoIA {
        java.util.Objects.requireNonNull(enemigo, "Enemigo");
        java.util.Objects.requireNonNull(juego, "Juego");
    }
    public int distanciaJugador() {
        return enemigo.getPosicion().distanciaManhattan(juego.getJugador().getPosicion());
    }
}
