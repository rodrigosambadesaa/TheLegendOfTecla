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
      * @param descripcion valor de {@code descripcion}
      * @param nombre valor de {@code nombre}
      * @param peso valor de {@code peso}
     */
    protected Objeto(String nombre, String descripcion, double peso) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.peso = peso;
    }

    /**
     * Ejecuta getNombre.
      * @return resultado de la operacion
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Ejecuta getDescripcion.
      * @return resultado de la operacion
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Ejecuta getPeso.
      * @return resultado de la operacion
     */
    public double getPeso() {
        return peso;
    }

    /**
     * Ejecuta usar.
      * @param personaje valor de {@code personaje}
      * @throws com.legendoftecla.exceptions.JuegoException si la operacion no puede completarse
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

