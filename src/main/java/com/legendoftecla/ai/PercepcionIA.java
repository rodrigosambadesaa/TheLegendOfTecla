package com.legendoftecla.ai;

import com.legendoftecla.engine.SistemaIluminacion;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.world.Juego;

/** Construye una percepcion reproducible sin ejecutar acciones. */
public final class PercepcionIA {
    /** @return instantanea visual, auditiva y tactica del enemigo */
    public ContextoIA percibir(Juego juego, Enemigo enemigo) {
        int distancia = enemigo.getPosicion().distanciaManhattan(
                juego.getJugador().getPosicion());
        boolean iluminado = SistemaIluminacion.hayLuz(
                juego, juego.getJugador().getPosicion());
        int rangoVisual = iluminado ? enemigo.getRangoVision()
                : Math.max(1, enemigo.getRangoVision() / 2);
        boolean veJugador = juego.getJugador().getSalud() > 0
                && distancia <= rangoVisual
                && juego.getMapa().hayLineaAtaque(
                        enemigo.getPosicion(), juego.getJugador().getPosicion());
        boolean aliadoHerido = juego.getEnemigos().stream()
                .filter(otro -> otro != enemigo && otro.getSalud() > 0)
                .anyMatch(otro -> otro.getSalud() < otro.getSaludMaxima());
        return new ContextoIA(enemigo, juego, veJugador,
                enemigo.getControladorIA().getUltimaPosicionConocida(),
                aliadoHerido, !enemigo.puedeAtacar());
    }
}
