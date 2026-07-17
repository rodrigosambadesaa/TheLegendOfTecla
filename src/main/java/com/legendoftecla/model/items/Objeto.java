package com.legendoftecla.model.items;

import com.legendoftecla.exceptions.JuegoException;
import com.legendoftecla.model.characters.Personaje;


/**
 * Representa la entidad Objeto del juego.
 */
public abstract class Objeto {
    private final String nombre;
    private final String descripcion;
    private final double peso;

    /**
     * Ejecuta Objeto.
     */
    protected Objeto(String nombre, String descripcion, double peso) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.peso = peso;
    }

    /**
     * Ejecuta getNombre.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Ejecuta getDescripcion.
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Ejecuta getPeso.
     */
    public double getPeso() {
        return peso;
    }

    /**
     * Ejecuta usar.
     */
    public abstract void usar(Personaje personaje) throws JuegoException;

    @Override
    /**
     * Ejecuta toString.
     */
    public String toString() {
        return nombre + " (" + descripcion + ", " + peso + " kg)";
    }
}

