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
     */
    public Posicion(int fila, int columna) {
        this.fila = fila;
        this.columna = columna;
    }

    /**
     * Ejecuta getFila.
     */
    public int getFila() {
        return fila;
    }

    /**
     * Ejecuta getColumna.
     */
    public int getColumna() {
        return columna;
    }

    /**
     * Ejecuta mover.
     */
    public Posicion mover(Direccion direccion) {
        return new Posicion(fila + direccion.getDeltaFila(), columna + direccion.getDeltaColumna());
    }

    /**
     * Ejecuta distanciaManhattan.
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

