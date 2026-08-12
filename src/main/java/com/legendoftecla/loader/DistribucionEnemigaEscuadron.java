package com.legendoftecla.loader;

import com.legendoftecla.model.characters.Enemigo;
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

/** Concentra parte de la amenaza en anillos de intercepcion cuando parte un escuadron. */
final class DistribucionEnemigaEscuadron {
    private DistribucionEnemigaEscuadron() {
    }

    static void endurecer(Juego juego, Random random, int cantidadAliados) {
        if (cantidadAliados <= 0 || juego.getEnemigos().isEmpty()) {
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
