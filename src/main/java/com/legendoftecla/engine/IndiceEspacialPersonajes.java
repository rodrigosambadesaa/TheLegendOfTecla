package com.legendoftecla.engine;

import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.validation.Validaciones;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/** Indice inmutable por turno que evita búsquedas cartesianas entre bandos. */
public final class IndiceEspacialPersonajes<T extends Personaje> {
    private final Map<Posicion, List<T>> porPosicion = new LinkedHashMap<>();
    private int filaMinima = Integer.MAX_VALUE;
    private int filaMaxima = Integer.MIN_VALUE;
    private int columnaMinima = Integer.MAX_VALUE;
    private int columnaMaxima = Integer.MIN_VALUE;

    public IndiceEspacialPersonajes(List<T> personajes) {
        Validaciones.noNulo(personajes, "Personajes").forEach(personaje -> {
            Posicion posicion = Validaciones.noNulo(personaje, "Personaje").getPosicion();
            porPosicion.computeIfAbsent(posicion, clave -> new ArrayList<>()).add(personaje);
            filaMinima = Math.min(filaMinima, posicion.getFila());
            filaMaxima = Math.max(filaMaxima, posicion.getFila());
            columnaMinima = Math.min(columnaMinima, posicion.getColumna());
            columnaMaxima = Math.max(columnaMaxima, posicion.getColumna());
        });
    }

    /** Busca por anillos Manhattan sin recorrer todos los personajes. */
    public T masCercano(Posicion origen, Predicate<T> filtro) {
        Validaciones.noNulo(origen, "Origen");
        Validaciones.noNulo(filtro, "Filtro");
        if (porPosicion.isEmpty()) return null;
        int distanciaMaxima = Math.max(Math.abs(origen.getFila() - filaMinima),
                Math.abs(origen.getFila() - filaMaxima))
                + Math.max(Math.abs(origen.getColumna() - columnaMinima),
                Math.abs(origen.getColumna() - columnaMaxima));
        for (int distancia = 0; distancia <= distanciaMaxima; distancia++) {
            T candidato = buscarEnAnillo(origen, distancia, filtro);
            if (candidato != null) return candidato;
        }
        return null;
    }

    /** Indica si alguna entrada del indice satisface el filtro. */
    public boolean alguno(Predicate<T> filtro) {
        Validaciones.noNulo(filtro, "Filtro");
        return porPosicion.values().stream().flatMap(List::stream).anyMatch(filtro);
    }

    /** Devuelve candidatos situados dentro de un radio Manhattan acotado. */
    public List<T> cercanos(Posicion origen, int radio, Predicate<T> filtro) {
        Validaciones.noNulo(origen, "Origen");
        Validaciones.noNulo(filtro, "Filtro");
        if (radio < 0) throw new IllegalArgumentException("Radio negativo.");
        List<T> resultado = new ArrayList<>();
        for (int distancia = 0; distancia <= radio; distancia++) {
            agregarAnillo(origen, distancia, filtro, resultado);
        }
        return List.copyOf(resultado);
    }

    /** Busca el candidato mas cercano sin superar el radio indicado. */
    public T masCercano(Posicion origen, int radio, Predicate<T> filtro) {
        Validaciones.noNulo(origen, "Origen");
        Validaciones.noNulo(filtro, "Filtro");
        if (radio < 0) throw new IllegalArgumentException("Radio negativo.");
        for (int distancia = 0; distancia <= radio; distancia++) {
            T candidato = buscarEnAnillo(origen, distancia, filtro);
            if (candidato != null) return candidato;
        }
        return null;
    }

    private T buscarEnAnillo(Posicion origen, int distancia, Predicate<T> filtro) {
        List<T> encontrados = new ArrayList<>();
        agregarAnillo(origen, distancia, filtro, encontrados);
        return encontrados.isEmpty() ? null : encontrados.get(0);
    }

    private void agregarAnillo(Posicion origen, int distancia, Predicate<T> filtro,
                               List<T> resultado) {
        for (int deltaFila = -distancia; deltaFila <= distancia; deltaFila++) {
            int deltaColumna = distancia - Math.abs(deltaFila);
            agregarPosicion(origen, deltaFila, -deltaColumna, filtro, resultado);
            if (deltaColumna != 0) {
                agregarPosicion(origen, deltaFila, deltaColumna, filtro, resultado);
            }
        }
    }

    private void agregarPosicion(Posicion origen, int deltaFila, int deltaColumna,
                                 Predicate<T> filtro, List<T> resultado) {
        List<T> personajes = porPosicion.get(new Posicion(
                origen.getFila() + deltaFila, origen.getColumna() + deltaColumna));
        if (personajes != null) personajes.stream().filter(filtro).forEach(resultado::add);
    }
}
