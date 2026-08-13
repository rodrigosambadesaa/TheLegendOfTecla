package com.legendoftecla.model.characters;
import com.legendoftecla.ai.*;
import com.legendoftecla.model.world.Posicion;
/** Sanitario enemigo que prioriza heridos y retaguardia. */
public final class Medic extends Enemigo implements EnemigoTactico {
    private static final int ALCANCE_CURACION = 2;
    private static final int POTENCIA_CURACION = 20;
    public Medic(String nombre, Posicion posicion, Mochila mochila, int vision) {
        super(nombre, 80, 130, posicion, mochila, vision);
    }
    public AccionIA decidirTactica(ContextoIA contexto) {
        TipoAccionIA tipo;
        if (contexto.aliadoHerido()) {
            tipo = TipoAccionIA.CURAR;
        } else if (contexto.distanciaJugador() < 2) {
            tipo = TipoAccionIA.ALEJARSE;
        } else {
            tipo = TipoAccionIA.ATACAR;
        }
        return new AccionIA(tipo, contexto.posicionObjetivo(),
                "apoyo sanitario");
    }
    /** @return distancia maxima a la que puede asistir a otro enemigo */
    public int getAlcanceCuracion() { return ALCANCE_CURACION; }
    /** @return salud restaurada por accion de apoyo */
    public int getPotenciaCuracion() { return POTENCIA_CURACION; }
}
