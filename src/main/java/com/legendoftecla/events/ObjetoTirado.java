package com.legendoftecla.events;
import com.legendoftecla.model.world.Posicion;
import java.time.Instant;
/** Objeto retirado de una mochila. */
public record ObjetoTirado(Instant instante, String personaje,
        String objeto, Posicion posicion) implements EventoJuego { }
