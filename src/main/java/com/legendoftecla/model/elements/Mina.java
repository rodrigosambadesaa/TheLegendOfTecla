package com.legendoftecla.model.elements;
/** Mina explosiva, opcionalmente remota. */
public final class Mina extends Trampa {
    public Mina(String id, int dano, int radio, boolean remota) {
        super(id, 7, 8, dano, radio, remota);
    }
}
