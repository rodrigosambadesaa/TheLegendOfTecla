package com.legendoftecla.model.characters;
import com.legendoftecla.ai.*;
import com.legendoftecla.model.world.Posicion;
/** Tirador que conserva distancia y cobertura. */
public final class Sniper extends Enemigo implements EnemigoTactico {
    public Sniper(String nombre, Posicion posicion, Mochila mochila, int vision) {
        super(nombre, 75, 100, posicion, mochila, vision);
        setRangoAudicion(8);
    }
    public AccionIA decidirTactica(ContextoIA contexto) {
        var proteccion = new com.legendoftecla.model.elements.SistemaCobertura(
                new java.util.Random(0)).proteccion(contexto.juego().getMapa(),
                        contexto.juego().getJugador().getPosicion(), getPosicion());
        TipoAccionIA tipo = contexto.distanciaJugador() < 4 ? TipoAccionIA.ALEJARSE
                : proteccion.tipo() == com.legendoftecla.model.elements.TipoCobertura.NINGUNA
                        ? TipoAccionIA.BUSCAR_COBERTURA : TipoAccionIA.ATACAR;
        return new AccionIA(tipo, contexto.juego().getJugador().getPosicion(), "distancia de francotirador");
    }
}
