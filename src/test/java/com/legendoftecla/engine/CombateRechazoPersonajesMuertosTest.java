package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.events.EventoJuego;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.TipoMunicion;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombateRechazoPersonajesMuertosTest {
    @Test
    void elJugadorMuertoNoPuedeAtacarNiEmitirEventos() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Sectoid objetivo = enemigo(juego, "Vivo");
        Arma arma = arma();
        juego.getJugador().setArmasEquipadas(List.of(arma));
        juego.getJugador().setSalud(0);
        List<EventoJuego> eventos = escuchar(juego);

        assertThrows(IllegalStateException.class, () ->
                SistemaCombate.atacar(juego, juego.getJugador(), objetivo, new Random(1)));

        assertEquals(objetivo.getSaludMaxima(), objetivo.getSalud());
        assertEquals(3, arma.getMunicionActual());
        assertTrue(eventos.isEmpty());
    }

    @Test
    void dispararAUnEnemigoMuertoNoDescuentaMunicionNiAnunciaAtaque() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Sectoid caido = enemigo(juego, "Caido");
        caido.setSalud(0);
        Arma arma = arma();
        juego.getJugador().setArmasEquipadas(List.of(arma));
        List<EventoJuego> eventos = escuchar(juego);

        assertThrows(IllegalArgumentException.class, () ->
                SistemaCombate.atacar(juego, juego.getJugador(), caido, new Random(1)));

        assertEquals(3, arma.getMunicionActual());
        assertTrue(eventos.isEmpty());
    }

    @Test
    void grupoMixtoRechazaTodaLaOperacionAntesDeAtacarAlPrimero() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Sectoid vivo = enemigo(juego, "Vivo");
        Sectoid caido = enemigo(juego, "Caido");
        caido.setSalud(0);
        Arma arma = arma();
        juego.getJugador().setArmasEquipadas(List.of(arma));
        List<EventoJuego> eventos = escuchar(juego);

        assertThrows(IllegalArgumentException.class, () ->
                SistemaCombate.atacarTodos(juego, juego.getJugador(),
                        List.of(vivo, caido), new Random(1)));

        assertEquals(vivo.getSaludMaxima(), vivo.getSalud());
        assertEquals(0, caido.getSalud());
        assertEquals(3, arma.getMunicionActual());
        assertTrue(eventos.isEmpty());
    }

    @Test
    void listaVaciaSigueSinProvocarAtaquesNiEventos() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        juego.getJugador().setSalud(0);
        List<EventoJuego> eventos = escuchar(juego);

        assertTrue(SistemaCombate.atacarTodos(
                juego, juego.getJugador(), List.of(), new Random(1)).isEmpty());
        assertTrue(eventos.isEmpty());
    }

    private static Sectoid enemigo(Juego juego, String nombre) {
        Sectoid enemigo = new Sectoid(nombre, new Posicion(0, 1),
                new Mochila(2, 10), 3);
        juego.agregarEnemigo(enemigo);
        return enemigo;
    }

    private static Arma arma() {
        return new Arma("Rifle", "Entrenamiento", 2, 12, true,
                TipoMunicion.RIFLE, 6, 3);
    }

    private static List<EventoJuego> escuchar(Juego juego) {
        List<EventoJuego> eventos = new ArrayList<>();
        juego.getBusEventos().suscribir(EventoJuego.class, eventos::add);
        return eventos;
    }
}
