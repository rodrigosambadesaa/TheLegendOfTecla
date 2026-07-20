package com.legendoftecla.model.world;

import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;

import java.util.Objects;

/** Tamano de mapa mutable exclusivamente mediante setters delimitados. */
public final class DimensionesMapa {
    private int filas;
    private int columnas;

    /**
     * Crea unas dimensiones validas.
     *
     * @param filas filas
     * @param columnas columnas
     */
    public DimensionesMapa(int filas, int columnas) {
        setFilas(filas);
        setColumnas(columnas);
    }

    /** @return filas */
    public int getFilas() {
        return filas;
    }

    /** @param filas filas entre los limites del mapa */
    public void setFilas(int filas) {
        this.filas = Validaciones.enteroEntre(
                filas, Limites.MAPA_MINIMO, Limites.MAPA_MAXIMO, "Filas");
    }

    /** @return columnas */
    public int getColumnas() {
        return columnas;
    }

    /** @param columnas columnas entre los limites del mapa */
    public void setColumnas(int columnas) {
        this.columnas = Validaciones.enteroEntre(
                columnas, Limites.MAPA_MINIMO, Limites.MAPA_MAXIMO, "Columnas");
    }

    /** @return filas, conservando el acceso anterior del record */
    public int filas() {
        return getFilas();
    }

    /** @return columnas, conservando el acceso anterior del record */
    public int columnas() {
        return getColumnas();
    }

    @Override
    public boolean equals(Object otro) {
        return otro instanceof DimensionesMapa dimensiones
                && filas == dimensiones.filas && columnas == dimensiones.columnas;
    }

    @Override
    public int hashCode() {
        return Objects.hash(filas, columnas);
    }

    @Override
    public String toString() {
        return filas + "x" + columnas;
    }
}
