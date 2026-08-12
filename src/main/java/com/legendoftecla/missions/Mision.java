package com.legendoftecla.missions;
import com.legendoftecla.model.world.Juego;
import java.util.List;
/** Agregado de objetivo principal, secundarios y recompensas. */
public final class Mision {
    private final String id;
    private final String nombre;
    private final ObjetivoMision principal;
    private final List<ObjetivoMision> secundarios;
    private final List<String> recompensas;
    public Mision(String id, String nombre, ObjetivoMision principal,
            List<ObjetivoMision> secundarios, List<String> recompensas) {
        if (id == null || id.isBlank() || nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("ID y nombre de mision obligatorios");
        }
        this.id = id;
        this.nombre = nombre;
        this.principal = java.util.Objects.requireNonNull(principal, "Principal");
        this.secundarios = List.copyOf(java.util.Objects.requireNonNull(
                secundarios, "Secundarios"));
        this.recompensas = List.copyOf(java.util.Objects.requireNonNull(
                recompensas, "Recompensas"));
    }
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public ObjetivoMision getPrincipal() { return principal; }
    public List<ObjetivoMision> getSecundarios() { return secundarios; }
    public List<String> getRecompensas() { return recompensas; }
    public boolean completada(Juego juego) { return principal.completado(juego); }
    public long secundariosCompletados(Juego juego) {
        return secundarios.stream().filter(objetivo -> objetivo.completado(juego)).count();
    }
}
