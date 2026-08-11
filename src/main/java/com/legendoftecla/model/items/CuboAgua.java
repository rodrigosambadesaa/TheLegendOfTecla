package com.legendoftecla.model.items;

import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.validation.Validaciones;

/** Cubo reutilizable que puede llenarse en una fuente y gastar su agua al apagar fuego. */
public final class CuboAgua extends Objeto {
    private boolean lleno;

    public CuboAgua(String nombre, String descripcion, double peso, boolean lleno) {
        super(nombre, descripcion, peso);
        setLleno(lleno);
    }

    public boolean isLleno() { return lleno; }

    public void setLleno(boolean lleno) { this.lleno = lleno; }

    public boolean consumirAgua() {
        if (!lleno) return false;
        setLleno(false);
        return true;
    }

    public void llenar() { setLleno(true); }

    @Override
    public void usar(Personaje personaje) {
        Validaciones.noNulo(personaje, "Personaje");
    }

    @Override
    public boolean isConsumible() { return false; }
}
