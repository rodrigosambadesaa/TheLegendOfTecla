package com.legendoftecla.events;
import java.time.Instant;
/** Aplicacion de un estado temporal. */
public record EstadoAplicado(Instant instante, String personaje,
        String estado) implements EventoJuego { }
