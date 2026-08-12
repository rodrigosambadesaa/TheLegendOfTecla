package com.legendoftecla.events;
import com.legendoftecla.model.world.Posicion;
import java.time.Instant;
/** Movimiento completado. */
public record PersonajeMovido(Instant instante, String personaje,
        Posicion origen, Posicion destino) implements EventoJuego { }
