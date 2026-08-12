package com.legendoftecla.model.characters;
import com.legendoftecla.ai.*;
import com.legendoftecla.model.world.Posicion;
/** Especialista en negar zonas mediante fuego. */
public final class Pyro extends Enemigo implements EnemigoTactico {
    public Pyro(String nombre, Posicion posicion, Mochila mochila, int vision) {
        super(nombre, 105, 100, posicion, mochila, vision);
    }
    public AccionIA decidirTactica(ContextoIA contexto) {
        boolean puedeIncendiar = !contexto.juego().getMapa().getCelda(
                contexto.juego().getJugador().getPosicion()).estaArdiendo();
        return new AccionIA(puedeIncendiar ? TipoAccionIA.INCENDIAR
                : TipoAccionIA.ATACAR,
                contexto.juego().getJugador().getPosicion(), "control incendiario");
    }
}
