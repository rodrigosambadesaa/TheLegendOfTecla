package com.legendoftecla.model.items;

import com.legendoftecla.model.characters.Personaje;


/**
 * Representa la entidad Botiquin del juego.
 */
public final class Botiquin extends Objeto {
    private final int curacion;

    /**
     * Ejecuta Botiquin.
     */
    public Botiquin(String nombre, String descripcion, double peso, int curacion) {
        super(nombre, descripcion, peso);
        this.curacion = curacion;
    }

    @Override
    /**
     * Ejecuta usar.
     */
    public void usar(Personaje personaje) {
        personaje.recuperarSalud(curacion);
    }
}

