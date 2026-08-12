package com.legendoftecla.missions;
import com.legendoftecla.model.world.Juego;
/** Objetivo de neutralizar un enemigo identificado. */
public class EliminarEnemigo implements ObjetivoMision {
    private final String nombre;
    public EliminarEnemigo(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Nombre de enemigo obligatorio");
        }
        this.nombre = nombre;
    }
    public boolean completado(Juego juego) {
        java.util.List<com.legendoftecla.model.characters.Enemigo> objetivos =
                juego.getEnemigos().stream()
                        .filter(e -> e.getNombre().equalsIgnoreCase(nombre)).toList();
        return !objetivos.isEmpty()
                && objetivos.stream().allMatch(e -> e.getSalud() <= 0);
    }
    public String descripcion() { return "Eliminar a " + nombre; }
    public String getNombre() { return nombre; }
}
