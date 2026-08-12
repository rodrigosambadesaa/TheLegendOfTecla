package com.legendoftecla.events;
import com.legendoftecla.model.world.Posicion;
import java.time.Instant;
/** Inspeccion presencial de una celda. */
public record CeldaInspeccionada(Instant instante, String personaje,
        Posicion posicion) implements EventoJuego { }
