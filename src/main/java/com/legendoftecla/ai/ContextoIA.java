package com.legendoftecla.ai;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
/** Instantanea de percepcion y evaluacion tactica. */
public record ContextoIA(Enemigo enemigo, Juego juego, boolean veJugador,
        Posicion ultimoRuido, boolean aliadoHerido, boolean armaVacia,
        Personaje objetivo, boolean coordinacionActiva) {
    public ContextoIA {
        java.util.Objects.requireNonNull(enemigo, "Enemigo");
        java.util.Objects.requireNonNull(juego, "Juego");
        java.util.Objects.requireNonNull(objetivo, "Objetivo");
    }
    /** Constructor compatible que infiere objetivo y modo de escuadron. */
    public ContextoIA(Enemigo enemigo, Juego juego, boolean veJugador,
            Posicion ultimoRuido, boolean aliadoHerido, boolean armaVacia) {
        this(enemigo, juego, veJugador, ultimoRuido, aliadoHerido, armaVacia,
                juego.getJugador(), juego.getAliados().stream()
                        .anyMatch(aliado -> aliado.getSalud() > 0));
    }
    public int distanciaJugador() {
        return enemigo.getPosicion().distanciaManhattan(objetivo.getPosicion());
    }
    /** @return posicion del miembro del escuadron priorizado */
    public Posicion posicionObjetivo() { return objetivo.getPosicion(); }
}
