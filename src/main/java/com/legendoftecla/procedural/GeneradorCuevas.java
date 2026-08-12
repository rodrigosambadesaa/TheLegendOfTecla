package com.legendoftecla.procedural;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;
import java.util.SplittableRandom;
/** Cueva celular simplificada con ruta reparada deterministamente. */
public final class GeneradorCuevas extends GeneradorBase {
    public Mapa generar(long seed, ConfiguracionGeneracion config) {
        SplittableRandom random = new SplittableRandom(seed);
        Mapa mapa = nuevo("Cuevas-" + seed, config, true);
        for (int f = 1; f < config.filas() - 1; f++) {
            for (int c = 1; c < config.columnas() - 1; c++) {
                mapa.getCelda(new Posicion(f, c)).setTransitable(
                        random.nextDouble() >= config.densidadMuros());
            }
        }
        garantizarRuta(mapa); decorar(mapa, config, random); return mapa;
    }
}
