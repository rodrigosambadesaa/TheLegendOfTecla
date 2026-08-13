package com.legendoftecla.loader;

import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Armadura;
import com.legendoftecla.model.items.Binocular;
import com.legendoftecla.model.items.Botiquin;
import com.legendoftecla.model.items.Componente;
import com.legendoftecla.model.items.CuboAgua;
import com.legendoftecla.model.items.Explosivo;
import com.legendoftecla.model.items.Granada;
import com.legendoftecla.model.items.Linterna;
import com.legendoftecla.model.items.Municion;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.items.TipoGranada;
import com.legendoftecla.model.items.TipoMunicion;
import com.legendoftecla.model.items.ToritoRojo;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Escala todas las familias de recursos con la poblacion de la partida. */
final class GeneradorSuministrosPoblacion {
    private static final int ENTIDADES_POR_LOTE = 8;
    private static final int TIPOS_RECURSO = 11;

    private GeneradorSuministrosPoblacion() { }

    /**
     * Añade lotes completos y equilibrados segun aliados y enemigos.
     *
     * @param juego partida ya poblada
     * @param random fuente determinista para repartir los objetos
     * @return cantidad total de objetos añadidos
     */
    static int poblar(Juego juego, Random random) {
        int entidades = juego.getAliadosRegistrados().size() + juego.getEnemigos().size();
        if (entidades <= 0) return 0;
        int lotes = Math.max(1, (entidades + ENTIDADES_POR_LOTE - 1) / ENTIDADES_POR_LOTE);
        List<Posicion> posiciones = posicionesTransitables(juego.getMapa());
        if (posiciones.isEmpty()) return 0;
        Collections.shuffle(posiciones, random);
        int total = lotes * TIPOS_RECURSO;
        for (int indice = 0; indice < total; indice++) {
            Posicion posicion = posiciones.get(indice % posiciones.size());
            juego.getMapa().getCelda(posicion).agregarObjeto(crear(indice, indice % TIPOS_RECURSO));
        }
        juego.getConsola().imprimirInfo("Suministros por poblacion: entidades=" + entidades
                + " | lotes=" + lotes + " | objetos=" + total);
        return total;
    }

    private static Objeto crear(int indice, int tipo) {
        String sufijo = Integer.toString(indice);
        return switch (tipo) {
            case 0 -> new Botiquin("botiquin_poblacion_" + sufijo,
                    "Curacion para despliegues numerosos", 1.0, 25);
            case 1 -> new ToritoRojo("torito_poblacion_" + sufijo,
                    "Energia para despliegues numerosos", 0.5, 30);
            case 2 -> new Municion("municion_poblacion_" + sufijo,
                    0.8, TipoMunicion.RIFLE, 12);
            case 3 -> new Arma("rifle_poblacion_" + sufijo,
                    "Arma humana de reserva", 3.0, 10, true, TipoMunicion.RIFLE, 8, 8);
            case 4 -> new Armadura("armadura_poblacion_" + sufijo,
                    "Blindaje humano de reserva", 5.0, 3, 10, 10);
            case 5 -> new Explosivo("explosivo_poblacion_" + sufijo,
                    "Carga tactica de reserva", 1.5);
            case 6 -> new Granada("granada_poblacion_" + sufijo,
                    "Granada de fragmentacion de reserva", 0.8, TipoGranada.FRAGMENTACION);
            case 7 -> new Binocular("binocular_poblacion_" + sufijo,
                    "Optica para exploracion del escuadron", 1.0, 2);
            case 8 -> new CuboAgua("cubo_poblacion_" + sufijo,
                    "Agua para controlar incendios", 2.0, true);
            case 9 -> new Linterna("linterna_poblacion_" + sufijo,
                    "Iluminacion para equipos de exploracion", 0.8, 4);
            default -> new Componente("componente_poblacion_" + sufijo,
                    "Material para fabricacion de campaña", 0.5);
        };
    }

    private static List<Posicion> posicionesTransitables(Mapa mapa) {
        List<Posicion> posiciones = new ArrayList<>();
        for (int fila = 0; fila < mapa.getFilas(); fila++) {
            for (int columna = 0; columna < mapa.getColumnas(); columna++) {
                Posicion posicion = new Posicion(fila, columna);
                if (mapa.esTransitable(posicion)
                        && !posicion.equals(mapa.getInicio())
                        && !posicion.equals(mapa.getObjetivo())) {
                    posiciones.add(posicion);
                }
            }
        }
        if (posiciones.isEmpty() && mapa.esTransitable(mapa.getInicio())) {
            posiciones.add(mapa.getInicio());
        }
        return posiciones;
    }
}
