package com.legendoftecla.model.characters;
import com.legendoftecla.ai.*;
import com.legendoftecla.model.world.Posicion;
/** Enemigo robusto que fuerza combate cercano. */
public final class Berserker extends Enemigo implements EnemigoTactico {
    public Berserker(String nombre, Posicion posicion, Mochila mochila, int vision) {
        super(nombre, 170, 110, posicion, mochila, vision);
    }
    public AccionIA decidirTactica(ContextoIA contexto) {
        return new AccionIA(contexto.distanciaJugador() <= 1 ? TipoAccionIA.ATACAR
                : TipoAccionIA.ACERCARSE, contexto.juego().getJugador().getPosicion(), "furia cuerpo a cuerpo");
    }
}
