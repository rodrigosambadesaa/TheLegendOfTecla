package com.legendoftecla.engine;

import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;

import java.util.HashSet;
import java.util.Set;

/** Decide qué celdas oscuras quedan iluminadas por fuego, antorchas o linternas. */
public final class SistemaIluminacion {
    private SistemaIluminacion() { }

    /**
     * Pre-calcula las posiciones iluminadas por linternas activas.
     * Llamar al inicio de cada turno y pasar el resultado a {@link #hayLuz(Juego, Posicion, Set)}.
     *
     * @param juego partida vigente
     * @return conjunto de posiciones iluminadas por linternas
     */
    public static Set<Posicion> precalcularIluminacion(Juego juego) {
        Set<Posicion> iluminadas = new HashSet<>();
        agregarIluminacion(juego.getJugador(), iluminadas);
        for (Aliado aliado : juego.getAliados()) {
            if (aliado.getSalud() > 0) {
                agregarIluminacion(aliado, iluminadas);
            }
        }
        return iluminadas;
    }

    /**
     * Consulta rápida con cache pre-calculado.
     *
     * @param juego partida vigente
     * @param posicion celda a consultar
     * @param iluminadasPorLinterna cache generado por {@link #precalcularIluminacion(Juego)}
     * @return {@code true} si la celda tiene luz suficiente
     */
    public static boolean hayLuz(Juego juego, Posicion posicion, Set<Posicion> iluminadasPorLinterna) {
        Celda celda = juego.getMapa().getCelda(posicion);
        if (!celda.isOscura() || celda.estaArdiendo() || celda.hasAntorchaMural()) return true;
        return iluminadasPorLinterna.contains(posicion);
    }

    /**
     * Consulta sin cache (retrocompatibilidad). Itera sobre todos los aliados.
     *
     * @param juego partida vigente
     * @param posicion celda a consultar
     * @return {@code true} si la celda tiene luz suficiente
     */
    public static boolean hayLuz(Juego juego, Posicion posicion) {
        Celda celda = juego.getMapa().getCelda(posicion);
        if (!celda.isOscura() || celda.estaArdiendo() || celda.hasAntorchaMural()) return true;
        if (ilumina(juego.getJugador(), posicion)) return true;
        return juego.getAliados().stream().anyMatch(aliado -> aliado.getSalud() > 0 && ilumina(aliado, posicion));
    }

    private static boolean ilumina(Personaje personaje, Posicion posicion) {
        return personaje.isLinternaActiva()
                && personaje.getPosicion().distanciaManhattan(posicion) <= personaje.getAlcanceLinterna();
    }

    private static void agregarIluminacion(Personaje personaje, Set<Posicion> destino) {
        if (!personaje.isLinternaActiva()) return;
        int alcance = personaje.getAlcanceLinterna();
        Posicion centro = personaje.getPosicion();
        for (int df = -alcance; df <= alcance; df++) {
            int colMax = alcance - Math.abs(df);
            for (int dc = -colMax; dc <= colMax; dc++) {
                destino.add(new Posicion(centro.getFila() + df, centro.getColumna() + dc));
            }
        }
    }
}
