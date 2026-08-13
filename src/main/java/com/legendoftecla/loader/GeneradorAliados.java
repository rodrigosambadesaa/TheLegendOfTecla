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
        return poblar(juego, mapa, dificultad, random, prefijo, -1);
    }

    static int poblar(Juego juego, Mapa mapa, Dificultad dificultad, Random random,
            String prefijo, int cantidadSolicitada) {
        return poblar(juego, mapa, dificultad, random, prefijo, cantidadSolicitada, 0);
    }

    static int poblar(Juego juego, Mapa mapa, Dificultad dificultad, Random random,
            String prefijo, int cantidadSolicitada, int nivelSolicitado) {
        Posicion despliegue = mapa.getInicio();
        if (!mapa.esTransitable(despliegue)) {
            throw new IllegalStateException(
                    "La casilla inicial debe ser transitable para desplegar el escuadron.");
        }

        int cantidad = cantidadSolicitada < 0
                ? calcularCantidad(mapa, dificultad)
                : cantidadSolicitada;
        int area = mapa.getFilas() * mapa.getColumnas();
        int salud = 90 + Math.min(30, area / 500 * 5);
        int energia = 140 + Math.min(160, (mapa.getFilas() + mapa.getColumnas()) * 2);
        int vision = area >= 1600 ? 4 : 3;
        int nivel = nivelSolicitado <= 0 ? 1 : nivelSolicitado;
        int bonusNivel = nivel - 1;
        salud += bonusNivel * 8;
        energia += bonusNivel * 10;
        vision = Math.min(12, vision + bonusNivel / 5);

        for (int indice = 0; indice < cantidad; indice++) {
            Aliado aliado = new Aliado(prefijo + "_" + (indice + 1), despliegue,
                    new Mochila(4 + bonusNivel / 4, 12 + bonusNivel * 1.5), vision);
            aliado.setNivel(nivel);
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
