package com.legendoftecla.missions;
import com.legendoftecla.model.world.Juego;
import java.util.function.BooleanSupplier;
/** Objetivo observable que permanece valido mientras no se haya disparado. */
public final class CompletarSinDisparar implements ObjetivoMision {
    private final BooleanSupplier seHaDisparado;
    private final ObjetivoMision finalizacion;
    public CompletarSinDisparar(BooleanSupplier seHaDisparado, ObjetivoMision finalizacion) {
        this.seHaDisparado = java.util.Objects.requireNonNull(
                seHaDisparado, "Contador de disparos");
        this.finalizacion = java.util.Objects.requireNonNull(
                finalizacion, "Finalizacion");
    }
    /** Variante persistible que consulta la proyeccion de disparos de la partida. */
    public CompletarSinDisparar(ObjetivoMision finalizacion) {
        this.seHaDisparado = null;
        this.finalizacion = java.util.Objects.requireNonNull(
                finalizacion, "Finalizacion");
    }
    public boolean completado(Juego juego) {
        boolean disparado = seHaDisparado == null
                ? juego.getEstadisticas().getDisparos() > 0 : seHaDisparado.getAsBoolean();
        return !disparado && finalizacion.completado(juego);
    }
    public String descripcion() { return "Completar sin disparar"; }
    public ObjetivoMision getFinalizacion() { return finalizacion; }
}
