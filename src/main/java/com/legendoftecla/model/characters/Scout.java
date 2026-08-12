package com.legendoftecla.model.characters;
import com.legendoftecla.ai.*;
import com.legendoftecla.model.world.Posicion;
/** Explorador movil que comunica contactos. */
public final class Scout extends Enemigo implements EnemigoTactico {
    private boolean contactoComunicado;

    public Scout(String nombre, Posicion posicion, Mochila mochila, int vision) {
        super(nombre, 65, 180, posicion, mochila, vision);
        setRangoAudicion(10);
    }
    public AccionIA decidirTactica(ContextoIA contexto) {
        TipoAccionIA tipo;
        if (!contexto.veJugador()) {
            tipo = TipoAccionIA.PATRULLAR;
        } else if (!contactoComunicado) {
            contactoComunicado = true;
            tipo = TipoAccionIA.ALERTAR;
        } else {
            tipo = TipoAccionIA.ATACAR;
        }
        return new AccionIA(tipo,
                contexto.juego().getJugador().getPosicion(), "reconocimiento rapido");
    }
}
