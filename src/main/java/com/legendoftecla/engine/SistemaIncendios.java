package com.legendoftecla.engine;

import com.legendoftecla.console.TipoMensaje;
import com.legendoftecla.events.IncendioExtinguido;
import com.legendoftecla.events.IncendioIniciado;
import com.legendoftecla.events.IncendioPropagado;
import com.legendoftecla.events.PersonajeDanado;
import com.legendoftecla.events.PersonajeMuerto;
import com.legendoftecla.events.RuidoGenerado;
import com.legendoftecla.effects.Quemado;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.model.world.TipoSuelo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Reglas de caida de antorchas, propagacion, daño y extincion. */
public final class SistemaIncendios {
    private static final double PROBABILIDAD_CAIDA = 0.35;
    private static final double PROBABILIDAD_IGNICION_MADERA = 0.65;
    private static final double PROBABILIDAD_PROPAGACION = 0.45;

    private SistemaIncendios() { }

    public static boolean intentarDerribarAntorcha(Juego juego, Posicion posicion, Random random) {
        Celda celda = juego.getMapa().getCelda(posicion);
        if (!celda.hasAntorchaMural() || random.nextDouble() >= PROBABILIDAD_CAIDA) return false;
        celda.setAntorchaMural(false);
        juego.getConsola().imprimirAdvertencia("Una antorcha mural cae en " + posicion + ".");
        if (celda.getTipoSuelo() == TipoSuelo.MADERA
                && random.nextDouble() < PROBABILIDAD_IGNICION_MADERA) {
            iniciar(juego, posicion, 3);
            return true;
        }
        return false;
    }

    public static void iniciar(Juego juego, Posicion posicion, int intensidad) {
        if (establecerFuego(juego, posicion, intensidad)) {
            juego.publicarEvento(new IncendioIniciado(juego.getBusEventos().ahora(), posicion));
            publicarRuidoFuego(juego, posicion);
        }
    }

    public static boolean apagar(Juego juego, Posicion posicion) {
        Celda celda = juego.getMapa().getCelda(posicion);
        if (!celda.estaArdiendo()) return false;
        celda.setNivelFuego(0);
        juego.getConsola().imprimirExito("Fuego apagado en " + posicion + ".");
        juego.publicarEvento(new IncendioExtinguido(juego.getBusEventos().ahora(), posicion));
        return true;
    }

    public static void avanzarTurno(Juego juego, Random random) {
        List<Posicion> ardiendo = new ArrayList<>();
        for (int f = 0; f < juego.getMapa().getFilas(); f++) {
            for (int c = 0; c < juego.getMapa().getColumnas(); c++) {
                Posicion posicion = new Posicion(f, c);
                if (juego.getMapa().getCelda(posicion).estaArdiendo()) ardiendo.add(posicion);
            }
        }
        // Los bandos de Juego son la fuente de verdad: las listas de Celda
        // pueden estar desactualizadas despues de cargas o movimientos.
        Map<Posicion, List<Personaje>> ocupantes = indexarCombatientes(juego);
        for (Posicion posicion : ardiendo) {
            Celda celda = juego.getMapa().getCelda(posicion);
            int danio = 4 + celda.getNivelFuego() * 3;
            for (Personaje personaje : ocupantes.getOrDefault(posicion, List.of())) {
                danar(juego, personaje, posicion, danio);
            }
            SistemaDestruccion.danar(juego, posicion,
                    Math.max(1, celda.getNivelFuego() * 3));
            for (Direccion direccion : Direccion.values()) {
                Posicion vecina = posicion.mover(direccion);
                if (!juego.getMapa().esTransitable(vecina)) continue;
                Celda destino = juego.getMapa().getCelda(vecina);
                if (!destino.estaArdiendo() && destino.getTipoSuelo() == TipoSuelo.MADERA
                        && random.nextDouble() < PROBABILIDAD_PROPAGACION) {
                    if (establecerFuego(juego, vecina,
                            Math.max(1, celda.getNivelFuego() - 1))) {
                        juego.publicarEvento(new IncendioPropagado(juego.getBusEventos().ahora(),
                                posicion, vecina));
                        publicarRuidoFuego(juego, vecina);
                    }
                }
            }
            celda.setNivelFuego(Math.max(0, celda.getNivelFuego() - 1));
            if (!celda.estaArdiendo() && celda.getTipoSuelo() == TipoSuelo.MADERA) {
                celda.setTipoSuelo(TipoSuelo.PIEDRA);
            }
        }
    }

    /** Construye una vez por turno el indice de combatientes actualmente activos. */
    private static Map<Posicion, List<Personaje>> indexarCombatientes(Juego juego) {
        Map<Posicion, List<Personaje>> ocupantes = new HashMap<>();
        agregarCombatiente(ocupantes, juego.getJugador());
        juego.getAliados().forEach(personaje -> agregarCombatiente(ocupantes, personaje));
        juego.getEnemigos().forEach(personaje -> agregarCombatiente(ocupantes, personaje));
        return ocupantes;
    }

    private static void agregarCombatiente(
            Map<Posicion, List<Personaje>> ocupantes, Personaje personaje) {
        Posicion posicion = personaje.getPosicion();
        Posicion clave = new Posicion(posicion.getFila(), posicion.getColumna());
        ocupantes.computeIfAbsent(clave, ignorada -> new ArrayList<>()).add(personaje);
    }

    private static void danar(Juego juego, Personaje personaje, Posicion posicion, int danio) {
        if (!personaje.getPosicion().equals(posicion) || personaje.getSalud() <= 0) return;
        int antes = personaje.getSalud();
        personaje.recibirDanio(danio);
        int quitada = antes - personaje.getSalud();
        juego.getConsola().imprimir("El fuego daña a " + personaje.getNombre() + ": quita " + quitada
                + " de vida; quedan " + personaje.getSalud() + "/" + personaje.getSaludMaxima() + ".",
                TipoMensaje.ERROR);
        if (quitada > 0) {
            juego.publicarEvento(new PersonajeDanado(juego.getBusEventos().ahora(),
                    personaje.getNombre(), quitada, posicion));
        }
        if (antes > 0 && personaje.getSalud() <= 0) {
            juego.publicarEvento(new PersonajeMuerto(juego.getBusEventos().ahora(),
                    personaje.getNombre(), posicion));
            juego.getConsola().imprimir(personaje.getNombre() + " muere por el incendio.", TipoMensaje.ERROR);
        } else if (personaje.getSalud() > 0) {
            personaje.getEstados().aplicar(new Quemado());
        }
    }

    private static boolean establecerFuego(Juego juego, Posicion posicion, int intensidad) {
        Celda celda = juego.getMapa().getCelda(posicion);
        int nuevaIntensidad = Math.max(0, Math.min(3, intensidad));
        if (celda.getNivelFuego() >= nuevaIntensidad) {
            return false;
        }
        celda.setNivelFuego(nuevaIntensidad);
        juego.getConsola().imprimir("INCENDIO iniciado en " + posicion + ".", TipoMensaje.ERROR);
        return true;
    }

    private static void publicarRuidoFuego(Juego juego, Posicion posicion) {
        juego.publicarEvento(new RuidoGenerado(juego.getBusEventos().ahora(),
                posicion, 5, "incendio"));
    }
}
