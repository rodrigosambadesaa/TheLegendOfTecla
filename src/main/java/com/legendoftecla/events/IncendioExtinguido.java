package com.legendoftecla.events;
import com.legendoftecla.model.world.Posicion;
import java.time.Instant;
/** Extincion de un incendio. */
public record IncendioExtinguido(Instant instante, Posicion posicion) implements EventoJuego { }
