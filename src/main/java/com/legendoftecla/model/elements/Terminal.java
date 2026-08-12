package com.legendoftecla.model.elements;
/** Terminal con dificultad y referencia a un elemento controlado. */
public final class Terminal extends ElementoBase {
    private final int dificultad;
    private final String objetivoId;
    private boolean hackeado;
    public Terminal(String id, int dificultad, String objetivoId) {
        super(id, false, 1);
        if (dificultad < 0) throw new IllegalArgumentException("Dificultad invalida");
        this.dificultad = dificultad;
        this.objetivoId = objetivoId;
    }
    public boolean hackear(int habilidad) {
        if (habilidad >= dificultad) hackeado = true;
        return hackeado;
    }
    public int getDificultad() { return dificultad; }
    public String getObjetivoId() { return objetivoId; }
    public boolean isHackeado() { return hackeado; }
    public boolean permitePaso() { return true; }
    public boolean bloqueaVision() { return false; }
    public char simbolo() { return hackeado ? 't' : 'T'; }
}
