package com.legendoftecla.procedural;

import com.legendoftecla.model.elements.EstadoPuerta;
import com.legendoftecla.model.elements.Mina;
import com.legendoftecla.model.elements.Puerta;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.AmbientacionMapa;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/** Operaciones comunes que garantizan inicio, salida y ruta conectada. */
abstract class GeneradorBase implements GeneradorMapa {
    protected Mapa nuevo(String tipo, ConfiguracionGeneracion config, boolean transitable) {
        Posicion inicio = new Posicion(1, 1);
        Posicion salida = new Posicion(config.filas() - 2, config.columnas() - 2);
        Mapa mapa = new Mapa(tipo, "Generado proceduralmente", config.filas(),
                config.columnas(), inicio, salida);
        for (int f = 0; f < config.filas(); f++) {
            for (int c = 0; c < config.columnas(); c++) {
                boolean borde = f == 0 || c == 0 || f == config.filas() - 1
                        || c == config.columnas() - 1;
                mapa.setCelda(f, c, new Celda(borde ? "Muro" : "Terreno",
                        !borde && transitable));
            }
        }
        return mapa;
    }

    protected void garantizarRuta(Mapa mapa) {
        Posicion inicio = mapa.getInicio();
        Posicion salida = mapa.getObjetivo();
        for (int c = inicio.getColumna(); c <= salida.getColumna(); c++) {
            mapa.getCelda(new Posicion(inicio.getFila(), c)).setTransitable(true);
        }
        for (int f = inicio.getFila(); f <= salida.getFila(); f++) {
            mapa.getCelda(new Posicion(f, salida.getColumna())).setTransitable(true);
        }
    }

    protected void decorar(Mapa mapa, ConfiguracionGeneracion config, RandomGenerator random) {
        List<Posicion> candidatas = new ArrayList<>();
        for (int f = 1; f < mapa.getFilas() - 1; f++) {
            for (int c = 1; c < mapa.getColumnas() - 1; c++) {
                Posicion p = new Posicion(f, c);
                if (mapa.esTransitable(p) && !p.equals(mapa.getInicio())
                        && !p.equals(mapa.getObjetivo())) candidatas.add(p);
            }
        }
        mezclar(candidatas, random);
        int indice = 0;
        for (int i = 0; i < config.puertas() && indice < candidatas.size(); i++, indice++) {
            mapa.getCelda(candidatas.get(indice)).agregarElemento(new Puerta(
                    "puerta-" + i, EstadoPuerta.ABIERTA, null, true, 15));
        }
        for (int i = 0; i < config.peligros() && indice < candidatas.size(); i++, indice++) {
            mapa.getCelda(candidatas.get(indice)).agregarElemento(
                    new Mina("mina-" + i, 12, 1, false));
        }
    }

    /** Completa la descripcion narrativa de todas las celdas del mapa generado. */
    protected Mapa finalizar(Mapa mapa) {
        AmbientacionMapa.completar(mapa);
        return mapa;
    }

    private void mezclar(List<Posicion> posiciones, RandomGenerator random) {
        for (int indice = posiciones.size() - 1; indice > 0; indice--) {
            int elegido = random.nextInt(indice + 1);
            Posicion temporal = posiciones.get(indice);
            posiciones.set(indice, posiciones.get(elegido));
            posiciones.set(elegido, temporal);
        }
    }
}
