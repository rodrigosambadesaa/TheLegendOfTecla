package com.legendoftecla.model.items;

import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;

/** Linterna reutilizable que ilumina las zonas oscuras cercanas. */
public final class Linterna extends Objeto {
    private int alcance;

    public Linterna(String nombre, String descripcion, double peso, int alcance) {
        super(nombre, descripcion, peso);
        setAlcance(alcance);
    }

    public int getAlcance() { return alcance; }

    public void setAlcance(int alcance) {
        this.alcance = Validaciones.enteroEntre(alcance, 1, Limites.ESTADISTICA, "Alcance de linterna");
    }

    @Override
    public void usar(Personaje personaje) {
        Personaje validado = Validaciones.noNulo(personaje, "Personaje");
        validado.setLinternaActiva(!validado.isLinternaActiva());
        validado.setAlcanceLinterna(alcance);
    }

    @Override
    public boolean isConsumible() { return false; }
}
