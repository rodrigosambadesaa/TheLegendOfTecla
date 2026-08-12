package com.legendoftecla.model.elements;
import com.legendoftecla.effects.Quemado;
import com.legendoftecla.model.characters.Personaje;
/** Trampa que aplica quemadura. */
public final class TrampaFuego extends Trampa {
    public TrampaFuego(String id) { super(id, 6, 7, 4, 1, false); }
    protected void aplicarEstado(Personaje victima) { victima.getEstados().aplicar(new Quemado()); }
}
