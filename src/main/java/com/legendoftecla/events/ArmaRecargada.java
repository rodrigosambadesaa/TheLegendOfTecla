package com.legendoftecla.events;

import com.legendoftecla.model.world.Posicion;
import java.time.Instant;

/** Municion finita transferida desde una mochila a un cargador. */
public record ArmaRecargada(Instant instante, String personaje,
        String arma, int cantidad, Posicion posicion) implements EventoJuego { }
