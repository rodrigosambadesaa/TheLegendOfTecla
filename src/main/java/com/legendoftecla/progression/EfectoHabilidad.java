package com.legendoftecla.progression;
import com.legendoftecla.model.characters.Jugador;
/** Efecto aplicable al desbloquear una habilidad. */
@FunctionalInterface
public interface EfectoHabilidad { void aplicar(Jugador jugador); }
