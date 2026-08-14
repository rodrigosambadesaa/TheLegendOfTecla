package com.legendoftecla.ai;

import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.world.Juego;

import java.util.Random;

/** Fachada que separa percepcion, evaluacion y ejecucion de la IA formal. */
public final class SistemaTurnosIA {
    private final PercepcionIA percepcion = new PercepcionIA();
    private final EvaluadorTactico evaluador = new EvaluadorTactico();
    private final EjecutorAccionIA ejecutor = new EjecutorAccionIA();
    private Juego juegoPreparado;
    private boolean coordinacionPreparada;
    private boolean aliadoHeridoPreparado;

    /** Calcula agregados costosos una sola vez antes de un lote enemigo. */
    public void prepararTurno(Juego juego) {
        juegoPreparado = java.util.Objects.requireNonNull(juego, "Juego");
        coordinacionPreparada = juego.getAliados().stream()
                .anyMatch(aliado -> aliado.getSalud() > 0);
        aliadoHeridoPreparado = juego.getEnemigos().stream()
                .anyMatch(enemigo -> enemigo.getSalud() > 0
                        && enemigo.getSalud() < enemigo.getSaludMaxima());
    }

    /** Descarta la instantanea al finalizar el lote. */
    public void finalizarTurno() {
        juegoPreparado = null;
    }

    /** Decide y ejecuta exactamente una accion con el RNG inyectado. */
    public ResultadoTurnoIA ejecutar(Juego juego, Enemigo enemigo, Random random) {
        ContextoIA contexto = juegoPreparado == juego
                ? percepcion.percibir(juego, enemigo,
                        coordinacionPreparada, aliadoHeridoPreparado)
                : percepcion.percibir(juego, enemigo);
        AccionIA accion = ajustarAlcance(evaluador.decidir(contexto), contexto);
        return new ResultadoTurnoIA(accion, ejecutor.ejecutar(juego, enemigo, accion, random));
    }

    private AccionIA ajustarAlcance(AccionIA accion, ContextoIA contexto) {
        if (accion.tipo() != TipoAccionIA.ATACAR) {
            return accion;
        }
        int distancia = contexto.enemigo().getPosicion().distanciaManhattan(
                contexto.posicionObjetivo());
        if (contexto.enemigo().puedeAtacarA(distancia)) {
            return accion;
        }
        TipoAccionIA alternativa = contexto.enemigo().puedeAtacar()
                ? TipoAccionIA.ACERCARSE : TipoAccionIA.RECARGAR;
        return new AccionIA(alternativa, contexto.posicionObjetivo(),
                alternativa == TipoAccionIA.ACERCARSE
                        ? "objetivo fuera del alcance del arma"
                        : "arma sin municion");
    }
}
