package com.legendoftecla.events;
import com.legendoftecla.model.world.Posicion;
import java.time.Instant;
/** Intento de ataque. */
public record PersonajeAtacado(Instant instante, String atacante,
        String objetivo, Posicion origen, Posicion destino) implements EventoJuego { }
