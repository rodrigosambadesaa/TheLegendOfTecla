package com.legendoftecla.model.items;

import com.legendoftecla.model.characters.Personaje;


/**
 * Representa la entidad ToritoRojo del juego.
 */
public final class ToritoRojo extends Objeto {
    private final int energiaTurno;

    /**
     * Ejecuta ToritoRojo.
      * @param descripcion valor de {@code descripcion}
      * @param energiaTurno valor de {@code energiaTurno}
      * @param nombre valor de {@code nombre}
      * @param peso valor de {@code peso}
     */
    public ToritoRojo(String nombre, String descripcion, double peso, int energiaTurno) {
        super(nombre, descripcion, peso);
        this.energiaTurno = energiaTurno;
    }

    /**
     * Obtiene el valor de {@code EnergiaTurno}.
      * @return resultado de la operacion
     */
    public int getEnergiaTurno() {
        return energiaTurno;
    }

    @Override
    /**
     * Ejecuta usar.
     */
    public void usar(Personaje personaje) {
        personaje.recuperarEnergia(energiaTurno);
        personaje.aplicarPenalizacionEnergiaSiguienteTurno(0.10);
    }
}

