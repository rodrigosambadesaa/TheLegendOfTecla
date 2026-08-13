package com.legendoftecla.loader;

import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.engine.ArsenalEnemigo;
import com.legendoftecla.model.characters.Berserker;
import com.legendoftecla.model.characters.Commander;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.HeavyFloater;
import com.legendoftecla.model.characters.LightFloater;
import com.legendoftecla.model.characters.Medic;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Pyro;
import com.legendoftecla.model.characters.Scout;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.characters.Sniper;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Escala y distribuye una fuerza enemiga proporcional al escuadron aliado. */
final class DistribucionEnemigaEscuadron {
    private DistribucionEnemigaEscuadron() {
    }

    static ResultadoEquilibrio equilibrar(Juego juego, Random random,
            int cantidadAliados, Dificultad dificultad) {
        int originales = juego.getEnemigos().size();
        if (cantidadAliados <= 0) {
            return new ResultadoEquilibrio(originales, originales, 0, originales);
        }
        int objetivo = Math.max(originales,
                dificultad.ajustarCantidadEnemigos(cantidadAliados));
        int agregados = agregarRefuerzos(
                juego, random, dificultad, objetivo - originales);
        endurecer(juego, random, cantidadAliados);
        return new ResultadoEquilibrio(
                originales, objetivo, agregados, juego.getEnemigos().size());
    }

    private static int agregarRefuerzos(Juego juego, Random random,
            Dificultad dificultad, int cantidad) {
        if (cantidad <= 0) return 0;
        List<Posicion> posiciones = posicionesDeDespliegue(juego.getMapa());
        if (posiciones.isEmpty()) return 0;
        int baseNombre = juego.getEnemigos().size();
        for (int indice = 0; indice < cantidad; indice++) {
            Posicion posicion = posicionMenosOcupada(juego, posiciones, random);
            Enemigo enemigo = crearRefuerzo(
                    baseNombre + indice, posicion, random.nextInt(9));
            enemigo.escalarSalud(dificultad.getMultiplicadorSaludEnemigo());
            ArsenalEnemigo.asignar(enemigo, dificultad);
            juego.agregarEnemigo(enemigo);
            juego.getMapa().getCelda(posicion).agregarEnemigo(enemigo);
        }
        return cantidad;
    }

    private static List<Posicion> posicionesDeDespliegue(Mapa mapa) {
        Map<Posicion, Integer> distancias = calcularDistancias(mapa);
        int radioSeguro = Math.min(3, Math.max(1,
                distancias.values().stream().mapToInt(Integer::intValue).max().orElse(0) / 8));
        List<Posicion> posiciones = distancias.entrySet().stream()
                .filter(entrada -> entrada.getValue() >= radioSeguro)
                .map(Map.Entry::getKey)
                .filter(posicion -> !posicion.equals(mapa.getInicio())
                        && !posicion.equals(mapa.getObjetivo()))
                .toList();
        if (!posiciones.isEmpty()) return posiciones;
        return distancias.keySet().stream()
                .filter(posicion -> !posicion.equals(mapa.getInicio())
                        && !posicion.equals(mapa.getObjetivo()))
                .toList();
    }

    private static Posicion posicionMenosOcupada(Juego juego,
            List<Posicion> posiciones, Random random) {
        int minimo = posiciones.stream().mapToInt(posicion ->
                juego.getMapa().getCelda(posicion).getEnemigos().size()).min().orElse(0);
        List<Posicion> candidatas = posiciones.stream().filter(posicion ->
                juego.getMapa().getCelda(posicion).getEnemigos().size() == minimo).toList();
        return candidatas.get(random.nextInt(candidatas.size()));
    }

