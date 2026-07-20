package com.legendoftecla.model.items;

import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;


/**
 * Representa la entidad Binocular del juego.
 */
public final class Binocular extends Objeto {
    private int rango;

    /**
     * Ejecuta Binocular.
      * @param descripcion valor de {@code descripcion}
      * @param nombre valor de {@code nombre}
      * @param peso valor de {@code peso}
      * @param rango valor de {@code rango}
     */
    public Binocular(String nombre, String descripcion, double peso, int rango) {
        super(nombre, descripcion, peso);
        setRango(rango);
    }

    /**
     * Ejecuta getRango.
      * @return resultado de la operacion
     */
    public int getRango() {
        return rango;
    }

    /** @param rango alcance positivo y acotado */
    public void setRango(int rango) {
        this.rango = Validaciones.enteroEntre(
                rango, 1, Limites.ESTADISTICA, "Rango del binocular");
    }

    @Override
    /**
     * Ejecuta usar.
     */
    public void usar(Personaje personaje) {
        Validaciones.noNulo(personaje, "Personaje");
        personaje.aumentarVisionTemporal(rango);
    }
}

