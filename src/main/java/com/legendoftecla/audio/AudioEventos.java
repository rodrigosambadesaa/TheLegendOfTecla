package com.legendoftecla.audio;

import com.legendoftecla.events.*;
import com.legendoftecla.model.world.Posicion;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Adaptador de audio que escucha eventos del dominio. */
public final class AudioEventos implements AutoCloseable {
    private final List<Suscripcion> suscripciones = new ArrayList<>();
    private final Supplier<Posicion> oyente;
    public AudioEventos(BusEventos bus, Supplier<Posicion> oyente) {
        this.oyente = oyente;
        suscripciones.add(bus.suscribir(PersonajeMovido.class,
                e -> reproducir(EventoSonido.MOVIMIENTO, e.destino())));
        suscripciones.add(bus.suscribir(PersonajeAtacado.class,
                e -> reproducir(EventoSonido.ATAQUE, e.origen())));
        suscripciones.add(bus.suscribir(PersonajeDanado.class,
                e -> reproducir(EventoSonido.DANIO, e.posicion())));
        suscripciones.add(bus.suscribir(IncendioIniciado.class,
                e -> reproducir(EventoSonido.INCENDIO, e.posicion())));
        suscripciones.add(bus.suscribir(IncendioExtinguido.class,
                e -> reproducir(EventoSonido.APAGAR_FUEGO, e.posicion())));
        suscripciones.add(bus.suscribir(PuertaAbierta.class,
                e -> GestorSonido.reproducir(EventoSonido.PUERTA)));
        suscripciones.add(bus.suscribir(TrampaActivada.class,
                e -> GestorSonido.reproducir(e.trampa().toLowerCase().contains("alarma")
                        ? EventoSonido.ALARMA : EventoSonido.TRAMPA)));
        suscripciones.add(bus.suscribir(TrampaDetectada.class,
                e -> GestorSonido.reproducir(EventoSonido.DESCUBRIMIENTO)));
        suscripciones.add(bus.suscribir(ArmaRecargada.class,
                e -> reproducir(EventoSonido.RECARGA, e.posicion())));
        suscripciones.add(bus.suscribir(ObjetoUsado.class, e -> {
            String objeto = e.objeto().toLowerCase();
            if (objeto.contains("agua") || objeto.contains("cubo")) {
                reproducir(EventoSonido.AGUA, e.posicion());
            }
        }));
        suscripciones.add(bus.suscribir(PersonajeMuerto.class,
                e -> reproducir(EventoSonido.MUERTE_ENEMIGO, e.posicion())));
        suscripciones.add(bus.suscribir(MisionCompletada.class,
                e -> GestorSonido.reproducir(EventoSonido.MISION)));
    }
    private void reproducir(EventoSonido sonido, Posicion origen) {
        GestorSonido.reproducir(sonido, origen, oyente.get());
    }
    @Override public void close() { suscripciones.forEach(Suscripcion::close); }
}
