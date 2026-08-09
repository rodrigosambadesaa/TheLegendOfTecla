package com.legendoftecla.constants;

import java.text.Normalizer;
import java.util.Locale;

/** Define quien debe alcanzar la salida para completar la partida. */
public enum CondicionVictoria {
    SOLO_JUGADOR("Solo el jugador"),
    JUGADOR_Y_ALIADOS("Jugador y todos los aliados");

    private final String etiqueta;

    CondicionVictoria(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** @return texto mostrado en los asistentes de configuracion */
    public String getEtiqueta() {
        return etiqueta;
    }

    /**
     * Interpreta los valores admitidos por la consola y la linea de comandos.
     *
     * @param texto valor introducido
     * @return condicion reconocida o {@code null}
     */
    public static CondicionVictoria desdeTexto(String texto) {
        if (texto == null) {
            return null;
        }
        String normalizado = Normalizer.normalize(texto.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('-', '_')
                .replace(' ', '_');
        return switch (normalizado) {
            case "1", "solo", "jugador", "solo_jugador" -> SOLO_JUGADOR;
            case "2", "todos", "aliados", "jugador_y_aliados" -> JUGADOR_Y_ALIADOS;
            default -> null;
        };
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
