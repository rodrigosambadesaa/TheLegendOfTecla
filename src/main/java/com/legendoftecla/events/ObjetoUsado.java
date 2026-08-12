package com.legendoftecla.events;
import com.legendoftecla.model.world.Posicion;
import java.time.Instant;
/** Uso de un objeto. */
public record ObjetoUsado(Instant instante, String personaje,
        String objeto, Posicion posicion) implements EventoJuego { }
