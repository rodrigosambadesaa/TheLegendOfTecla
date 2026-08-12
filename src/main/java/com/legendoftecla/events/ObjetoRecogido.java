package com.legendoftecla.events;
import com.legendoftecla.model.world.Posicion;
import java.time.Instant;
/** Objeto incorporado a una mochila. */
public record ObjetoRecogido(Instant instante, String personaje,
        String objeto, Posicion posicion) implements EventoJuego { }
