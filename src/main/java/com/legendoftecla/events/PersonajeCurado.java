package com.legendoftecla.events;
import com.legendoftecla.model.world.Posicion;
import java.time.Instant;
/** Curacion efectiva. */
public record PersonajeCurado(Instant instante, String personaje,
        int cantidad, Posicion posicion) implements EventoJuego { }
