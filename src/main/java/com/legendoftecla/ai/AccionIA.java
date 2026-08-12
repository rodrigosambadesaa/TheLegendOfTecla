package com.legendoftecla.ai;
import com.legendoftecla.model.world.Posicion;
/** Decision explicable y separada de su ejecucion. */
public record AccionIA(TipoAccionIA tipo, Posicion objetivo, String motivo) {
    public AccionIA {
        java.util.Objects.requireNonNull(tipo, "Tipo");
        motivo = motivo == null ? "" : motivo;
    }
}
