package com.legendoftecla.model.elements;

/** Muro destructible que abre paso y línea de visión al quedarse sin resistencia. */
public final class ParedDebil extends ElementoBase {
    public ParedDebil(String id, int resistencia) {
        super(id, true, resistencia);
    }

    @Override public boolean permitePaso() { return estaDestruido(); }
    @Override public boolean bloqueaVision() { return !estaDestruido(); }
    @Override public char simbolo() { return estaDestruido() ? '.' : 'W'; }
}
