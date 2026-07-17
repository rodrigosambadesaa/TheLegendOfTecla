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
     */
    public Arma(String nombre, String descripcion, double peso, int danio, boolean dosManos) {
        super(nombre, descripcion, peso);
        this.danio = danio;
        this.dosManos = dosManos;
    }

    /**
     * Ejecuta getDanio.
     */
    public int getDanio() {
        return danio;
    }

    /**
     * Ejecuta isDosManos.
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

