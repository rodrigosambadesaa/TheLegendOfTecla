package com.legendoftecla.model.elements;
/** Interruptor binario referenciable por terminales, puertas o scripts. */
public final class Interruptor extends ElementoBase {
    private boolean activo;
    private final String objetivoId;
    public Interruptor(String id, boolean activo) { this(id, activo, null); }
    public Interruptor(String id, boolean activo, String objetivoId) {
        super(id, false, 1); this.activo = activo; this.objetivoId = objetivoId;
    }
    public boolean activar() { setActivo(!activo); return activo; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public String getObjetivoId() { return objetivoId; }
    public boolean permitePaso() { return true; }
    public boolean bloqueaVision() { return false; }
    public char simbolo() { return activo ? 'I' : 'i'; }
}
