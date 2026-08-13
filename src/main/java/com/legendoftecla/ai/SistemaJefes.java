package com.legendoftecla.ai;

import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.effects.Inspirado;
import com.legendoftecla.engine.ArsenalEnemigo;
import com.legendoftecla.engine.SistemaIncendios;
import com.legendoftecla.model.characters.CommanderPrime;
import com.legendoftecla.model.characters.FaseJefe;
import com.legendoftecla.model.characters.Jefe;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.PyroOverlord;
import com.legendoftecla.model.characters.Scout;
import com.legendoftecla.model.elements.Barricada;
import com.legendoftecla.model.elements.OrientacionCobertura;
import com.legendoftecla.model.elements.TipoCobertura;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;

import java.util.Random;

/** Habilidades de fase con efectos reales y ejecucion unica por transicion. */
public final class SistemaJefes {
    private SistemaJefes() { }

    /** @return si la habilidad de la fase actual modifico partida o entorno */
    public static boolean activarFase(Juego juego, Jefe jefe, Random random) {
        if (jefe instanceof CommanderPrime
                && juego.getAliados().stream().noneMatch(aliado -> aliado.getSalud() > 0)) {
            return false;
        }
        if (!jefe.consumirHabilidadDeFase()) {
            return false;
        }
        if (jefe instanceof CommanderPrime prime) {
            return activarCommander(juego, prime, random);
        }
        if (jefe instanceof PyroOverlord pyro) {
            return activarPyro(juego, pyro);
        }
        return false;
    }

    private static boolean activarCommander(Juego juego, CommanderPrime jefe, Random random) {
        return switch (jefe.getFase()) {
            case UNO -> inspirarCercanos(juego, jefe);
            case DOS -> invocarScout(juego, jefe, random);
            case TRES -> fortificar(juego, jefe);
            case FINAL -> {
                jefe.getEstados().aplicar(new Inspirado());
                yield true;
            }
        };
    }

    private static boolean inspirarCercanos(Juego juego, Jefe jefe) {
        boolean aplicado = false;
        for (var enemigo : juego.getEnemigos()) {
            if (enemigo != jefe && enemigo.getSalud() > 0
                    && jefe.getPosicion().distanciaManhattan(enemigo.getPosicion()) <= 4) {
                enemigo.getEstados().aplicar(new Inspirado());
                aplicado = true;
            }
        }
        return aplicado;
    }

    private static boolean invocarScout(Juego juego, Jefe jefe, Random random) {
        java.util.List<Posicion> libres = java.util.Arrays.stream(Direccion.values())
                .map(jefe.getPosicion()::mover)
                .filter(juego.getMapa()::esTransitable)
                .filter(posicion -> juego.getEnemigos().stream()
                        .noneMatch(enemigo -> enemigo.getPosicion().equals(posicion)))
                .filter(posicion -> !posicion.equals(juego.getJugador().getPosicion()))
                .toList();
        if (libres.isEmpty()) {
            return false;
        }
        Posicion posicion = libres.get(random.nextInt(libres.size()));
        Scout refuerzo = new Scout(jefe.getNombre() + "-refuerzo-"
                + juego.getPasos(), posicion, new Mochila(3, 10), 5);
        ArsenalEnemigo.asignar(refuerzo, Dificultad.NORMAL);
        juego.agregarEnemigo(refuerzo);
        juego.getMapa().getCelda(posicion).agregarEnemigo(refuerzo);
        return true;
    }

    private static boolean fortificar(Juego juego, Jefe jefe) {
        for (Direccion direccion : Direccion.values()) {
            Posicion posicion = jefe.getPosicion().mover(direccion);
            if (juego.getMapa().esTransitable(posicion)
                    && juego.getMapa().getCelda(posicion).getElementos().isEmpty()) {
                juego.getMapa().getCelda(posicion).agregarElemento(new Barricada(
                        "fortificacion-" + jefe.getNombre(), 30,
                        TipoCobertura.COMPLETA, OrientacionCobertura.TODAS));
                return true;
            }
        }
        return false;
    }

    private static boolean activarPyro(Juego juego, PyroOverlord jefe) {
        int radio = switch (jefe.getFase()) {
            case UNO -> 0;
            case DOS -> 1;
            case TRES -> 2;
            case FINAL -> 3;
        };
        Posicion centro = juego.getJugador().getPosicion();
        boolean iniciado = false;
        for (int df = -radio; df <= radio; df++) {
            for (int dc = -radio; dc <= radio; dc++) {
                Posicion posicion = new Posicion(
                        centro.getFila() + df, centro.getColumna() + dc);
                if (juego.getMapa().estaDentro(posicion)
                        && centro.distanciaManhattan(posicion) <= radio) {
                    int antes = juego.getMapa().getCelda(posicion).getNivelFuego();
                    SistemaIncendios.iniciar(juego, posicion, Math.min(3, radio + 1));
                    iniciado |= juego.getMapa().getCelda(posicion).getNivelFuego() > antes;
                }
            }
        }
        return iniciado;
    }
}
