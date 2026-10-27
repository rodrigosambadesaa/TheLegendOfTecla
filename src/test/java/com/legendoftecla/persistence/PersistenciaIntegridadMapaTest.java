package com.legendoftecla.persistence;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.legendoftecla.TestFixtures;
import com.legendoftecla.exceptions.JuegoException;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Regresion de integridad del mapa al importar savegames no confiables. */
class PersistenciaIntegridadMapaTest {
    private static final Gson JSON = new Gson();

    @TempDir Path directorio;

    @Test
    void partidaCompletaPermiteCargarCeldasFueraDeOrden() throws Exception {
        JsonObject original = partidaValida();
        JsonArray iniciales = celdas(original);
        List<com.google.gson.JsonElement> lista = new ArrayList<>();
        iniciales.forEach(elemento -> lista.add(elemento.deepCopy()));

        Random random = new Random(20261027L);
        for (int iteracion = 0; iteracion < 25; iteracion++) {
            Collections.shuffle(lista, random);
            JsonArray desordenadas = new JsonArray();
            lista.forEach(desordenadas::add);
            JsonObject partida = original.deepCopy();
            partida.getAsJsonObject("mapa").add("celdas", desordenadas);

            Juego restaurado = cargar(partida);
            assertEquals(original.getAsJsonObject("mapa").get("filas").getAsInt(),
                    restaurado.getMapa().getFilas());
            assertEquals(original.getAsJsonObject("mapa").get("columnas").getAsInt(),
                    restaurado.getMapa().getColumnas());
            for (int fila = 0; fila < restaurado.getMapa().getFilas(); fila++) {
                for (int columna = 0; columna < restaurado.getMapa().getColumnas(); columna++) {
                    assertTrue(restaurado.getMapa().getCelda(new Posicion(fila, columna)) != null);
                }
            }
        }
    }

    @Test
    void rechazaCeldasAusentesEnSavegame() {
        JsonObject partida = partidaValida();
        JsonArray celdas = celdas(partida);
        celdas.remove(celdas.size() - 1);
        assertThrows(JuegoException.class, () -> cargar(partida));
    }

    @Test
    void rechazaCeldasDuplicadasInclusoCuandoRecuentoCoincide() {
        JsonObject partida = partidaValida();
        JsonArray celdas = celdas(partida);
        celdas.set(celdas.size() - 1, celdas.get(0).deepCopy());
        assertThrows(JuegoException.class, () -> cargar(partida));
    }

    @Test
    void rechazaNulosYCoordenadasFueraDeRango() {
        JsonObject nula = partidaValida();
        celdas(nula).set(0, JsonNull.INSTANCE);
        assertThrows(JuegoException.class, () -> cargar(nula));

        for (int coordenada : new int[] {-1, Integer.MAX_VALUE}) {
            JsonObject fuera = partidaValida();
            celdas(fuera).get(0).getAsJsonObject().addProperty("fila", coordenada);
            assertThrows(JuegoException.class, () -> cargar(fuera));
        }

        JsonObject columna = partidaValida();
        celdas(columna).get(0).getAsJsonObject().addProperty("columna",
                columna.getAsJsonObject("mapa").get("columnas").getAsInt());
        assertThrows(JuegoException.class, () -> cargar(columna));
    }

    @Test
    void rechazaDimensionesAbusivasSinReservarUnaMatrizGigante() {
        for (int dimension : new int[] {0, -2, 201, 1_000_000}) {
            JsonObject partida = partidaValida();
            partida.getAsJsonObject("mapa").addProperty("filas", dimension);
            assertThrows(JuegoException.class, () -> cargar(partida));
        }
    }

    private JsonObject partidaValida() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        return JSON.toJsonTree(PersistenciaPartida.capturar(juego, 42L)).getAsJsonObject();
    }

    private static JsonArray celdas(JsonObject partida) {
        return partida.getAsJsonObject("mapa").getAsJsonArray("celdas");
    }

    private Juego cargar(JsonObject partida) throws Exception {
        Path archivo = directorio.resolve("partida-integridad.json");
        Files.writeString(archivo, JSON.toJson(partida));
        return PersistenciaPartida.cargar(archivo, TestFixtures.consola());
    }
}
