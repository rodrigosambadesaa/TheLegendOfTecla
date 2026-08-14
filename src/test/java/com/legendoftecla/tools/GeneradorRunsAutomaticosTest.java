package com.legendoftecla.tools;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.legendoftecla.validation.Limites;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneradorRunsAutomaticosTest {
    @TempDir
    Path temporal;

    @Test
    void generaPartidasCompletasConAmbasCondicionesYFinales() throws IOException {
        GeneradorRunsAutomaticos.ResumenLote resumen = GeneradorRunsAutomaticos.generar(
                temporal.resolve("runs"), 16, 77L, Instant.parse("2026-08-14T00:00:00Z"));

        assertEquals(16, resumen.totalRuns());
        assertEquals(16, resumen.runsAutonomos());
        assertEquals(16, resumen.runsCompletados());
        assertEquals(8, resumen.victoriasHumanas());
        assertEquals(8, resumen.victoriasEnemigas());
        assertEquals(8, resumen.porCondicion().get("SOLO_JUGADOR"));
        assertEquals(8, resumen.porCondicion().get("JUGADOR_Y_ALIADOS"));
        assertTrue(resumen.porFinal().containsKey("VICTORIA_TODOS_ALIADOS"));
        assertTrue(resumen.porFinal().containsKey("VICTORIA_ALIADA_POST_MORTEM"));
    }

    @Test
    void cubreDesdeCienHastaElMaximoDelJuego() throws IOException {
        Path salida = temporal.resolve("limites");
        GeneradorRunsAutomaticos.ResumenLote resumen = GeneradorRunsAutomaticos.generar(
                salida, 2, 91L, Instant.parse("2026-08-14T00:00:00Z"));

        assertEquals(GeneradorRunsAutomaticos.ALIADOS_MINIMOS, resumen.aliadosMinimos());
        assertEquals(Limites.ALIADOS_MAXIMOS, resumen.aliadosMaximos());
        JsonObject primero = JsonParser.parseString(Files.readString(
                salida.resolve("run-0001.json"))).getAsJsonObject();
        JsonObject ultimo = JsonParser.parseString(Files.readString(
                salida.resolve("run-0002.json"))).getAsJsonObject();
        assertEquals(100, primero.get("aliadosIniciales").getAsInt());
        assertEquals(Limites.ALIADOS_MAXIMOS, ultimo.get("aliadosIniciales").getAsInt());
        assertEquals("FINALIZADA", ultimo.getAsJsonArray("eventos")
                .get(ultimo.getAsJsonArray("eventos").size() - 1)
                .getAsJsonObject().get("fase").getAsString());
    }
}
