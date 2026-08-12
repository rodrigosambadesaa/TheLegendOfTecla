package com.legendoftecla.events;
import com.legendoftecla.model.world.Posicion;
import java.time.Instant;
/** Muerte de un personaje. */
public record PersonajeMuerto(Instant instante, String personaje,
        Posicion posicion) implements EventoJuego { }
