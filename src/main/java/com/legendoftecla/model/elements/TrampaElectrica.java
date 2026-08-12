package com.legendoftecla.model.elements;
import com.legendoftecla.effects.Aturdido;
import com.legendoftecla.model.characters.Personaje;
/** Trampa electrica que aturde. */
public final class TrampaElectrica extends Trampa {
    public TrampaElectrica(String id) { super(id, 5, 9, 3, 0, false); }
    protected void aplicarEstado(Personaje victima) { victima.getEstados().aplicar(new Aturdido()); }
}
