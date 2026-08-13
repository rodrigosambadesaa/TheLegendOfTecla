package com.legendoftecla.procedural;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;
import java.util.SplittableRandom;
/** Laberinto de bandas alternas con pasos sembrados. */
public final class GeneradorLaberinto extends GeneradorBase {
    public Mapa generar(long seed, ConfiguracionGeneracion config) {
        SplittableRandom random = new SplittableRandom(seed);
        Mapa mapa = nuevo("Laberinto-" + seed, config, true);
        for (int f = 2; f < config.filas() - 2; f += 2) {
            int paso = 1 + random.nextInt(config.columnas() - 2);
            for (int c = 1; c < config.columnas() - 1; c++) {
                if (c != paso) mapa.getCelda(new Posicion(f, c)).setTransitable(false);
            }
        }
        garantizarRuta(mapa); decorar(mapa, config, random); return finalizar(mapa);
    }
}
