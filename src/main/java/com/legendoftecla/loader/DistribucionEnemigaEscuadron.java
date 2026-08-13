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
import java.util.Collections;
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
        dispersar(juego, random);
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
        int radioDeseado = calcularRadioSeguro(distancias);
        for (int radio = radioDeseado; radio >= 2; radio--) {
            int distanciaMinima = radio;
            List<Posicion> posiciones = distancias.entrySet().stream()
                    .filter(entrada -> entrada.getValue() >= distanciaMinima)
                    .map(Map.Entry::getKey)
                    .filter(posicion -> posicion.distanciaManhattan(mapa.getInicio())
                            >= distanciaMinima)
                    .filter(posicion -> posicion.distanciaManhattan(mapa.getObjetivo()) >= 2)
                    .filter(posicion -> !posicion.equals(mapa.getInicio())
                            && !posicion.equals(mapa.getObjetivo()))
                    .toList();
            if (!posiciones.isEmpty()) return posiciones;
        }
        return distancias.keySet().stream()
                .filter(posicion -> !posicion.equals(mapa.getInicio())
                        && !posicion.equals(mapa.getObjetivo()))
                .toList();
    }

    private static int calcularRadioSeguro(Map<Posicion, Integer> distancias) {
        int alcanceMapa = distancias.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        return Math.min(20, Math.max(4, alcanceMapa / 4));
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

    private static void dispersar(Juego juego, Random random) {
        if (juego.getEnemigos().isEmpty()) {
            return;
        }
        Mapa mapa = juego.getMapa();
        List<Posicion> destinos = distribuirPorSectores(
                posicionesDeDespliegue(mapa), mapa, random);
        if (destinos.isEmpty()) return;
        List<Enemigo> enemigos = new ArrayList<>(juego.getEnemigos());
        enemigos.forEach(enemigo -> mapa.getCelda(enemigo.getPosicion()).quitarEnemigo(enemigo));
        for (int indice = 0; indice < enemigos.size(); indice++) {
            Enemigo enemigo = enemigos.get(indice);
            Posicion destino = destinos.get(indice % destinos.size());
            enemigo.setPosicion(destino);
            mapa.getCelda(destino).agregarEnemigo(enemigo);
        }
    }

    private static List<Posicion> distribuirPorSectores(List<Posicion> posiciones,
            Mapa mapa, Random random) {
        int divisiones = 4;
        Map<Integer, List<Posicion>> sectores = new HashMap<>();
        for (Posicion posicion : posiciones) {
            int sectorFila = Math.min(divisiones - 1,
                    posicion.getFila() * divisiones / mapa.getFilas());
            int sectorColumna = Math.min(divisiones - 1,
                    posicion.getColumna() * divisiones / mapa.getColumnas());
            sectores.computeIfAbsent(sectorFila * divisiones + sectorColumna,
                    clave -> new ArrayList<>()).add(posicion);
        }
        sectores.values().forEach(lista -> Collections.shuffle(lista, random));
        List<Integer> claves = new ArrayList<>(sectores.keySet());
        Collections.shuffle(claves, random);
        List<Posicion> resultado = new ArrayList<>(posiciones.size());
        boolean quedanPosiciones = true;
        while (quedanPosiciones) {
            quedanPosiciones = false;
            for (Integer clave : claves) {
                List<Posicion> sector = sectores.get(clave);
                if (!sector.isEmpty()) {
                    resultado.add(sector.remove(sector.size() - 1));
                    quedanPosiciones = true;
                }
            }
        }
        return resultado;
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
