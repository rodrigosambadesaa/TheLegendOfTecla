package com.legendoftecla.missions;
import com.legendoftecla.model.world.Juego;
/** Escolta de un aliado vivo hasta la salida. */
public final class EscoltarPersonaje implements ObjetivoMision {
    private final String nombre;
    public EscoltarPersonaje(String nombre) { this.nombre = nombre; }
    public boolean completado(Juego juego) {
        return juego.getAliadosExtraidosDetalle().stream()
                .anyMatch(a -> a.getSalud() > 0 && a.getNombre().equalsIgnoreCase(nombre));
    }
    public String descripcion() { return "Escoltar a " + nombre; }
    public String getNombre() { return nombre; }
}
