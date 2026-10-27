package com.legendoftecla.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.legendoftecla.stats.EstadisticasGlobales;

import java.io.IOException;
import java.nio.file.Path;

/** Persistencia opcional de estadísticas acumuladas entre partidas. */
public final class PersistenciaEstadisticas {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private PersistenciaEstadisticas() { }
    public static void guardar(EstadisticasGlobales estadisticas, Path archivo)
            throws IOException {
        ArchivoJsonAcotado.escribir(archivo, GSON.toJson(estadisticas.snapshot()));
    }
    public static EstadisticasGlobales cargar(Path archivo) throws IOException {
        try {
            var snapshot = GSON.fromJson(ArchivoJsonAcotado.leer(archivo),
                    EstadisticasGlobales.Snapshot.class);
            EstadisticasGlobales estadisticas = new EstadisticasGlobales();
            estadisticas.restaurar(snapshot);
            return estadisticas;
        } catch (RuntimeException error) {
            throw new IOException("Estadisticas corruptas", error);
        }
    }
}
