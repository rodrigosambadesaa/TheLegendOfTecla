package com.legendoftecla.events;
import java.time.Instant;
/** Descubrimiento de una trampa. */
public record TrampaDetectada(Instant instante, String trampa,
        String personaje) implements EventoJuego { }
