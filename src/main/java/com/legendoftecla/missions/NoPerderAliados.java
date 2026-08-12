package com.legendoftecla.missions;
import com.legendoftecla.model.world.Juego;
/** Condicion secundaria que exige que ningun aliado registrado muera. */
public final class NoPerderAliados implements ObjetivoMision {
    public boolean completado(Juego juego) {
        return juego.getAliadosRegistrados().stream().allMatch(a -> a.getSalud() > 0);
    }
    public String descripcion() { return "No perder aliados"; }
}
