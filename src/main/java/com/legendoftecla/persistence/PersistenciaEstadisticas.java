package com.legendoftecla.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.legendoftecla.stats.EstadisticasGlobales;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Persistencia opcional de estadísticas acumuladas entre partidas. */
public final class PersistenciaEstadisticas {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private PersistenciaEstadisticas() { }
    public static void guardar(EstadisticasGlobales estadisticas, Path archivo)
            throws IOException {
        Path padre = archivo.toAbsolutePath().normalize().getParent();
        if (padre != null) Files.createDirectories(padre);
        Files.writeString(archivo, GSON.toJson(estadisticas.snapshot()),
                StandardCharsets.UTF_8);
    }
    public static EstadisticasGlobales cargar(Path archivo) throws IOException {
        var snapshot = GSON.fromJson(Files.readString(archivo, StandardCharsets.UTF_8),
                EstadisticasGlobales.Snapshot.class);
        EstadisticasGlobales estadisticas = new EstadisticasGlobales();
        try {
            estadisticas.restaurar(snapshot);
        } catch (IllegalArgumentException error) {
            throw new IOException("Estadisticas corruptas", error);
        }
        return estadisticas;
    }
}
