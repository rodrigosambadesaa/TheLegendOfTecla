package com.legendoftecla.model.elements;

import java.util.Objects;

/** Implementacion comun de identidad y resistencia estructural. */
public abstract class ElementoBase implements ElementoMapa {
    private final String id;
    private final boolean destructible;
    private int resistencia;

    /** @param id identidad no vacia @param destructible permite dano @param resistencia vida */
    protected ElementoBase(String id, boolean destructible, int resistencia) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("ID obligatorio");
        if (resistencia < 1) throw new IllegalArgumentException("Resistencia invalida");
        this.id = id;
        this.destructible = destructible;
        this.resistencia = resistencia;
    }

    public final String getId() { return id; }
    /** @return resistencia restante */
    public final int getResistencia() { return resistencia; }
    /** @return si admite dano */
    public final boolean isDestructible() { return destructible; }

    @Override
    public void recibirDanio(int cantidad) {
        if (cantidad < 0) throw new IllegalArgumentException("Dano negativo");
        if (destructible) resistencia = Math.max(0, resistencia - cantidad);
    }

    @Override
    public boolean estaDestruido() { return resistencia == 0; }

    @Override
    public final boolean equals(Object otro) {
        return otro instanceof ElementoMapa elemento && id.equals(elemento.getId());
    }

    @Override
    public final int hashCode() { return Objects.hash(id); }
}
