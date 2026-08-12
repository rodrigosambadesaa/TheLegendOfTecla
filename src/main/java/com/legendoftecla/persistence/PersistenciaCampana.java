package com.legendoftecla.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.legendoftecla.missions.Campana;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Guarda la secuencia y progresión; inventario y aliados permanecen en el savegame. */
public final class PersistenciaCampana {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private PersistenciaCampana() { }

    public static void guardar(Campana campana, Path archivo) throws IOException {
        var progreso = campana.getProgresion();
        CampanaGuardada estado = new CampanaGuardada(CampanaGuardada.VERSION_ACTUAL,
                campana.getId(), campana.getIndice(), progreso.getNivel(),
                progreso.getExperiencia(), progreso.getDesbloqueadas());
        Path padre = archivo.toAbsolutePath().normalize().getParent();
        if (padre != null) Files.createDirectories(padre);
        Files.writeString(archivo, GSON.toJson(estado), StandardCharsets.UTF_8);
    }

    public static void restaurar(Campana campana, Path archivo) throws IOException {
        CampanaGuardada estado = GSON.fromJson(
                Files.readString(archivo, StandardCharsets.UTF_8), CampanaGuardada.class);
        if (estado == null || estado.version() != CampanaGuardada.VERSION_ACTUAL
                || !campana.getId().equals(estado.campanaId())) {
            throw new IOException("Campana corrupta, incompatible o de otra definicion");
        }
        campana.restaurarIndice(estado.indice());
        campana.getProgresion().restaurar(estado.nivel(), estado.experiencia(),
                estado.habilidades());
    }
}
