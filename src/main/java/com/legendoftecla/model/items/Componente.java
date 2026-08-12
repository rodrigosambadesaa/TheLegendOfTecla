package com.legendoftecla.model.items;
import com.legendoftecla.exceptions.ObjetoNoUsableException;
import com.legendoftecla.model.characters.Personaje;
/** Recurso simple de crafting sin uso directo. */
public final class Componente extends Objeto {
    public Componente(String nombre, String descripcion, double peso) { super(nombre, descripcion, peso); }
    @Override public void usar(Personaje personaje) throws ObjetoNoUsableException {
        throw new ObjetoNoUsableException("Este componente se utiliza para fabricar.");
    }
}
