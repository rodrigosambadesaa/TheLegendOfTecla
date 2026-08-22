package com.legendoftecla.engine;

import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.validation.Validaciones;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache por turno que reutiliza un único BFS completo desde cada origen.
 * <p>
 * Cuando varios aliados necesitan distancias o primer paso desde la misma
 * posición de origen, el BFS solo se ejecuta una vez y las consultas
 * posteriores son O(1).
 */
public final class CacheNavegacion {
    private final Mapa mapa;
    private final Map<Posicion, ResultadoBFS> cache = new HashMap<>();

    /**
     * Crea un cache ligado al mapa vigente durante un turno.
     *
     * @param mapa mapa no nulo del turno actual
     */
    public CacheNavegacion(Mapa mapa) {
        this.mapa = Validaciones.noNulo(mapa, "Mapa");
    }

    /**
     * Distancia transitable entre dos posiciones o {@code -1} si no hay ruta.
     *
     * @param origen punto de partida
     * @param destino punto de llegada
     * @return distancia en casillas transitables
     */
    public int distancia(Posicion origen, Posicion destino) {
        ResultadoBFS bfs = obtenerBFS(origen);
        Integer distancia = bfs.distancias.get(destino);
        return distancia != null ? distancia : -1;
    }

    /**
     * Primer paso de la ruta mínima o {@code null} si no existe.
     *
     * @param origen punto de partida
     * @param destino punto de llegada
     * @return primera dirección del camino óptimo
     */
    public Direccion primerPaso(Posicion origen, Posicion destino) {
        if (origen.equals(destino)) return null;
        ResultadoBFS bfs = obtenerBFS(origen);
        if (!bfs.anteriores.containsKey(destino)) return null;
        Posicion paso = destino;
        while (bfs.anteriores.get(paso) != null && !bfs.anteriores.get(paso).equals(origen)) {
            paso = bfs.anteriores.get(paso);
        }
        return bfs.entradas.get(paso);
    }

    private ResultadoBFS obtenerBFS(Posicion origen) {
        return cache.computeIfAbsent(origen, this::explorar);
    }

    private ResultadoBFS explorar(Posicion origen) {
        ResultadoBFS resultado = new ResultadoBFS();
        ArrayDeque<Posicion> pendientes = new ArrayDeque<>();
        pendientes.add(origen);
        resultado.distancias.put(origen, 0);
        resultado.anteriores.put(origen, null);
        while (!pendientes.isEmpty()) {
            Posicion actual = pendientes.removeFirst();
            int distancia = resultado.distancias.get(actual);
            for (Direccion direccion : Direccion.values()) {
                Posicion candidata = actual.mover(direccion);
                if (resultado.distancias.containsKey(candidata)
                        || !mapa.esTransitable(candidata)) {
                    continue;
                }
                resultado.distancias.put(candidata, distancia + 1);
                resultado.anteriores.put(candidata, actual);
                resultado.entradas.put(candidata, direccion);
                pendientes.addLast(candidata);
            }
        }
        return resultado;
    }

    /** Estructura interna con los resultados completos de un BFS. */
    private static final class ResultadoBFS {
        final Map<Posicion, Integer> distancias = new HashMap<>();
        final Map<Posicion, Posicion> anteriores = new HashMap<>();
        final Map<Posicion, Direccion> entradas = new HashMap<>();
    }
}
