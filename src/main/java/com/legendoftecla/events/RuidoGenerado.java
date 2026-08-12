package com.legendoftecla.events;
import com.legendoftecla.model.world.Posicion;
import java.time.Instant;
/** Sonido tactico perceptible por la IA. */
public record RuidoGenerado(Instant instante, Posicion origen,
        int intensidad, String causa) implements EventoJuego { }
