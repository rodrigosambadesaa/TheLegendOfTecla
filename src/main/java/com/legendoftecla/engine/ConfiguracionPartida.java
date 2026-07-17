package com.legendoftecla.engine;

import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.model.world.DimensionesMapa;

import java.nio.file.Path;

/**
 * Datos necesarios para crear una partida desde cualquier interfaz.
 *
 * @param nombreJugador nombre del personaje controlado por el usuario
 * @param clase clase elegida para el jugador
 * @param modo origen y tipo del mapa que se cargara
 * @param dificultad nivel de dificultad de la partida
 * @param dimensiones dimensiones opcionales del mapa
 * @param directorioDatos directorio de un escenario externo, si procede
 * @param conAliados indica si la partida debe incluir aliados generados automaticamente
 * @param varianteMapa variante determinista del mapa generado, entre 1 y 50
 */
public record ConfiguracionPartida(
        String nombreJugador,
        String clase,
        String modo,
        Dificultad dificultad,
        DimensionesMapa dimensiones,
        Path directorioDatos,
        boolean conAliados,
        int varianteMapa) {

    /**
     * Valida y crea una instancia de {@code ConfiguracionPartida}.
     */
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
        if (varianteMapa < 1 || varianteMapa > 50) {
            throw new IllegalArgumentException("La variante del mapa debe estar entre 1 y 50.");
        }
    }
}
