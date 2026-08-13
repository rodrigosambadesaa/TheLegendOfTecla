package com.legendoftecla.model.characters;
import com.legendoftecla.ai.*;
import com.legendoftecla.model.world.Posicion;
/** Coordinador que protege y mejora una escuadra cercana. */
public class Commander extends Enemigo implements EnemigoTactico {
    private boolean ordenEmitida;

    public Commander(String nombre, Posicion posicion, Mochila mochila, int vision) {
        super(nombre, 125, 120, posicion, mochila, vision);
    }
    public double bonificacionAliados() { return 1.15; }
    public AccionIA decidirTactica(ContextoIA contexto) {
        TipoAccionIA tipo = !contexto.coordinacionActiva() || ordenEmitida
                ? TipoAccionIA.ATACAR : TipoAccionIA.PROTEGER;
        ordenEmitida = true;
        return new AccionIA(tipo,
                contexto.posicionObjetivo(), "coordinacion de escuadra");
    }
}
