package com.legendoftecla.events;
import java.time.Instant;
/** Apertura de puerta. */
public record PuertaAbierta(Instant instante, String puerta) implements EventoJuego { }
