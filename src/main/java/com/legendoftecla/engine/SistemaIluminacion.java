package com.legendoftecla.engine;

import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;

/** Decide qué celdas oscuras quedan iluminadas por fuego, antorchas o linternas. */
public final class SistemaIluminacion {
    private SistemaIluminacion() { }

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
}
