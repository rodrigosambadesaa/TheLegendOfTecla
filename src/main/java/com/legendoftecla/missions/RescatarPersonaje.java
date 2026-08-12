package com.legendoftecla.missions;
import com.legendoftecla.model.world.Juego;
/** Rescate por evacuacion de un aliado concreto. */
public final class RescatarPersonaje implements ObjetivoMision {
    private final String nombre;
    public RescatarPersonaje(String nombre) { this.nombre = nombre; }
    public boolean completado(Juego juego) {
        return juego.getAliadosExtraidosDetalle().stream()
                .anyMatch(a -> a.getNombre().equalsIgnoreCase(nombre));
    }
    public String descripcion() { return "Rescatar a " + nombre; }
    public String getNombre() { return nombre; }
}
