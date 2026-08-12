package com.legendoftecla.events;
import java.time.Instant;
/** Cumplimiento de una mision. */
public record MisionCompletada(Instant instante, String mision) implements EventoJuego { }
