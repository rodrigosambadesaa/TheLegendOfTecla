package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.events.EventoJuego;
import com.legendoftecla.events.PersonajeAtacado;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtaqueMultipleObjetivosValidacionTest {
    @Test
    void objetivoRepetidoNoConsumeMunicionNiGeneraEventos() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Arma arma = rifle();
        juego.getJugador().setArmasEquipadas(List.of(arma));
        Sectoid enemigo = enemigo(juego, "Uno", new Posicion(0, 0));
        List<EventoJuego> eventos = escuchar(juego);
        int salud = enemigo.getSalud();

        assertThrows(IllegalArgumentException.class, () -> SistemaCombate.atacarTodos(
                juego, juego.getJugador(), List.of(enemigo, enemigo), new Random(7)));

        assertEquals(3, arma.getMunicionActual());
        assertEquals(salud, enemigo.getSalud());
        assertTrue(eventos.isEmpty());
    }

    @Test
    void objetivosEnCasillasDistintasNoSeDananADistanciaIndebida() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Arma arma = rifle();
        juego.getJugador().setArmasEquipadas(List.of(arma));
        Sectoid cercano = enemigo(juego, "Cercano", new Posicion(0, 1));
        Sectoid alejado = enemigo(juego, "Lejano", new Posicion(2, 2));
        List<EventoJuego> eventos = escuchar(juego);

        assertThrows(IllegalArgumentException.class, () -> SistemaCombate.atacarTodos(
                juego, juego.getJugador(), List.of(cercano, alejado), new Random(7)));

        assertEquals(3, arma.getMunicionActual());
        assertEquals(cercano.getSaludMaxima(), cercano.getSalud());
        assertEquals(alejado.getSaludMaxima(), alejado.getSalud());
        assertTrue(eventos.isEmpty());
    }

    @Test
    void objetivoNuloSeRechazaAntesDeNotificarAtaque() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Arma arma = rifle();
        juego.getJugador().setArmasEquipadas(List.of(arma));
        Sectoid enemigo = enemigo(juego, "Uno", new Posicion(0, 0));
        List<Sectoid> conNulo = new ArrayList<>();
        conNulo.add(enemigo);
        conNulo.add(null);
        List<EventoJuego> eventos = escuchar(juego);

        assertThrows(NullPointerException.class, () -> SistemaCombate.atacarTodos(
                juego, juego.getJugador(), conNulo, new Random(7)));

        assertEquals(3, arma.getMunicionActual());
        assertEquals(enemigo.getSaludMaxima(), enemigo.getSalud());
        assertTrue(eventos.isEmpty());
    }

    @Test
    void dosObjetivosValidosSiguenRecibiendoUnDisparoColectivo() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Arma arma = rifle();
        juego.getJugador().setArmasEquipadas(List.of(arma));
        Sectoid uno = enemigo(juego, "Uno", new Posicion(0, 0));
        Sectoid dos = enemigo(juego, "Dos", new Posicion(0, 0));
        List<EventoJuego> eventos = escuchar(juego);

        List<ResultadoAtaque> resultados = SistemaCombate.atacarTodos(
                juego, juego.getJugador(), List.of(uno, dos), new Random(7));

        assertEquals(2, resultados.size());
        assertEquals(2, arma.getMunicionActual());
        assertTrue(uno.getSalud() < uno.getSaludMaxima());
        assertTrue(dos.getSalud() < dos.getSaludMaxima());
        assertEquals(2, eventos.stream().filter(PersonajeAtacado.class::isInstance).count());
        assertSame(uno, juego.getEnemigos().get(0));
    }

    private static Arma rifle() {
        return new Arma("Rifle", "Ensayo", 1, 15, true,
                TipoMunicion.RIFLE, 5, 3);
    }

    private static Sectoid enemigo(Juego juego, String nombre, Posicion posicion) {
        Sectoid enemigo = new Sectoid(nombre, posicion, new Mochila(2, 10), 3);
        juego.agregarEnemigo(enemigo);
        return enemigo;
    }

    private static List<EventoJuego> escuchar(Juego juego) {
        List<EventoJuego> eventos = new ArrayList<>();
        juego.getBusEventos().suscribir(EventoJuego.class, eventos::add);
        return eventos;
    }
}
