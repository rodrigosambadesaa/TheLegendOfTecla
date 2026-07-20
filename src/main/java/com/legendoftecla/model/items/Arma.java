package com.legendoftecla.model.items;

import com.legendoftecla.exceptions.ObjetoNoUsableException;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;


/**
 * Representa la entidad Arma del juego.
 */
public final class Arma extends Objeto {
    private int danio;
    private boolean dosManos;

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
        setDanio(danio);
        setDosManos(dosManos);
    }

    /**
     * Ejecuta getDanio.
      * @return resultado de la operacion
     */
    public int getDanio() {
        return danio;
    }

    /** @param danio dano positivo y acotado */
    public void setDanio(int danio) {
        this.danio = Validaciones.enteroEntre(danio, 1, Limites.ESTADISTICA, "Dano del arma");
    }

    /**
     * Ejecuta isDosManos.
      * @return resultado de la operacion
     */
    public boolean isDosManos() {
        return dosManos;
    }

    /** @param dosManos estado solicitado */
    public void setDosManos(boolean dosManos) {
        this.dosManos = dosManos;
    }

    @Override
    /**
     * Ejecuta usar.
     */
    public void usar(Personaje personaje) throws ObjetoNoUsableException {
        throw new ObjetoNoUsableException("Las armas no se usan directamente; se equipan.");
    }
}

