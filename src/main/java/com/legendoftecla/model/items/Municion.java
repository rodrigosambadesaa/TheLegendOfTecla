package com.legendoftecla.model.items;

import com.legendoftecla.exceptions.ObjetoNoUsableException;
import com.legendoftecla.model.characters.Personaje;

/** Paquete finito y ponderado de municion. */
public final class Municion extends Objeto {
    private final TipoMunicion tipo;
    private int cantidad;

    public Municion(String nombre, double peso, TipoMunicion tipo, int cantidad) {
        super(nombre, "Municion " + tipo, peso);
        if (cantidad < 0) throw new IllegalArgumentException("Cantidad invalida");
        if (tipo == TipoMunicion.INFINITA) {
            throw new IllegalArgumentException("La municion infinita no se transporta");
        }
        this.tipo = java.util.Objects.requireNonNull(tipo, "Tipo");
        this.cantidad = cantidad;
    }

    public TipoMunicion getTipo() { return tipo; }
    public int getCantidad() { return cantidad; }

    /** Retira hasta la cantidad solicitada. */
    public int consumir(int solicitada) {
        if (solicitada < 0) throw new IllegalArgumentException("Cantidad negativa");
        int retirada = Math.min(cantidad, solicitada);
        cantidad -= retirada;
        return retirada;
    }

    @Override
    public void usar(Personaje personaje) throws ObjetoNoUsableException {
        throw new ObjetoNoUsableException("La municion se usa mediante recargar.");
    }
}
