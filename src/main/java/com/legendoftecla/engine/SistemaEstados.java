package com.legendoftecla.engine;

import com.legendoftecla.effects.EstadoActivo;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Coordina los efectos temporales de todos los personajes de una partida. */
public final class SistemaEstados {
    private SistemaEstados() { }

    /** Ejecuta los efectos de comienzo de turno en orden estable. */
    public static void iniciarTurno(Juego juego) {
        personajes(juego).forEach(personaje -> personaje.getEstados().inicioTurno());
    }

    /** Ejecuta y descuenta los efectos al terminar el turno. */
    public static void finalizarTurno(Juego juego) {
        personajes(juego).forEach(personaje -> personaje.getEstados().finTurno());
    }

    /** Moja a los personajes que ocupan una celda y elimina sus quemaduras. */
    public static void mojarEn(Juego juego, Posicion posicion, int turnos) {
        personajes(juego).stream().filter(personaje -> personaje.getPosicion().equals(posicion))
                .forEach(personaje -> personaje.getEstados().mojar(turnos));
    }

    /** @return resumen corto para consola y paneles Swing */
    public static String resumen(Personaje personaje) {
        List<EstadoActivo> activos = personaje.getEstados().getActivos();
        if (activos.isEmpty()) {
            return "ninguno";
        }
        return activos.stream().map(estado -> estado.tipo().name().toLowerCase()
                + "(" + estado.turnosRestantes() + ")")
                .collect(Collectors.joining(", "));
    }

    private static List<Personaje> personajes(Juego juego) {
        List<Personaje> personajes = new ArrayList<>();
        personajes.add(juego.getJugador());
        personajes.addAll(juego.getAliados());
        personajes.addAll(juego.getEnemigos());
        return personajes;
    }
}
