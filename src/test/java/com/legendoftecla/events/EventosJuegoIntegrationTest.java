package com.legendoftecla.events;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.commands.ComandoMover;
import com.legendoftecla.commands.CommandContext;
import com.legendoftecla.engine.SistemaCombate;
import com.legendoftecla.engine.SistemaIncendios;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class EventosJuegoIntegrationTest {
    @Test
    void publicaInspeccionYMovimientoConInstanteDeterminista() throws Exception {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Instant instante = Instant.parse("2040-05-06T07:08:09Z");
        juego.setBusEventos(new BusEventos(Clock.fixed(instante, ZoneOffset.UTC)));
        List<EventoJuego> eventos = new ArrayList<>();
        juego.getBusEventos().suscribir(EventoJuego.class, eventos::add);

        juego.inspeccionarCeldaActual();
        juego.inspeccionarCeldaActual();
        new ComandoMover(new CommandContext(juego), Direccion.ESTE).ejecutar();

        assertEquals(3, eventos.size());
        assertInstanceOf(CeldaInspeccionada.class, eventos.get(0));
        assertInstanceOf(PersonajeMovido.class, eventos.get(1));
        assertInstanceOf(RuidoGenerado.class, eventos.get(2));
        assertEquals(instante, eventos.get(2).instante());
    }

    @Test
    void combateEIncendioPublicanHechosSinDependerDeLaInterfaz() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion posicion = juego.getJugador().getPosicion();
        Sectoid enemigo = new Sectoid("Alien", posicion, new Mochila(2, 10), 2);
        juego.agregarEnemigo(enemigo);
        juego.getMapa().getCelda(posicion).agregarEnemigo(enemigo);
        List<Class<?>> tipos = new ArrayList<>();
        juego.getBusEventos().suscribir(EventoJuego.class,
                evento -> tipos.add(evento.getClass()));

        SistemaCombate.atacar(juego, juego.getJugador(), enemigo, new Random(1));
        SistemaIncendios.iniciar(juego, posicion, 2);
        SistemaIncendios.apagar(juego, posicion);

        assertEquals(List.of(PersonajeAtacado.class, RuidoGenerado.class,
                PersonajeDanado.class, IncendioIniciado.class, RuidoGenerado.class,
                IncendioExtinguido.class), tipos);
    }
}
