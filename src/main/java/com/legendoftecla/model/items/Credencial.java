package com.legendoftecla.model.items;
import com.legendoftecla.exceptions.ObjetoNoUsableException;
import com.legendoftecla.model.characters.Personaje;
/** Llave o tarjeta finita identificada para puertas. */
public final class Credencial extends Objeto {
    private final String codigo;
    public Credencial(String nombre, String descripcion, double peso, String codigo) {
        super(nombre, descripcion, peso);
        if (codigo == null || codigo.isBlank()) throw new IllegalArgumentException("Codigo obligatorio");
        this.codigo = codigo;
    }
    public String getCodigo() { return codigo; }
    @Override public void usar(Personaje personaje) throws ObjetoNoUsableException {
        throw new ObjetoNoUsableException("Usa la credencial al abrir una puerta.");
    }
}
