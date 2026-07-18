package com.legendoftecla.loader;

import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.items.Binocular;
import com.legendoftecla.model.items.Botiquin;
import com.legendoftecla.model.items.ToritoRojo;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Aplica la politica unica de generacion automatica de aliados. */
final class GeneradorAliados {
    private GeneradorAliados() {
    }

    static int poblar(Juego juego, Mapa mapa, Dificultad dificultad, Random random, String prefijo) {
        List<Posicion> disponibles = posicionesDisponibles(mapa);
        if (disponibles.isEmpty()) {
            return 0;
        }
        Collections.shuffle(disponibles, random);

        int cantidad = Math.min(calcularCantidad(mapa, dificultad), disponibles.size());
        int area = mapa.getFilas() * mapa.getColumnas();
        int salud = 90 + Math.min(30, area / 500 * 5);
        int energia = 140 + Math.min(160, (mapa.getFilas() + mapa.getColumnas()) * 2);
        int vision = area >= 1600 ? 4 : 3;

        for (int indice = 0; indice < cantidad; indice++) {
            Posicion posicion = disponibles.get(indice);
            Aliado aliado = new Aliado(prefijo + "_" + (indice + 1), posicion, new Mochila(4, 12), vision);
            aliado.configurarEstadisticas(salud, energia, vision);
            aliado.getMochila().guardar(new Botiquin("botiquin_apoyo_" + prefijo + "_" + (indice + 1),
                    "Botiquin reservado para asistencia prioritaria", 1.0, 25));
            aliado.getMochila().guardar(new ToritoRojo("torito_apoyo_" + prefijo + "_" + (indice + 1),
                    "Suministro energetico reservado para asistencia", 0.5, 30));
            if (indice % 2 == 0) {
                aliado.getMochila().guardar(new Binocular("radar_tactico_" + prefijo + "_" + (indice + 1),
                        "Radar tactico asignado automaticamente", 1.0, 2));
            }
            mapa.getCelda(posicion).agregarAliado(aliado);
            juego.agregarAliado(aliado);
        }
        return cantidad;
    }

    static int calcularCantidad(Mapa mapa, Dificultad dificultad) {
        double escalaMapa = Math.sqrt((double) mapa.getFilas() * mapa.getColumnas()) / 10.0;
        double escalaAmenaza = Math.sqrt(dificultad.getMultiplicadorEnemigos());
        return Math.max(1, Math.min(12, (int) Math.ceil(escalaMapa * escalaAmenaza)));
    }

    private static List<Posicion> posicionesDisponibles(Mapa mapa) {
        List<Posicion> posiciones = new ArrayList<>();
        for (int fila = 0; fila < mapa.getFilas(); fila++) {
            for (int columna = 0; columna < mapa.getColumnas(); columna++) {
                Posicion posicion = new Posicion(fila, columna);
                if (mapa.esTransitable(posicion)
                        && !posicion.equals(mapa.getInicio())
                        && !posicion.equals(mapa.getObjetivo())
                        && mapa.getCelda(posicion).getEnemigos().isEmpty()
                        && mapa.getCelda(posicion).getAliados().isEmpty()) {
                    posiciones.add(posicion);
                }
            }
        }
        return posiciones;
    }
}
