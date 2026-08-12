package com.legendoftecla.model.elements;
import com.legendoftecla.effects.Envenenado;
import com.legendoftecla.model.characters.Personaje;
/** Trampa que envenena. */
public final class TrampaVeneno extends Trampa {
    public TrampaVeneno(String id) { super(id, 7, 7, 2, 0, false); }
    protected void aplicarEstado(Personaje victima) { victima.getEstados().aplicar(new Envenenado()); }
}
