package com.legendoftecla.model.items;

import com.legendoftecla.model.characters.Personaje;


/**
 * Representa la entidad Binocular del juego.
 */
public final class Binocular extends Objeto {
    private final int rango;

    /**
     * Ejecuta Binocular.
     */
    public Binocular(String nombre, String descripcion, double peso, int rango) {
        super(nombre, descripcion, peso);
        this.rango = rango;
    }

    /**
     * Ejecuta getRango.
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

