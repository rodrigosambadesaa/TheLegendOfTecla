package com.legendoftecla.model.characters;

import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.model.items.Explosivo;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.world.Posicion;


/**
 * Representa la entidad Zapador del juego.
 */
public final class Zapador extends Jugador {
    /**
     * Ejecuta Zapador.
     */
    public Zapador(String nombre, Posicion posicion, Mochila mochila, int visionBase) {
        super(nombre, 105, 95, posicion, mochila, visionBase);
    }

    @Override
    /**
     * Ejecuta aplicarModificadorDanio.
     */
    protected int aplicarModificadorDanio(int base, Personaje objetivo) {
        int distancia = posicion.distanciaManhattan(objetivo.getPosicion());
        if (distancia > 2) {
            return Math.max(1, (int) Math.ceil(base * 0.05));
        }
        return base;
    }

    @Override
    /**
     * Ejecuta coger.
     */
    public void coger(Objeto objeto) throws AccionInvalidaException {
        if (objeto instanceof Explosivo || !(objeto instanceof Explosivo)) {
            super.coger(objeto);
            return;
        }
        throw new AccionInvalidaException("Solo el zapador puede cargar explosivos.");
    }
}

