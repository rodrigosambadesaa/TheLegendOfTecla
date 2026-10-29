package com.legendoftecla.ai;

import com.legendoftecla.events.BusEventos;
import com.legendoftecla.events.RuidoGenerado;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Mapa;

import java.util.List;
import java.util.function.Supplier;

/** Traduce acciones a eventos y eventos a percepcion de enemigos. */
public final class SistemaRuido implements AutoCloseable {
    private final BusEventos eventos;
    private final Supplier<List<Enemigo>> enemigos;
    private final Mapa mapa;
    private final com.legendoftecla.events.Suscripcion suscripcion;
    private RuidoGenerado ultimoRuido;
    public SistemaRuido(BusEventos eventos, List<Enemigo> enemigos) {
        this(eventos, () -> List.copyOf(enemigos), null);
    }
    /** Mantiene una vista dinamica de los enemigos de la partida. */
    public SistemaRuido(Juego juego) {
        this(juego.getBusEventos(), juego::getEnemigos, juego.getMapa());
    }
    private SistemaRuido(BusEventos eventos, Supplier<List<Enemigo>> enemigos, Mapa mapa) {
        this.eventos = java.util.Objects.requireNonNull(eventos, "Eventos");
        this.enemigos = java.util.Objects.requireNonNull(enemigos, "Enemigos");
        this.mapa = mapa;
        suscripcion = eventos.suscribir(RuidoGenerado.class, this::distribuir);
    }
    public void generar(Posicion origen, FuenteRuido fuente) {
        eventos.publicar(new RuidoGenerado(eventos.ahora(), origen,
                fuente.intensidad(), fuente.name()));
    }
    /** @return ultimo estimulo sonoro publicado, si existe */
    public java.util.Optional<RuidoGenerado> getUltimoRuido() {
        return java.util.Optional.ofNullable(ultimoRuido);
    }
    private void distribuir(RuidoGenerado ruido) {
        ultimoRuido = ruido;
        if (mapa != null && mapa.estaDentro(ruido.origen())) {
            distribuirEnRadio(ruido);
            return;
        }
        enemigos.get().stream().filter(enemigo -> enemigo.getSalud() > 0)
                .forEach(enemigo -> enemigo.getControladorIA().percibir(ruido));
    }

    private void distribuirEnRadio(RuidoGenerado ruido) {
        int radio = Math.max(0, ruido.intensidad());
        // La lista activa es la fuente de verdad: el registro de una celda
        // puede no reflejar movimientos, restauraciones o bajas anteriores.
        for (Enemigo enemigo : enemigos.get()) {
            if (enemigo.getSalud() > 0
                    && mapa.estaDentro(enemigo.getPosicion())
                    && ruido.origen().distanciaManhattan(enemigo.getPosicion()) <= radio) {
                enemigo.getControladorIA().percibir(ruido);
            }
        }
    }
    @Override public void close() { suscripcion.close(); }
}
