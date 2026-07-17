package com.legendoftecla.model.items;

import com.legendoftecla.exceptions.ObjetoNoUsableException;
import com.legendoftecla.model.characters.Personaje;


/**
 * Representa la entidad Arma del juego.
 */
public final class Arma extends Objeto {
    private final int danio;
    private final boolean dosManos;

    /**
     * Ejecuta Arma.
      * @param danio valor de {@code danio}
      * @param descripcion valor de {@code descripcion}
      * @param dosManos valor de {@code dosManos}
      * @param nombre valor de {@code nombre}
      * @param peso valor de {@code peso}
     */
    public Arma(String nombre, String descripcion, double peso, int danio, boolean dosManos) {
        super(nombre, descripcion, peso);
        this.danio = danio;
        this.dosManos = dosManos;
    }

    /**
     * Ejecuta getDanio.
      * @return resultado de la operacion
     */
    public int getDanio() {
        return danio;
    }

    /**
     * Ejecuta isDosManos.
      * @return resultado de la operacion
     */
    public boolean isDosManos() {
        return dosManos;
    }

    @Override
    /**
     * Ejecuta usar.
     */
    public void usar(Personaje personaje) throws ObjetoNoUsableException {
        throw new ObjetoNoUsableException("Las armas no se usan directamente; se equipan.");
    }
}

