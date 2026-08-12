package com.legendoftecla.events;
import java.time.Instant;
/** Cierre de puerta. */
public record PuertaCerrada(Instant instante, String puerta) implements EventoJuego { }
