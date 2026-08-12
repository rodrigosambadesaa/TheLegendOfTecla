package com.legendoftecla.missions;
import com.legendoftecla.model.world.Juego;
/** Objetivo de neutralizar un enemigo identificado. */
public class EliminarEnemigo implements ObjetivoMision {
    private final String nombre;
    public EliminarEnemigo(String nombre) { this.nombre = nombre; }
    public boolean completado(Juego juego) {
        return juego.getEnemigos().stream().filter(e -> e.getNombre().equalsIgnoreCase(nombre))
                .allMatch(e -> e.getSalud() <= 0);
    }
    public String descripcion() { return "Eliminar a " + nombre; }
    public String getNombre() { return nombre; }
}
