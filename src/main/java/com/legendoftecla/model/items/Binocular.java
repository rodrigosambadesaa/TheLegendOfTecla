package com.legendoftecla.model.items;

import com.legendoftecla.model.characters.Personaje;


/**
 * Representa la entidad Binocular del juego.
 */
public final class Binocular extends Objeto {
    private final int rango;

    /**
     * Ejecuta Binocular.
      * @param descripcion valor de {@code descripcion}
      * @param nombre valor de {@code nombre}
      * @param peso valor de {@code peso}
      * @param rango valor de {@code rango}
     */
    public Binocular(String nombre, String descripcion, double peso, int rango) {
        super(nombre, descripcion, peso);
        this.rango = rango;
    }

    /**
     * Ejecuta getRango.
      * @return resultado de la operacion
     */
    public int getRango() {
        return rango;
    }

    @Override
    /**
     * Ejecuta usar.
     */
    public void usar(Personaje personaje) {
        personaje.aumentarVisionTemporal(rango);
    }
}

