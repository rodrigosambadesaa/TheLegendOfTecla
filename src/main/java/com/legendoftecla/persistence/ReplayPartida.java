package com.legendoftecla.persistence;
import java.util.List;
import java.util.Map;
/** Registro determinista de semilla, comandos y huella esperada. */
public record ReplayPartida(int version, long seed, Map<String, String> configuracion,
        List<String> comandos,
        String hashFinal) {
    public ReplayPartida {
        configuracion = configuracion == null ? Map.of() : Map.copyOf(configuracion);
        comandos = List.copyOf(comandos);
    }
    /** Constructor compatible con replays v1 que solo declaraban semilla. */
    public ReplayPartida(int version, long seed, List<String> comandos, String hashFinal) {
        this(version, seed, Map.of(), comandos, hashFinal);
    }
}
