package com.legendoftecla.ai;

import com.legendoftecla.events.BusEventos;
import com.legendoftecla.events.RuidoGenerado;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.model.world.Juego;

import java.util.List;
import java.util.function.Supplier;

/** Traduce acciones a eventos y eventos a percepcion de enemigos. */
public final class SistemaRuido implements AutoCloseable {
    private final BusEventos eventos;
    private final Supplier<List<Enemigo>> enemigos;
    private final com.legendoftecla.events.Suscripcion suscripcion;
    public SistemaRuido(BusEventos eventos, List<Enemigo> enemigos) {
        this(eventos, () -> List.copyOf(enemigos));
    }
    /** Mantiene una vista dinamica de los enemigos de la partida. */
    public SistemaRuido(Juego juego) {
        this(juego.getBusEventos(), juego::getEnemigos);
    }
    private SistemaRuido(BusEventos eventos, Supplier<List<Enemigo>> enemigos) {
        this.eventos = java.util.Objects.requireNonNull(eventos, "Eventos");
        this.enemigos = java.util.Objects.requireNonNull(enemigos, "Enemigos");
        suscripcion = eventos.suscribir(RuidoGenerado.class, this::distribuir);
    }
    public void generar(Posicion origen, FuenteRuido fuente) {
        eventos.publicar(new RuidoGenerado(eventos.ahora(), origen,
                fuente.intensidad(), fuente.name()));
    }
    private void distribuir(RuidoGenerado ruido) {
        enemigos.get().stream().filter(enemigo -> enemigo.getSalud() > 0)
                .forEach(enemigo -> enemigo.getControladorIA().percibir(ruido));
    }
    @Override public void close() { suscripcion.close(); }
}
