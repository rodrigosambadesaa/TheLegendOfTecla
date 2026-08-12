package com.legendoftecla.events;
import com.legendoftecla.model.world.Posicion;
import java.time.Instant;
/** Dano efectivo recibido. */
public record PersonajeDanado(Instant instante, String personaje,
        int cantidad, Posicion posicion) implements EventoJuego { }
