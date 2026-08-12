package com.legendoftecla.events;
import java.time.Instant;
/** Desactivacion de una trampa. */
public record TrampaDesactivada(Instant instante, String trampa,
        String personaje) implements EventoJuego { }
