package com.legendoftecla.model.items;

import com.legendoftecla.exceptions.ObjetoNoUsableException;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;


/**
 * Representa la entidad Armadura del juego.
 */
public final class Armadura extends Objeto {
    private int defensa;
    private int bonusSalud;
    private int bonusEnergia;

    /**
     * Ejecuta Armadura.
      * @param bonusEnergia valor de {@code bonusEnergia}
      * @param bonusSalud valor de {@code bonusSalud}
      * @param defensa valor de {@code defensa}
      * @param descripcion valor de {@code descripcion}
      * @param nombre valor de {@code nombre}
      * @param peso valor de {@code peso}
     */
    public Armadura(String nombre, String descripcion, double peso, int defensa, int bonusSalud, int bonusEnergia) {
        super(nombre, descripcion, peso);
        setDefensa(defensa);
        setBonusSalud(bonusSalud);
        setBonusEnergia(bonusEnergia);
    }

    /**
     * Ejecuta getDefensa.
      * @return resultado de la operacion
     */
    public int getDefensa() {
        return defensa;
    }

    /** @param defensa defensa no negativa y acotada */
    public void setDefensa(int defensa) {
        this.defensa = Validaciones.enteroEntre(defensa, 0, Limites.ESTADISTICA, "Defensa");
    }

    /**
     * Ejecuta getBonusSalud.
      * @return resultado de la operacion
     */
    public int getBonusSalud() {
        return bonusSalud;
    }

    /** @param bonusSalud bonificacion no negativa y acotada */
    public void setBonusSalud(int bonusSalud) {
        this.bonusSalud = Validaciones.enteroEntre(
                bonusSalud, 0, Limites.ESTADISTICA, "Bonificacion de salud");
    }

    /**
     * Ejecuta getBonusEnergia.
      * @return resultado de la operacion
     */
    public int getBonusEnergia() {
        return bonusEnergia;
    }

    /** @param bonusEnergia bonificacion no negativa y acotada */
    public void setBonusEnergia(int bonusEnergia) {
        this.bonusEnergia = Validaciones.enteroEntre(
                bonusEnergia, 0, Limites.ESTADISTICA, "Bonificacion de energia");
    }

    @Override
    /**
     * Ejecuta usar.
     */
    public void usar(Personaje personaje) throws ObjetoNoUsableException {
        throw new ObjetoNoUsableException("Las armaduras no se usan directamente; se equipan.");
    }
}

