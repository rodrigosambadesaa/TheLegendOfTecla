package com.legendoftecla.model.characters;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.ai.*;
/** Jefe coordinador que invoca y potencia refuerzos. */
public final class CommanderPrime extends Jefe implements EnemigoTactico {
    public CommanderPrime(String nombre, Posicion posicion, Mochila mochila, int vision) {
        super(nombre, 320, posicion, mochila, vision);
    }
    public String habilidadActual() {
        return switch (getFase()) {
            case UNO -> "orden tactica";
            case DOS -> "invocar refuerzos";
            case TRES -> "fortificar entorno";
            case FINAL -> "ofensiva total";
        };
    }
    public AccionIA decidirTactica(ContextoIA contexto) {
        TipoAccionIA accion = !contexto.coordinacionActiva()
                ? TipoAccionIA.ATACAR : switch (getFase()) {
            case UNO, TRES -> TipoAccionIA.PROTEGER;
            case DOS -> TipoAccionIA.ALERTAR;
            case FINAL -> TipoAccionIA.ATACAR;
        };
        return new AccionIA(accion, contexto.posicionObjetivo(),
                habilidadActual());
    }
}
