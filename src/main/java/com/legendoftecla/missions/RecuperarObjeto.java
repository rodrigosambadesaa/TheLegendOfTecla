package com.legendoftecla.missions;
import com.legendoftecla.model.world.Juego;
/** Recuperacion de un objeto en el inventario del jugador. */
public final class RecuperarObjeto implements ObjetivoMision {
    private final String nombre;
    public RecuperarObjeto(String nombre) { this.nombre = nombre; }
    public boolean completado(Juego juego) {
        return juego.getJugador().getMochila().getObjetos().stream()
                .anyMatch(o -> o.getNombre().equalsIgnoreCase(nombre));
    }
    public String descripcion() { return "Recuperar " + nombre; }
    public String getNombre() { return nombre; }
}
