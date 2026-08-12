package com.legendoftecla.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.time.Clock;
import java.time.Instant;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Bus sincrono de instancia con listeners ordenados y aislados ante fallos. */
public final class BusEventos {
    private final List<Entrada<?>> entradas = new ArrayList<>();
    private final Clock reloj;
    private final BiConsumer<EventoJuego, RuntimeException> manejadorError;

    /** Crea un bus que ignora los errores de listeners tras aislarlos. */
    public BusEventos() {
        this(Clock.systemUTC(), (evento, error) -> { });
    }

    /** @param manejadorError observador no nulo de errores de listeners */
    public BusEventos(BiConsumer<EventoJuego, RuntimeException> manejadorError) {
        this(Clock.systemUTC(), manejadorError);
    }

    /** @param reloj fuente de tiempo no nula, inyectable para pruebas y replays */
    public BusEventos(Clock reloj) {
        this(reloj, (evento, error) -> { });
    }

    /**
     * Crea un bus con tiempo y diagnostico inyectables.
     *
     * @param reloj fuente de tiempo no nula
     * @param manejadorError observador no nulo de errores de listeners
     */
    public BusEventos(Clock reloj,
            BiConsumer<EventoJuego, RuntimeException> manejadorError) {
        this.reloj = Objects.requireNonNull(reloj, "Reloj");
        this.manejadorError = Objects.requireNonNull(manejadorError, "Manejador de error");
    }

    /** @return instante actual de la fuente temporal del bus */
    public Instant ahora() {
        return reloj.instant();
    }

    /**
     * Suscribe un listener. El orden de llamada coincide con el de suscripcion.
     *
     * @param tipo clase de eventos aceptada
     * @param listener receptor sincrono
     * @param <T> tipo del evento
     * @return suscripcion revocable
     */
    public synchronized <T extends EventoJuego> Suscripcion suscribir(
            Class<T> tipo, Consumer<? super T> listener) {
        Entrada<T> entrada = new Entrada<>(Objects.requireNonNull(tipo, "Tipo"),
                Objects.requireNonNull(listener, "Listener"));
        entradas.add(entrada);
        return () -> eliminar(entrada);
    }

    /** Publica sobre una instantanea, permitiendo altas y bajas durante la llamada. */
    public void publicar(EventoJuego evento) {
        Objects.requireNonNull(evento, "Evento");
        for (Entrada<?> entrada : instantanea()) {
            if (entrada.tipo().isInstance(evento)) {
                notificar(entrada, evento);
            }
        }
    }

    /** @return numero actual de suscripciones */
    public synchronized int numeroSuscripciones() {
        return entradas.size();
    }

    private synchronized List<Entrada<?>> instantanea() {
        return List.copyOf(entradas);
    }

    private synchronized void eliminar(Entrada<?> entrada) {
        entradas.remove(entrada);
    }

    private void notificar(Entrada<?> entrada, EventoJuego evento) {
        try {
            entrada.aceptar(evento);
        } catch (RuntimeException error) {
            try {
                manejadorError.accept(evento, error);
            } catch (RuntimeException ignorado) {
                // El diagnostico tampoco puede impedir los listeners posteriores.
            }
        }
    }

    private record Entrada<T extends EventoJuego>(Class<T> tipo,
            Consumer<? super T> listener) {
        private void aceptar(EventoJuego evento) {
            listener.accept(tipo.cast(evento));
        }
    }
}
