package com.legendoftecla.model.characters;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.ai.*;
/** Jefe incendiario que altera progresivamente el escenario. */
public final class PyroOverlord extends Jefe implements EnemigoTactico {
    public PyroOverlord(String nombre, Posicion posicion, Mochila mochila, int vision) {
        super(nombre, 360, posicion, mochila, vision);
    }
    public String habilidadActual() {
        return switch (getFase()) {
            case UNO -> "llamarada";
            case DOS -> "suelo ardiente";
            case TRES -> "explosion combustible";
            case FINAL -> "infierno final";
        };
    }
    public AccionIA decidirTactica(ContextoIA contexto) {
        boolean objetivoArdiendo = contexto.juego().getMapa().getCelda(
                contexto.juego().getJugador().getPosicion()).estaArdiendo();
        return new AccionIA(objetivoArdiendo ? TipoAccionIA.ATACAR
                : TipoAccionIA.INCENDIAR,
                contexto.juego().getJugador().getPosicion(), habilidadActual());
    }
}
