package com.legendoftecla.procedural;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;
import java.util.SplittableRandom;
/** Salas rectangulares conectadas por la ruta garantizada. */
public final class GeneradorHabitaciones extends GeneradorBase {
    public Mapa generar(long seed, ConfiguracionGeneracion config) {
        SplittableRandom random = new SplittableRandom(seed);
        Mapa mapa = nuevo("Habitaciones-" + seed, config, false);
        int salas = Math.max(3, config.filas() * config.columnas() / 60);
        for (int i = 0; i < salas; i++) {
            int alto = 2 + random.nextInt(Math.max(1, Math.min(5, config.filas() - 3) - 1));
            int ancho = 2 + random.nextInt(Math.max(1, Math.min(7, config.columnas() - 3) - 1));
            int fila = 1 + random.nextInt(Math.max(1, config.filas() - alto - 1));
            int columna = 1 + random.nextInt(Math.max(1, config.columnas() - ancho - 1));
            for (int f = fila; f < Math.min(config.filas() - 1, fila + alto); f++) {
                for (int c = columna; c < Math.min(config.columnas() - 1, columna + ancho); c++) {
                    mapa.getCelda(new Posicion(f, c)).setTransitable(true);
                }
            }
        }
        garantizarRuta(mapa); decorar(mapa, config, random); return finalizar(mapa);
    }
}
