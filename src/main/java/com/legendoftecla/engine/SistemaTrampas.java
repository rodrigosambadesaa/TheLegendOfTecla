package com.legendoftecla.engine;

import com.legendoftecla.events.PersonajeDanado;
import com.legendoftecla.events.PersonajeMuerto;
import com.legendoftecla.events.RuidoGenerado;
import com.legendoftecla.events.TrampaActivada;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.elements.Alarma;
import com.legendoftecla.model.elements.Trampa;
import com.legendoftecla.model.elements.TrampaFuego;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;

import java.util.ArrayList;
import java.util.List;

/** Resuelve activaciones, radios y observabilidad de trampas sobre cualquier bando. */
public final class SistemaTrampas {
    private SistemaTrampas() { }

    /** Activa las trampas no remotas al entrar en una celda. */
    public static void activarAlEntrar(Juego juego, Posicion posicion, Personaje activador) {
        trampasEn(juego, posicion).forEach(trampa -> resolver(
                juego, posicion, trampa, activador, Operacion.ACTIVAR));
    }

    /** Detona una trampa remota y aplica su radio real. */
    public static boolean detonar(Juego juego, Posicion posicion, Trampa trampa) {
        return resolver(juego, posicion, trampa, juego.getJugador(), Operacion.DETONAR);
    }

    /** Dispara una trampa conocida, sea remota o de contacto. */
    public static boolean disparar(Juego juego, Posicion posicion, Trampa trampa) {
        return resolver(juego, posicion, trampa, juego.getJugador(), Operacion.DISPARAR);
    }

    /** @return trampas de una celda en su orden de escenario */
    public static List<Trampa> trampasEn(Juego juego, Posicion posicion) {
        if (!juego.getMapa().estaDentro(posicion)) {
            return List.of();
        }
        return juego.getMapa().getCelda(posicion).getElementos().stream()
                .filter(Trampa.class::isInstance).map(Trampa.class::cast).toList();
    }

    private static boolean resolver(Juego juego, Posicion posicion, Trampa trampa,
            Personaje activador, Operacion operacion) {
        List<Personaje> victimas = personajesEnRadio(juego, posicion, trampa.getRadio());
        List<Integer> vidas = victimas.stream().map(Personaje::getSalud).toList();
        boolean activada = switch (operacion) {
            case ACTIVAR -> trampa.activar(victimas);
            case DETONAR -> trampa.detonar(victimas);
            case DISPARAR -> trampa.disparar(victimas);
        };
        if (!activada) {
            return false;
        }
        for (int indice = 0; indice < victimas.size(); indice++) {
            Personaje victima = victimas.get(indice);
            int danio = vidas.get(indice) - victima.getSalud();
            juego.publicarEvento(new TrampaActivada(juego.getBusEventos().ahora(),
                    trampa.getId(), victima.getNombre()));
            if (danio > 0) {
                juego.publicarEvento(new PersonajeDanado(juego.getBusEventos().ahora(),
                        victima.getNombre(), danio, victima.getPosicion()));
            }
            if (vidas.get(indice) > 0 && victima.getSalud() <= 0) {
                juego.publicarEvento(new PersonajeMuerto(juego.getBusEventos().ahora(),
                        victima.getNombre(), victima.getPosicion()));
            }
        }
        if (trampa instanceof TrampaFuego) {
            SistemaIncendios.iniciar(juego, posicion, 2);
        }
        int intensidad = trampa instanceof Alarma ? 10 : Math.max(4, trampa.getDano());
        juego.publicarEvento(new RuidoGenerado(juego.getBusEventos().ahora(),
                posicion, Math.min(10, intensidad), "trampa:" + trampa.getId()));
        if (victimas.isEmpty()) {
            juego.publicarEvento(new TrampaActivada(juego.getBusEventos().ahora(),
                    trampa.getId(), activador.getNombre()));
        }
        return true;
    }

    private static List<Personaje> personajesEnRadio(Juego juego, Posicion centro, int radio) {
        List<Personaje> personajes = new ArrayList<>();
        personajes.add(juego.getJugador());
        personajes.addAll(juego.getAliados());
        personajes.addAll(juego.getEnemigos());
        return personajes.stream().filter(personaje -> personaje.getSalud() > 0)
                .filter(personaje -> personaje.getPosicion().distanciaManhattan(centro) <= radio)
                .toList();
    }

    private enum Operacion { ACTIVAR, DETONAR, DISPARAR }
}
