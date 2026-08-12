package com.legendoftecla.model.elements;
/** Obstaculo destructible que aporta cobertura orientada. */
public final class Barricada extends ElementoBase {
    private final TipoCobertura cobertura;
    private final OrientacionCobertura orientacion;
    public Barricada(String id, int resistencia, TipoCobertura cobertura,
            OrientacionCobertura orientacion) {
        super(id, true, resistencia);
        this.cobertura = java.util.Objects.requireNonNull(cobertura, "Cobertura");
        this.orientacion = java.util.Objects.requireNonNull(orientacion, "Orientacion");
    }
    public TipoCobertura getCobertura() { return estaDestruido() ? TipoCobertura.NINGUNA : cobertura; }
    public OrientacionCobertura getOrientacion() { return orientacion; }
    public boolean permitePaso() { return estaDestruido(); }
    public boolean bloqueaVision() {
        return !estaDestruido() && cobertura == TipoCobertura.COMPLETA;
    }
    public char simbolo() { return estaDestruido() ? '.' : 'B'; }
}