    private static Enemigo crearRefuerzo(int indice, Posicion posicion, int tipo) {
        Mochila mochila = new Mochila(5, 30);
        String nombre = "Refuerzo_" + indice;
        return switch (tipo) {
            case 0 -> new Sectoid("Sectoid_" + nombre, posicion, mochila, 4);
            case 1 -> new LightFloater("LightFloater_" + nombre, posicion, mochila, 4);
            case 2 -> new HeavyFloater("HeavyFloater_" + nombre, posicion, mochila, 4);
            case 3 -> new Berserker("Berserker_" + nombre, posicion, mochila, 4);
            case 4 -> new Medic("Medic_" + nombre, posicion, mochila, 5);
            case 5 -> new Sniper("Sniper_" + nombre, posicion, mochila, 7);
            case 6 -> new Pyro("Pyro_" + nombre, posicion, mochila, 5);
            case 7 -> new Scout("Scout_" + nombre, posicion, mochila, 7);
            default -> new Commander("Commander_" + nombre, posicion, mochila, 6);
        };
    }

    private static void endurecer(Juego juego, Random random, int cantidadAliados) {
        if (juego.getEnemigos().isEmpty()) {
            return;
        }
        Mapa mapa = juego.getMapa();
        Map<Posicion, Integer> distancias = calcularDistancias(mapa);
        int distanciaMaxima = distancias.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        int radioSeguro = Math.min(3, Math.max(1, distanciaMaxima / 8));
        int limitePresion = Math.max(radioSeguro + 2, distanciaMaxima / 2);

        List<Posicion> destinos = new ArrayList<>();
        for (Map.Entry<Posicion, Integer> entrada : distancias.entrySet()) {
            Posicion posicion = entrada.getKey();
            int distancia = entrada.getValue();
            if (distancia >= radioSeguro && distancia <= limitePresion
                    && !posicion.equals(mapa.getInicio())
                    && !posicion.equals(mapa.getObjetivo())
                    && mapa.getCelda(posicion).getAliados().isEmpty()
                    && mapa.getCelda(posicion).getEnemigos().isEmpty()) {
                destinos.add(posicion);
            }
        }
        java.util.Collections.shuffle(destinos, random);
        destinos.sort(Comparator.comparingInt(posicion -> distancias.get(posicion)));

        List<Enemigo> candidatos = new ArrayList<>(juego.getEnemigos());
        java.util.Collections.shuffle(candidatos, random);
        candidatos.sort(Comparator.comparingInt((Enemigo enemigo) ->
                distancias.getOrDefault(enemigo.getPosicion(), Integer.MAX_VALUE)).reversed());
        int cantidad = Math.min(Math.min(cantidadAliados, candidatos.size()), destinos.size());
        for (int indice = 0; indice < cantidad; indice++) {
            Enemigo enemigo = candidatos.get(indice);
            Posicion destino = destinos.get(indice);
            int distanciaActual = distancias.getOrDefault(enemigo.getPosicion(), Integer.MAX_VALUE);
            if (distancias.get(destino) >= distanciaActual) {
                continue;
            }
            mapa.getCelda(enemigo.getPosicion()).quitarEnemigo(enemigo);
            enemigo.setPosicion(destino);
            mapa.getCelda(destino).agregarEnemigo(enemigo);
        }
    }

    /** Resumen del ajuste aplicado para consola, pruebas y telemetria. */
    record ResultadoEquilibrio(int originales, int objetivo,
            int agregados, int finales) { }

    private static Map<Posicion, Integer> calcularDistancias(Mapa mapa) {
        Map<Posicion, Integer> distancias = new HashMap<>();
        ArrayDeque<Posicion> pendientes = new ArrayDeque<>();
        Posicion inicio = mapa.getInicio();
        distancias.put(inicio, 0);
        pendientes.add(inicio);
        while (!pendientes.isEmpty()) {
            Posicion actual = pendientes.removeFirst();
            for (Direccion direccion : Direccion.values()) {
                Posicion candidata = actual.mover(direccion);
                if (mapa.esTransitable(candidata) && !distancias.containsKey(candidata)) {
                    distancias.put(candidata, distancias.get(actual) + 1);
                    pendientes.addLast(candidata);
                }
            }
        }
        return distancias;
    }
}
