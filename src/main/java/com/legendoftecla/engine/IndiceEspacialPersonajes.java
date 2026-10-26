package com.legendoftecla.engine;

import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Indice espacial inmutable por turno. Guarda las coordenadas en el momento de
 * construccion para evitar que el movimiento posterior modifique la instantanea.
 *
 * <p>Las busquedas recorren solo los personajes indexados, no los anillos
 * intermedios. Asi, incluso un radio Integer.MAX_VALUE o un origen remoto
 * conserva un coste acotado por el numero de personajes.
 */
public final class IndiceEspacialPersonajes<T extends Personaje> {
    private static final int MAX_GRID = Limites.MAPA_MAXIMO;

    private final List<T> todos;
    private final List<Entrada<T>> indexados;

    /**
     * Crea una instantanea que no comparte la lista mutable recibida.
     *
     * @param personajes personajes del turno; sin elementos nulos
     */
    public IndiceEspacialPersonajes(List<T> personajes) {
        this.todos = List.copyOf(Validaciones.noNulo(personajes, "Personajes"));
        List<Entrada<T>> entradas = new ArrayList<>(todos.size());
        for (T personaje : todos) {
            Posicion posicion = personaje.getPosicion();
            int fila = posicion.getFila();
            int columna = posicion.getColumna();
            if (fila >= 0 && fila < MAX_GRID && columna >= 0 && columna < MAX_GRID) {
                entradas.add(new Entrada<>(personaje, fila, columna));
            }
        }
        this.indexados = List.copyOf(entradas);
    }

    /** Busca el personaje mas cercano por distancia Manhattan. */
    public T masCercano(Posicion origen, Predicate<T> filtro) {
        return masCercano(origen, Integer.MAX_VALUE, filtro);
    }

    /** Indica si algun personaje de la instantanea satisface el filtro. */
    public boolean alguno(Predicate<T> filtro) {
        Validaciones.noNulo(filtro, "Filtro");
        for (T personaje : todos) {
            if (filtro.test(personaje)) return true;
        }
        return false;
    }

    /**
     * Devuelve candidatos dentro del radio en orden de distancia, fila y
     * columna. Los empates en una misma celda mantienen el orden de entrada.
     */
    public List<T> cercanos(Posicion origen, int radio, Predicate<T> filtro) {
        validarConsulta(origen, radio, filtro);
        List<Entrada<T>> seleccionados = new ArrayList<>();
        for (Entrada<T> entrada : indexados) {
            if (entrada.distancia(origen) <= radio && filtro.test(entrada.personaje())) {
                seleccionados.add(entrada);
            }
        }
        seleccionados.sort((primera, segunda) -> comparar(primera, segunda, origen));
        List<T> resultado = new ArrayList<>(seleccionados.size());
        for (Entrada<T> entrada : seleccionados) {
            resultado.add(entrada.personaje());
        }
        return List.copyOf(resultado);
    }

    /** Busca el primer candidato sin superar el radio indicado. */
    public T masCercano(Posicion origen, int radio, Predicate<T> filtro) {
        validarConsulta(origen, radio, filtro);
        Entrada<T> mejor = null;
        for (Entrada<T> entrada : indexados) {
            if (entrada.distancia(origen) > radio || !filtro.test(entrada.personaje())) {
                continue;
            }
            if (mejor == null || comparar(entrada, mejor, origen) < 0) {
                mejor = entrada;
            }
        }
        return mejor == null ? null : mejor.personaje();
    }

    private static <T extends Personaje> int comparar(
            Entrada<T> primera, Entrada<T> segunda, Posicion origen) {
        int comparacion = Integer.compare(primera.distancia(origen), segunda.distancia(origen));
        if (comparacion != 0) return comparacion;
        comparacion = Integer.compare(primera.fila(), segunda.fila());
        if (comparacion != 0) return comparacion;
        return Integer.compare(primera.columna(), segunda.columna());
    }

    private static <T extends Personaje> void validarConsulta(
            Posicion origen, int radio, Predicate<T> filtro) {
        Validaciones.noNulo(origen, "Origen");
        Validaciones.noNulo(filtro, "Filtro");
        if (radio < 0) throw new IllegalArgumentException("Radio negativo.");
    }

    private record Entrada<T extends Personaje>(T personaje, int fila, int columna) {
        private int distancia(Posicion origen) {
            return Math.abs(origen.getFila() - fila)
                    + Math.abs(origen.getColumna() - columna);
        }
    }
}
