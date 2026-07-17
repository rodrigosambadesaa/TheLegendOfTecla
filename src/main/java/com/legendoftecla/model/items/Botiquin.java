package com.legendoftecla.model.items;

import com.legendoftecla.model.characters.Personaje;


/**
 * Representa la entidad Botiquin del juego.
 */
public final class Botiquin extends Objeto {
    private final int curacion;

    /**
     * Ejecuta Botiquin.
      * @param curacion valor de {@code curacion}
      * @param descripcion valor de {@code descripcion}
      * @param nombre valor de {@code nombre}
      * @param peso valor de {@code peso}
     */
    public Botiquin(String nombre, String descripcion, double peso, int curacion) {
        super(nombre, descripcion, peso);
        this.curacion = curacion;
    }

    /**
     * Obtiene el valor de {@code Curacion}.
      * @return resultado de la operacion
     */
    public int getCuracion() {
        return curacion;
    }

    @Override
    /**
     * Ejecuta usar.
     */
    public void usar(Personaje personaje) {
        personaje.recuperarSalud(curacion);
    }
}

