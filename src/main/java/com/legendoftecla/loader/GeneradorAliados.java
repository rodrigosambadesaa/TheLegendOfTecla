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

import java.util.Random;

/** Aplica la politica unica de generacion automatica de aliados. */
final class GeneradorAliados {
    private GeneradorAliados() {
    }

    static int poblar(Juego juego, Mapa mapa, Dificultad dificultad, Random random, String prefijo) {
        Posicion despliegue = mapa.getInicio();
        if (!mapa.esTransitable(despliegue)) {
            throw new IllegalStateException(
                    "La casilla inicial debe ser transitable para desplegar el escuadron.");
        }

        int cantidad = calcularCantidad(mapa, dificultad);
        int area = mapa.getFilas() * mapa.getColumnas();
        int salud = 90 + Math.min(30, area / 500 * 5);
        int energia = 140 + Math.min(160, (mapa.getFilas() + mapa.getColumnas()) * 2);
        int vision = area >= 1600 ? 4 : 3;

        for (int indice = 0; indice < cantidad; indice++) {
            Aliado aliado = new Aliado(prefijo + "_" + (indice + 1), despliegue,
                    new Mochila(4, 12), vision);
            aliado.configurarEstadisticas(salud, energia, vision);
            aliado.getMochila().guardar(new Botiquin("botiquin_apoyo_" + prefijo + "_" + (indice + 1),
                    "Botiquin reservado para asistencia prioritaria", 1.0, 25));
            aliado.getMochila().guardar(new ToritoRojo("torito_apoyo_" + prefijo + "_" + (indice + 1),
                    "Suministro energetico reservado para asistencia", 0.5, 30));
            if (indice % 2 == 0) {
                aliado.getMochila().guardar(new Binocular("radar_tactico_" + prefijo + "_" + (indice + 1),
                        "Radar tactico asignado automaticamente", 1.0, 2));
            }
            mapa.getCelda(despliegue).agregarAliado(aliado);
            juego.agregarAliado(aliado);
        }
        DistribucionEnemigaEscuadron.endurecer(juego, random, cantidad);
        return cantidad;
    }

    static int calcularCantidad(Mapa mapa, Dificultad dificultad) {
        double escalaMapa = Math.sqrt((double) mapa.getFilas() * mapa.getColumnas()) / 10.0;
        double escalaAmenaza = Math.sqrt(dificultad.getMultiplicadorEnemigos());
        return Math.max(1, Math.min(12, (int) Math.ceil(escalaMapa * escalaAmenaza)));
    }

}
