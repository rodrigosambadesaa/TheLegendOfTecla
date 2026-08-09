package com.legendoftecla.model.items;

import com.legendoftecla.exceptions.JuegoException;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;


/**
 * Representa la entidad Objeto del juego.
 */
public abstract class Objeto {
    private String nombre;
    private String descripcion;
    private double peso;

    /**
     * Ejecuta Objeto.
      * @param descripcion valor de {@code descripcion}
      * @param nombre valor de {@code nombre}
      * @param peso valor de {@code peso}
     */
    protected Objeto(String nombre, String descripcion, double peso) {
        setNombre(nombre);
        setDescripcion(descripcion);
        setPeso(peso);
    }

    /**
     * Ejecuta getNombre.
      * @return resultado de la operacion
     */
    public String getNombre() {
        return nombre;
    }

    /** @param nombre nombre obligatorio y acotado */
    public void setNombre(String nombre) {
        this.nombre = Validaciones.textoObligatorio(
                nombre, "Nombre del objeto", Limites.TEXTO_CORTO);
    }

    /**
     * Ejecuta getDescripcion.
      * @return resultado de la operacion
     */
    public String getDescripcion() {
        return descripcion;
    }

    /** @param descripcion descripcion no nula y acotada */
    public void setDescripcion(String descripcion) {
        this.descripcion = Validaciones.texto(
                descripcion, "Descripcion del objeto", Limites.DESCRIPCION);
    }

    /**
     * Ejecuta getPeso.
      * @return resultado de la operacion
     */
    public double getPeso() {
        return peso;
    }

    /** @param peso peso finito no negativo */
    public void setPeso(double peso) {
        this.peso = Validaciones.decimalEntre(
                peso, 0.0, Limites.PESO_MAXIMO, "Peso del objeto");
    }

    /**
     * Ejecuta usar.
      * @param personaje valor de {@code personaje}
      * @throws com.legendoftecla.exceptions.JuegoException si la operacion no puede completarse
     */
    public abstract void usar(Personaje personaje) throws JuegoException;

    /**
     * Indica si el objeto desaparece de la mochila despues de usarse.
     *
     * @return {@code true} para consumibles de un solo uso
     */
    public boolean isConsumible() {
        return true;
    }

    @Override
    /**
     * Ejecuta toString.
     */
    public String toString() {
        return nombre + " (" + descripcion + ", " + peso + " kg)";
    }
}

