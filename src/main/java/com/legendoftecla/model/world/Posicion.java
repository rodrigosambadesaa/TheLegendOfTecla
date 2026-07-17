package com.legendoftecla.model.world;

import java.util.Objects;


/**
 * Representa la entidad Posicion del juego.
 */
public class Posicion {
    private final int fila;
    private final int columna;

    /**
     * Ejecuta Posicion.
      * @param columna valor de {@code columna}
      * @param fila valor de {@code fila}
     */
    public Posicion(int fila, int columna) {
        this.fila = fila;
        this.columna = columna;
    }

    /**
     * Ejecuta getFila.
      * @return resultado de la operacion
     */
    public int getFila() {
        return fila;
    }

    /**
     * Ejecuta getColumna.
      * @return resultado de la operacion
     */
    public int getColumna() {
        return columna;
    }

    /**
     * Ejecuta mover.
      * @param direccion valor de {@code direccion}
      * @return resultado de la operacion
     */
    public Posicion mover(Direccion direccion) {
        return new Posicion(fila + direccion.getDeltaFila(), columna + direccion.getDeltaColumna());
    }

    /**
     * Ejecuta distanciaManhattan.
      * @param otra valor de {@code otra}
      * @return resultado de la operacion
     */
    public int distanciaManhattan(Posicion otra) {
        return Math.abs(fila - otra.fila) + Math.abs(columna - otra.columna);
    }

    @Override
    /**
     * Ejecuta equals.
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Posicion posicion)) {
            return false;
        }
        return fila == posicion.fila && columna == posicion.columna;
    }

    @Override
    /**
     * Ejecuta hashCode.
     */
    public int hashCode() {
        return Objects.hash(fila, columna);
    }

    @Override
    /**
     * Ejecuta toString.
     */
    public String toString() {
        return "(" + fila + "," + columna + ")";
    }
}

