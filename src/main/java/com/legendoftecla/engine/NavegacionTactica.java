package com.legendoftecla.engine;

import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.validation.Validaciones;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

/** Busqueda de rutas determinista compartida por aliados y enemigos. */
public final class NavegacionTactica {
    private NavegacionTactica() { }

    /** Devuelve la distancia transitable o -1 cuando no existe ruta. */
    public static int distancia(Mapa mapa, Posicion origen, Posicion objetivo) {
        validar(mapa, origen, objetivo);
        if (origen.equals(objetivo)) return 0;
        ArrayDeque<Posicion> pendientes = new ArrayDeque<>();
        Map<Posicion, Integer> distancias = new HashMap<>();
        pendientes.add(origen);
        distancias.put(origen, 0);
        while (!pendientes.isEmpty()) {
            Posicion actual = pendientes.removeFirst();
            int distancia = distancias.get(actual);
            for (Direccion direccion : Direccion.values()) {
                Posicion candidata = actual.mover(direccion);
                if (distancias.containsKey(candidata) || !mapa.esTransitable(candidata)) continue;
                if (candidata.equals(objetivo)) return distancia + 1;
                distancias.put(candidata, distancia + 1);
                pendientes.addLast(candidata);
            }
        }
        return -1;
    }

    /** Devuelve el primer paso de una ruta minima o {@code null} si no existe. */
    public static Direccion primerPaso(Mapa mapa, Posicion origen, Posicion objetivo) {
        validar(mapa, origen, objetivo);
        if (origen.equals(objetivo)) return null;
        ArrayDeque<Posicion> pendientes = new ArrayDeque<>();
        Map<Posicion, Posicion> anterior = new HashMap<>();
        Map<Posicion, Direccion> entradas = new HashMap<>();
        pendientes.add(origen);
        anterior.put(origen, null);
        while (!pendientes.isEmpty() && !anterior.containsKey(objetivo)) {
            Posicion actual = pendientes.removeFirst();
            for (Direccion direccion : Direccion.values()) {
                Posicion candidata = actual.mover(direccion);
                if (anterior.containsKey(candidata) || !mapa.esTransitable(candidata)) continue;
                anterior.put(candidata, actual);
                entradas.put(candidata, direccion);
                pendientes.addLast(candidata);
            }
        }
        if (!anterior.containsKey(objetivo)) return null;
        Posicion paso = objetivo;
        while (anterior.get(paso) != null && !anterior.get(paso).equals(origen)) {
            paso = anterior.get(paso);
        }
        return entradas.get(paso);
    }

    private static void validar(Mapa mapa, Posicion origen, Posicion objetivo) {
        Validaciones.noNulo(mapa, "Mapa");
        Validaciones.noNulo(origen, "Origen");
        Validaciones.noNulo(objetivo, "Objetivo");
    }
}
