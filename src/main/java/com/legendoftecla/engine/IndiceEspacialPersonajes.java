package com.legendoftecla.engine;

import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/** Indice inmutable por turno que evita búsquedas cartesianas entre bandos. */
public final class IndiceEspacialPersonajes<T extends Personaje> {
    private static final int MAX_GRID = Limites.MAPA_MAXIMO;
    @SuppressWarnings("unchecked")
    private final List<T>[][] grid = new List[MAX_GRID][MAX_GRID];
    private final List<T> todos;
    private int filaMinima = MAX_GRID;
    private int filaMaxima = -1;
    private int columnaMinima = MAX_GRID;
    private int columnaMaxima = -1;

    public IndiceEspacialPersonajes(List<T> personajes) {
        this.todos = Validaciones.noNulo(personajes, "Personajes");
        for (T personaje : todos) {
            if (personaje == null) continue;
            Posicion pos = personaje.getPosicion();
            int f = pos.getFila();
            int c = pos.getColumna();
            if (f >= 0 && f < MAX_GRID && c >= 0 && c < MAX_GRID) {
                if (grid[f][c] == null) {
                    grid[f][c] = new ArrayList<>(2);
                }
                grid[f][c].add(personaje);
                filaMinima = Math.min(filaMinima, f);
                filaMaxima = Math.max(filaMaxima, f);
                columnaMinima = Math.min(columnaMinima, c);
                columnaMaxima = Math.max(columnaMaxima, c);
            }
        }
    }

    /** Busca por anillos Manhattan sin recorrer todos los personajes. */
    public T masCercano(Posicion origen, Predicate<T> filtro) {
        Validaciones.noNulo(origen, "Origen");
        Validaciones.noNulo(filtro, "Filtro");
        if (todos.isEmpty() || filaMaxima < 0) return null;
        int distanciaMaxima = Math.max(Math.abs(origen.getFila() - filaMinima),
                Math.abs(origen.getFila() - filaMaxima))
                + Math.max(Math.abs(origen.getColumna() - columnaMinima),
                Math.abs(origen.getColumna() - columnaMaxima));
        return masCercano(origen, distanciaMaxima, filtro);
    }

    /** Indica si alguna entrada del indice satisface el filtro. */
    public boolean alguno(Predicate<T> filtro) {
        Validaciones.noNulo(filtro, "Filtro");
        for (int i = 0; i < todos.size(); i++) {
            if (filtro.test(todos.get(i))) return true;
        }
        return false;
    }

    /** Devuelve candidatos situados dentro de un radio Manhattan acotado. */
    public List<T> cercanos(Posicion origen, int radio, Predicate<T> filtro) {
        Validaciones.noNulo(origen, "Origen");
        Validaciones.noNulo(filtro, "Filtro");
        if (radio < 0) throw new IllegalArgumentException("Radio negativo.");
        List<T> resultado = new ArrayList<>();
        int origF = origen.getFila();
        int origC = origen.getColumna();
        for (int distancia = 0; distancia <= radio; distancia++) {
            for (int deltaFila = -distancia; deltaFila <= distancia; deltaFila++) {
                int deltaColumna = distancia - Math.abs(deltaFila);
                int f = origF + deltaFila;
                if (f >= 0 && f < MAX_GRID) {
                    agregarPosicion(f, origC - deltaColumna, filtro, resultado);
                    if (deltaColumna != 0) {
                        agregarPosicion(f, origC + deltaColumna, filtro, resultado);
                    }
                }
            }
        }
        return List.copyOf(resultado);
    }

    /** Busca el candidato mas cercano sin superar el radio indicado. */
    public T masCercano(Posicion origen, int radio, Predicate<T> filtro) {
        Validaciones.noNulo(origen, "Origen");
        Validaciones.noNulo(filtro, "Filtro");
        if (radio < 0) throw new IllegalArgumentException("Radio negativo.");
        if (todos.isEmpty() || filaMaxima < 0) return null;
        int origF = origen.getFila();
        int origC = origen.getColumna();
        for (int distancia = 0; distancia <= radio; distancia++) {
            for (int deltaFila = -distancia; deltaFila <= distancia; deltaFila++) {
                int deltaColumna = distancia - Math.abs(deltaFila);
                int f = origF + deltaFila;
                if (f >= 0 && f < MAX_GRID) {
                    T p1 = buscarPosicion(f, origC - deltaColumna, filtro);
                    if (p1 != null) return p1;
                    if (deltaColumna != 0) {
                        T p2 = buscarPosicion(f, origC + deltaColumna, filtro);
                        if (p2 != null) return p2;
                    }
                }
            }
        }
        return null;
    }

    private T buscarPosicion(int f, int c, Predicate<T> filtro) {
        if (c >= 0 && c < MAX_GRID) {
            List<T> celda = grid[f][c];
            if (celda != null) {
                for (int i = 0; i < celda.size(); i++) {
                    T p = celda.get(i);
                    if (filtro.test(p)) return p;
                }
            }
        }
        return null;
    }

    private void agregarPosicion(int f, int c, Predicate<T> filtro, List<T> resultado) {
        if (c >= 0 && c < MAX_GRID) {
            List<T> celda = grid[f][c];
            if (celda != null) {
                for (int i = 0; i < celda.size(); i++) {
                    T p = celda.get(i);
                    if (filtro.test(p)) resultado.add(p);
                }
            }
        }
    }
}
