package com.legendoftecla.ai;

import com.legendoftecla.engine.SistemaIluminacion;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Construye una percepcion reproducible sin ejecutar acciones. */
public final class PercepcionIA {
    /** @return instantanea visual, auditiva y tactica del enemigo */
    public ContextoIA percibir(Juego juego, Enemigo enemigo) {
        boolean coordinacion = juego.getAliados().stream()
                .anyMatch(aliado -> aliado.getSalud() > 0);
        boolean aliadoHerido = coordinacion && juego.getEnemigos().stream()
                .anyMatch(otro -> otro != enemigo && otro.getSalud() > 0
                        && otro.getSalud() < otro.getSaludMaxima());
        return percibir(juego, enemigo, coordinacion, aliadoHerido);
    }

    /** Construye la percepcion usando agregados calculados una vez por turno. */
    ContextoIA percibir(Juego juego, Enemigo enemigo,
                        boolean coordinacion, boolean aliadoHerido) {
        List<Personaje> escuadron = new ArrayList<>();
        escuadron.add(juego.getJugador());
        if (coordinacion) {
            agregarAliadosCandidatos(juego, enemigo, escuadron);
        }
        Personaje objetivoVisible = escuadron.stream()
                .filter(objetivo -> visible(juego, enemigo, objetivo))
                .min(Comparator
                        .comparingDouble((Personaje objetivo) ->
                                (double) objetivo.getSalud() / objetivo.getSaludMaxima())
                        .thenComparingInt(objetivo -> enemigo.getPosicion()
                                .distanciaManhattan(objetivo.getPosicion()))
                        .thenComparing(Personaje::getNombre))
                .orElse(null);
        Personaje objetivo = objetivoVisible == null
                ? juego.getJugador() : objetivoVisible;
        return new ContextoIA(enemigo, juego, objetivoVisible != null,
                enemigo.getControladorIA().getUltimaPosicionConocida(),
                aliadoHerido, !enemigo.puedeAtacar(), objetivo, coordinacion);
    }

    private void agregarAliadosCandidatos(Juego juego, Enemigo enemigo,
                                          List<Personaje> destino) {
        // Recorrer los aliados realmente activos, no las listas de las celdas.
        // La distancia filtra candidatos antes de calcular iluminacion y vision.
        Posicion origen = enemigo.getPosicion();
        int radio = enemigo.getRangoVision();
        juego.getAliados().stream()
                .filter(aliado -> aliado.getSalud() > 0)
                .filter(aliado -> origen.distanciaManhattan(aliado.getPosicion()) <= radio)
                .forEach(destino::add);
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
