package com.legendoftecla.model.items;

import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;


/**
 * Representa la entidad ToritoRojo del juego.
 */
public final class ToritoRojo extends Objeto {
    private int energiaTurno;

    /**
     * Ejecuta ToritoRojo.
      * @param descripcion valor de {@code descripcion}
      * @param energiaTurno valor de {@code energiaTurno}
      * @param nombre valor de {@code nombre}
      * @param peso valor de {@code peso}
     */
    public ToritoRojo(String nombre, String descripcion, double peso, int energiaTurno) {
        super(nombre, descripcion, peso);
        setEnergiaTurno(energiaTurno);
    }

    /**
     * Obtiene el valor de {@code EnergiaTurno}.
      * @return resultado de la operacion
     */
    public int getEnergiaTurno() {
        return energiaTurno;
    }

    /** @param energiaTurno energia positiva y acotada */
    public void setEnergiaTurno(int energiaTurno) {
        this.energiaTurno = Validaciones.enteroEntre(
                energiaTurno, 1, Limites.ESTADISTICA, "Energia recuperada");
    }

    @Override
    /**
     * Ejecuta usar.
     */
    public void usar(Personaje personaje) {
        Validaciones.noNulo(personaje, "Personaje");
        personaje.recuperarEnergia(energiaTurno);
        personaje.aplicarPenalizacionEnergiaSiguienteTurno(0.10);
    }
}

