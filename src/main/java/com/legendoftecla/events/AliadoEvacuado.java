package com.legendoftecla.events;
import com.legendoftecla.model.world.Posicion;
import java.time.Instant;
/** Evacuacion con vida de un aliado. */
public record AliadoEvacuado(Instant instante, String aliado,
        Posicion posicion) implements EventoJuego { }
