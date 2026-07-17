package com.legendoftecla.engine;

import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.model.world.DimensionesMapa;

import java.nio.file.Path;

/** Datos necesarios para crear una partida desde cualquier interfaz. */
public record ConfiguracionPartida(
        String nombreJugador,
        String clase,
        String modo,
        Dificultad dificultad,
        DimensionesMapa dimensiones,
        Path directorioDatos) {

    public ConfiguracionPartida {
        if (nombreJugador == null || nombreJugador.isBlank()) {
            throw new IllegalArgumentException("El nombre del jugador es obligatorio.");
        }
        if (!"marine".equals(clase) && !"francotirador".equals(clase) && !"zapador".equals(clase)) {
            throw new IllegalArgumentException("Clase de jugador invalida: " + clase);
        }
        if (!"default".equals(modo) && !"grande".equals(modo) && !"ficheros".equals(modo)) {
            throw new IllegalArgumentException("Modo de juego invalido: " + modo);
        }
        if (dificultad == null) {
            dificultad = Dificultad.NORMAL;
        }
        if ("ficheros".equals(modo) && directorioDatos == null) {
            throw new IllegalArgumentException("Selecciona el directorio del escenario.");
        }
    }
}
