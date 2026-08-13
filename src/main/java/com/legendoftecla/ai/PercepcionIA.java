package com.legendoftecla.ai;

import com.legendoftecla.engine.SistemaIluminacion;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.world.Juego;

/** Construye una percepcion reproducible sin ejecutar acciones. */
public final class PercepcionIA {
    /** @return instantanea visual, auditiva y tactica del enemigo */
    public ContextoIA percibir(Juego juego, Enemigo enemigo) {
        boolean coordinacion = juego.getAliados().stream()
                .anyMatch(aliado -> aliado.getSalud() > 0);
        java.util.List<Personaje> escuadron = new java.util.ArrayList<>();
        escuadron.add(juego.getJugador());
        if (coordinacion) {
            juego.getAliados().stream().filter(aliado -> aliado.getSalud() > 0)
                    .forEach(escuadron::add);
        }
        Personaje objetivoVisible = escuadron.stream()
                .filter(objetivo -> visible(juego, enemigo, objetivo))
                .min(java.util.Comparator
                        .comparingDouble((Personaje objetivo) ->
                                (double) objetivo.getSalud() / objetivo.getSaludMaxima())
                        .thenComparingInt(objetivo -> enemigo.getPosicion()
                                .distanciaManhattan(objetivo.getPosicion()))
                        .thenComparing(Personaje::getNombre))
                .orElse(null);
        boolean aliadoHerido = coordinacion && juego.getEnemigos().stream()
                .filter(otro -> otro != enemigo && otro.getSalud() > 0)
                .anyMatch(otro -> otro.getSalud() < otro.getSaludMaxima());
        Personaje objetivo = objetivoVisible == null
                ? juego.getJugador() : objetivoVisible;
        return new ContextoIA(enemigo, juego, objetivoVisible != null,
                enemigo.getControladorIA().getUltimaPosicionConocida(),
                aliadoHerido, !enemigo.puedeAtacar(), objetivo, coordinacion);
    }

    private boolean visible(Juego juego, Enemigo enemigo, Personaje objetivo) {
        if (objetivo.getSalud() <= 0) return false;
        int distancia = enemigo.getPosicion().distanciaManhattan(objetivo.getPosicion());
        boolean iluminado = SistemaIluminacion.hayLuz(juego, objetivo.getPosicion());
        int rangoVisual = iluminado ? enemigo.getRangoVision()
                : Math.max(1, enemigo.getRangoVision() / 2);
        return distancia <= rangoVisual && juego.getMapa().hayLineaAtaque(
                enemigo.getPosicion(), objetivo.getPosicion());
    }
}
