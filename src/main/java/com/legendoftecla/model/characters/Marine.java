package com.legendoftecla.model.characters;

import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.world.Posicion;


/**
 * Representa la entidad Marine del juego.
 */
public final class Marine extends Jugador {
    /**
     * Ejecuta Marine.
      * @param mochila valor de {@code mochila}
      * @param nombre valor de {@code nombre}
      * @param posicion valor de {@code posicion}
      * @param visionBase valor de {@code visionBase}
     */
    public Marine(String nombre, Posicion posicion, Mochila mochila, int visionBase) {
        super(nombre, 120, 90, posicion, mochila, visionBase);
    }

    @Override
    /**
     * Ejecuta aplicarModificadorDanio.
     */
    protected int aplicarModificadorDanio(int base, Personaje objetivo) {
        int distancia = posicion.distanciaManhattan(objetivo.getPosicion());
        if (distancia <= 1) {
            return base * 2;
        }
        if (distancia > 2) {
            return Math.max(1, (int) Math.ceil(base * 0.05));
        }
        return base;
    }

    @Override
    /**
     * Estima el coste de movimiento propio del marine.
     */
    public int estimarCosteMovimiento() {
        return (int) Math.ceil(super.estimarCosteMovimiento() * 1.2);
    }

    @Override
    /**
     * Ejecuta equiparArma.
     */
    protected void equiparArma(Arma arma) throws AccionInvalidaException {
        long dosManos = armasEquipadas.stream().filter(Arma::isDosManos).count();
        if (arma.isDosManos() && dosManos >= 2) {
            throw new AccionInvalidaException("El marine ya lleva dos armas a dos manos.");
        }
        if (!arma.isDosManos()) {
            super.equiparArma(arma);
            return;
        }
        armasEquipadas.add(arma);
    }

    @Override
    /**
     * Ejecuta coger.
     */
    public void coger(Objeto objeto) throws AccionInvalidaException {
        super.coger(objeto);
    }
}

