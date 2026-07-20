package com.legendoftecla.model.items;

import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;


/**
 * Representa la entidad Botiquin del juego.
 */
public final class Botiquin extends Objeto {
    private int curacion;

    /**
     * Ejecuta Botiquin.
      * @param curacion valor de {@code curacion}
      * @param descripcion valor de {@code descripcion}
      * @param nombre valor de {@code nombre}
      * @param peso valor de {@code peso}
     */
    public Botiquin(String nombre, String descripcion, double peso, int curacion) {
        super(nombre, descripcion, peso);
        setCuracion(curacion);
    }

    /**
     * Obtiene el valor de {@code Curacion}.
      * @return resultado de la operacion
     */
    public int getCuracion() {
        return curacion;
    }

    /** @param curacion curacion positiva y acotada */
    public void setCuracion(int curacion) {
        this.curacion = Validaciones.enteroEntre(
                curacion, 1, Limites.ESTADISTICA, "Curacion");
    }

    @Override
    /**
     * Ejecuta usar.
     */
    public void usar(Personaje personaje) {
        Validaciones.noNulo(personaje, "Personaje");
        personaje.recuperarSalud(curacion);
    }
}

