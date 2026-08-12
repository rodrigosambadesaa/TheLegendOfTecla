package com.legendoftecla.loader;

import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.model.items.Botiquin;
import com.legendoftecla.model.items.Municion;
import com.legendoftecla.model.items.TipoMunicion;
import com.legendoftecla.model.items.ToritoRojo;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/** Distribuye suministros de apoyo adicionales en las dificultades faciles. */
final class GeneradorSuministrosDificultad {
    private static final int CURACION_BOTIQUIN = 20;
    private static final int ENERGIA_TORITO = 30;

    private GeneradorSuministrosDificultad() {
    }

    /**
     * Agrega la misma cantidad de botiquines y Toritos, repartidos entre las
     * celdas transitables y lejos de las posiciones de inicio y objetivo.
     *
     * @param mapa mapa que recibe los suministros
     * @param dificultad dificultad seleccionada
     * @param random fuente determinista para distribuir posiciones
     */
    static void poblar(Mapa mapa, Dificultad dificultad, Random random) {
        int cantidad = dificultad.calcularSuministrosExtra(
                mapa.getFilas() * mapa.getColumnas());
        int paquetesMunicion = dificultad.calcularMunicionExtra(
                mapa.getFilas() * mapa.getColumnas());
        if (cantidad == 0 && paquetesMunicion == 0) {
            return;
        }

        List<Posicion> posiciones = posicionesTransitables(mapa, false);
        if (posiciones.isEmpty()) {
            posiciones = posicionesTransitables(mapa, true);
        }
        if (posiciones.isEmpty()) {
            return;
        }
        Collections.shuffle(posiciones, random);

        for (int indice = 0; indice < cantidad; indice++) {
            Posicion posicion = posiciones.get(indice % posiciones.size());
            mapa.getCelda(posicion).agregarObjeto(new Botiquin(
                    "botiquin_apoyo_" + dificultad.name().toLowerCase(Locale.ROOT) + "_" + indice,
                    "Suministro adicional de dificultad",
                    1.0,
                    CURACION_BOTIQUIN));
        }
        for (int indice = 0; indice < cantidad; indice++) {
            Posicion posicion = posiciones.get((cantidad + indice) % posiciones.size());
            mapa.getCelda(posicion).agregarObjeto(new ToritoRojo(
                    "torito_apoyo_" + dificultad.name().toLowerCase(Locale.ROOT) + "_" + indice,
                    "Suministro adicional de dificultad",
                    0.5,
                    ENERGIA_TORITO));
        }
        for (int indice = 0; indice < paquetesMunicion; indice++) {
            Posicion posicion = posiciones.get((cantidad * 2 + indice) % posiciones.size());
            mapa.getCelda(posicion).agregarObjeto(new Municion(
                    "municion_rifle_" + dificultad.name().toLowerCase(Locale.ROOT)
                            + "_" + indice,
                    0.8, TipoMunicion.RIFLE, 6));
        }
    }

    private static List<Posicion> posicionesTransitables(Mapa mapa, boolean incluirExtremos) {
        List<Posicion> posiciones = new ArrayList<>();
        for (int fila = 0; fila < mapa.getFilas(); fila++) {
            for (int columna = 0; columna < mapa.getColumnas(); columna++) {
                Posicion posicion = new Posicion(fila, columna);
                if (mapa.esTransitable(posicion)
                        && (incluirExtremos
                                || (!posicion.equals(mapa.getInicio())
                                        && !posicion.equals(mapa.getObjetivo())))) {
                    posiciones.add(posicion);
                }
            }
        }
        return posiciones;
    }
}
