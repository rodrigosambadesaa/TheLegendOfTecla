package com.legendoftecla.model.items;

import com.legendoftecla.exceptions.ObjetoNoUsableException;
import com.legendoftecla.model.characters.Personaje;


/**
 * Representa la entidad Explosivo del juego.
 */
public final class Explosivo extends Objeto {
    /**
     * Ejecuta Explosivo.
     */
    public Explosivo(String nombre, String descripcion, double peso) {
        super(nombre, descripcion, peso);
    }

    @Override
    /**
     * Ejecuta usar.
     */
    public void usar(Personaje personaje) throws ObjetoNoUsableException {
        throw new ObjetoNoUsableException("El explosivo debe lanzarse como parte de una accion de combate.");
    }
}

