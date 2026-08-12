package com.legendoftecla.ai;

import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.world.Juego;

import java.util.Random;

/** Fachada que separa percepcion, evaluacion y ejecucion de la IA formal. */
public final class SistemaTurnosIA {
    private final PercepcionIA percepcion = new PercepcionIA();
    private final EvaluadorTactico evaluador = new EvaluadorTactico();
    private final EjecutorAccionIA ejecutor = new EjecutorAccionIA();

    /** Decide y ejecuta exactamente una accion con el RNG inyectado. */
    public ResultadoTurnoIA ejecutar(Juego juego, Enemigo enemigo, Random random) {
        ContextoIA contexto = percepcion.percibir(juego, enemigo);
        AccionIA accion = evaluador.decidir(contexto);
        return new ResultadoTurnoIA(accion, ejecutor.ejecutar(juego, enemigo, accion, random));
    }
}
