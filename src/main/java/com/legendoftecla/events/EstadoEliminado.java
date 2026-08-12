package com.legendoftecla.events;
import java.time.Instant;
/** Eliminacion de un estado temporal. */
public record EstadoEliminado(Instant instante, String personaje,
        String estado) implements EventoJuego { }
