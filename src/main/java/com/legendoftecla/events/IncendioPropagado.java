package com.legendoftecla.events;
import com.legendoftecla.model.world.Posicion;
import java.time.Instant;
/** Propagacion entre dos celdas. */
public record IncendioPropagado(Instant instante, Posicion origen,
        Posicion destino) implements EventoJuego { }
