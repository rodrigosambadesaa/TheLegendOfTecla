package com.legendoftecla.events;

import com.legendoftecla.model.world.Posicion;
import java.time.Instant;
import java.util.Objects;

/** Instantanea inmutable de un sonido tactico perceptible por la IA. */
public record RuidoGenerado(Instant instante, Posicion origen,
        int intensidad, String causa) implements EventoJuego {

    /** Evita compartir coordenadas mutables con emisor, listeners o replays. */
    public RuidoGenerado {
        Objects.requireNonNull(instante, "Instante");
        Objects.requireNonNull(origen, "Origen");
        Objects.requireNonNull(causa, "Causa");
        origen = new Posicion(origen.getFila(), origen.getColumna());
    }

    @Override
    public Posicion origen() {
        return new Posicion(origen.getFila(), origen.getColumna());
    }
}
