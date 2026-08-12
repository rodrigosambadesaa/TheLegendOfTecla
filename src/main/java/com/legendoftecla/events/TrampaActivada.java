package com.legendoftecla.events;
import java.time.Instant;
/** Activacion de una trampa. */
public record TrampaActivada(Instant instante, String trampa,
        String victima) implements EventoJuego { }
