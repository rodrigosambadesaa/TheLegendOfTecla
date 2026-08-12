package com.legendoftecla.missions;
import com.legendoftecla.progression.ProgresionPersonaje;
import java.util.List;
/** Secuencia opcional de misiones con progreso persistente. */
public final class Campana {
    private final String id;
    private final List<Mision> misiones;
    private final ProgresionPersonaje progresion;
    private int indice;
    public Campana(String id, List<Mision> misiones, ProgresionPersonaje progresion) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("ID obligatorio");
        if (misiones == null || misiones.isEmpty()) {
            throw new IllegalArgumentException("Campana vacia");
        }
        this.id = id;
        this.misiones = List.copyOf(misiones);
        this.progresion = java.util.Objects.requireNonNull(progresion, "Progresion");
    }
    public String getId() { return id; }
    public Mision misionActual() { return indice < misiones.size() ? misiones.get(indice) : null; }
    public boolean avanzar() {
        if (indice < misiones.size()) indice++;
        return completada();
    }
    public boolean completada() { return indice >= misiones.size(); }
    public int getIndice() { return indice; }
    /** Restaura una posicion validada dentro de la secuencia, incluido el final. */
    public void restaurarIndice(int indice) {
        if (indice < 0 || indice > misiones.size()) {
            throw new IllegalArgumentException("Indice de campana invalido");
        }
        this.indice = indice;
    }
    public ProgresionPersonaje getProgresion() { return progresion; }
}
