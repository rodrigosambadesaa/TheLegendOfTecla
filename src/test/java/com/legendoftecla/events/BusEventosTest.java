package com.legendoftecla.events;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.legendoftecla.model.world.Posicion;

class BusEventosTest {
    @Test
    void publicaEnOrdenFiltraPorTipoYPermiteCancelar() {
        List<String> llamadas = new ArrayList<>();
        BusEventos bus = new BusEventos();
        Suscripcion primera = bus.suscribir(EventoJuego.class,
                evento -> llamadas.add("general"));
        bus.suscribir(PersonajeMuerto.class,
                evento -> llamadas.add(evento.personaje()));

        bus.publicar(new PersonajeMuerto(Instant.EPOCH, "sectoid", new Posicion(0, 0)));
        primera.close();
        primera.close();
        bus.publicar(new PuertaAbierta(Instant.EPOCH, "p1"));

        assertEquals(List.of("general", "sectoid"), llamadas);
        assertEquals(1, bus.numeroSuscripciones());
    }

    @Test
    void aislaErroresIncluidoElManejadorYContinua() {
        List<String> llamadas = new ArrayList<>();
        BusEventos bus = new BusEventos((evento, error) -> {
            llamadas.add(error.getMessage());
            throw new IllegalStateException("error de diagnostico");
        });
        bus.suscribir(PersonajeMuerto.class, evento -> {
            throw new IllegalArgumentException("fallo controlado");
        });
        bus.suscribir(PersonajeMuerto.class,
                evento -> llamadas.add(evento.personaje()));

        bus.publicar(new PersonajeMuerto(Instant.EPOCH, "marine", new Posicion(0, 0)));

        assertEquals(List.of("fallo controlado", "marine"), llamadas);
    }

    @Test
    void usaInstantaneaSiUnListenerSeDaDeBajaDuranteLaPublicacion() {
        List<String> llamadas = new ArrayList<>();
        BusEventos bus = new BusEventos();
        Suscripcion[] segunda = new Suscripcion[1];
        bus.suscribir(EventoJuego.class, evento -> segunda[0].close());
        segunda[0] = bus.suscribir(EventoJuego.class,
                evento -> llamadas.add("segunda"));

        bus.publicar(new MisionCompletada(Instant.EPOCH, "rescate"));
        bus.publicar(new MisionCompletada(Instant.EPOCH, "rescate"));

        assertEquals(List.of("segunda"), llamadas);
    }

    @Test
    void usaRelojInyectableParaMantenerDeterminismo() {
        Instant instante = Instant.parse("2030-01-02T03:04:05Z");
        BusEventos bus = new BusEventos(Clock.fixed(instante, ZoneOffset.UTC));

        assertEquals(instante, bus.ahora());
    }
}
