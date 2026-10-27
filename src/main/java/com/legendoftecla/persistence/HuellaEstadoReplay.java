package com.legendoftecla.persistence;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.legendoftecla.model.world.Juego;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

/**
 * Huella reproducible del estado persistible completo de una partida.
 *
 * <p>La normalizacion ordena las propiedades JSON y las colecciones que
 * representan conjuntos, pero conserva el orden de mochilas, personajes y
 * elementos del mapa, que forma parte del estado de la partida.
 */
final class HuellaEstadoReplay {
    private static final Gson JSON = new Gson();

    private HuellaEstadoReplay() {
    }

    static String calcular(Juego juego) {
        Objects.requireNonNull(juego, "Juego");
        JsonElement estado = JSON.toJsonTree(PersistenciaPartida.capturar(juego, 0L));
        String canonico = "tecla-replay-v2\n" + JSON.toJson(normalizar(estado, false));
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(canonico.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException imposible) {
            throw new IllegalStateException(imposible);
        }
    }

    private static JsonElement normalizar(JsonElement elemento, boolean ordenarArray) {
        if (elemento.isJsonObject()) {
            JsonObject original = elemento.getAsJsonObject();
            JsonObject ordenado = new JsonObject();
            original.keySet().stream().sorted().forEach(nombre ->
                    ordenado.add(nombre, normalizar(original.get(nombre), esConjunto(nombre))));
            return ordenado;
        }
        if (elemento.isJsonArray()) {
            List<JsonElement> normalizados = new ArrayList<>();
            for (JsonElement parte : elemento.getAsJsonArray()) {
                normalizados.add(normalizar(parte, false));
            }
            if (ordenarArray) {
                normalizados.sort(Comparator.comparing(JSON::toJson));
            }
            JsonArray resultado = new JsonArray();
            normalizados.forEach(resultado::add);
            return resultado;
        }
        return elemento.deepCopy();
    }

    private static boolean esConjunto(String nombre) {
        return "inspeccionadas".equals(nombre) || "logros".equals(nombre)
                || "habilidades".equals(nombre);
    }
}
