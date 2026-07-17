package com.legendoftecla.model.items;

import com.legendoftecla.exceptions.ObjetoNoUsableException;
import com.legendoftecla.model.characters.Personaje;


/**
 * Representa la entidad Armadura del juego.
 */
public final class Armadura extends Objeto {
    private final int defensa;
    private final int bonusSalud;
    private final int bonusEnergia;

    /**
     * Ejecuta Armadura.
     */
    public Armadura(String nombre, String descripcion, double peso, int defensa, int bonusSalud, int bonusEnergia) {
        super(nombre, descripcion, peso);
        this.defensa = defensa;
        this.bonusSalud = bonusSalud;
        this.bonusEnergia = bonusEnergia;
    }

    /**
     * Ejecuta getDefensa.
     */
    public int getDefensa() {
        return defensa;
    }

    /**
     * Ejecuta getBonusSalud.
     */
    public int getBonusSalud() {
        return bonusSalud;
    }

    /**
     * Ejecuta getBonusEnergia.
     */
    public int getBonusEnergia() {
        return bonusEnergia;
    }

    @Override
    /**
     * Ejecuta usar.
     */
    public void usar(Personaje personaje) throws ObjetoNoUsableException {
        throw new ObjetoNoUsableException("Las armaduras no se usan directamente; se equipan.");
    }
}

