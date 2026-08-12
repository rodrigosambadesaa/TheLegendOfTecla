package com.legendoftecla.events;
import com.legendoftecla.model.world.Posicion;
import java.time.Instant;
/** Inicio de un incendio. */
public record IncendioIniciado(Instant instante, Posicion posicion) implements EventoJuego { }
