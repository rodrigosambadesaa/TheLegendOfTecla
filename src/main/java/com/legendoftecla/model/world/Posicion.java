package com.legendoftecla.model.world;

import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;

import java.util.Objects;


/**
 * Representa la entidad Posicion del juego.
 */
public class Posicion {
    private int fila;
    private int columna;

    /**
     * Ejecuta Posicion.
      * @param columna valor de {@code columna}
      * @param fila valor de {@code fila}
     */
    public Posicion(int fila, int columna) {
        setFila(fila);
        setColumna(columna);
    }

    /**
     * Ejecuta getFila.
      * @return resultado de la operacion
     */
    public int getFila() {
        return fila;
    }

    /** @param fila fila acotada */
    public void setFila(int fila) {
        this.fila = Validaciones.enteroEntre(fila, -Limites.COORDENADA_ABSOLUTA,
                Limites.COORDENADA_ABSOLUTA, "Fila");
    }

    /**
     * Ejecuta getColumna.
      * @return resultado de la operacion
     */
    public int getColumna() {
        return columna;
    }

    /** @param columna columna acotada */
    public void setColumna(int columna) {
        this.columna = Validaciones.enteroEntre(columna, -Limites.COORDENADA_ABSOLUTA,
                Limites.COORDENADA_ABSOLUTA, "Columna");
    }

    /**
     * Ejecuta mover.
      * @param direccion valor de {@code direccion}
      * @return resultado de la operacion
     */
    public Posicion mover(Direccion direccion) {
        Validaciones.noNulo(direccion, "Direccion");
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
        return 31 * (31 + fila) + columna;
    }

    @Override
    /**
     * Ejecuta toString.
     */
    public String toString() {
        return "(" + fila + "," + columna + ")";
    }
}

