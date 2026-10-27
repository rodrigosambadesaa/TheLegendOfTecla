package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.events.TrampaActivada;
import com.legendoftecla.model.elements.Mina;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrampasUbicacionTest {
    @Test
    void rechazaDetonacionEnCoordenadasEquivocadasSinDanar() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Mina mina = new Mina("mina-remota", 20, 3, true);
        juego.getMapa().getCelda(new Posicion(1, 1)).agregarElemento(mina);
        int saludInicial = juego.getJugador().getSalud();
        List<TrampaActivada> eventos = escuchar(juego);

        assertFalse(SistemaTrampas.detonar(juego, new Posicion(0, 1), mina));

        assertTrue(mina.isActiva());
        assertEquals(saludInicial, juego.getJugador().getSalud());
        assertTrue(eventos.isEmpty());
    }

    @Test
    void rechazaOtraTrampaConElMismoIdAunqueEsteEnOtraCelda() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion posicion = new Posicion(1, 1);
        Mina registrada = new Mina("duplicada", 20, 3, true);
        Mina externa = new Mina("duplicada", 20, 3, true);
        juego.getMapa().getCelda(posicion).agregarElemento(registrada);
        int saludInicial = juego.getJugador().getSalud();
        List<TrampaActivada> eventos = escuchar(juego);

        assertFalse(SistemaTrampas.detonar(juego, posicion, externa));
        assertFalse(SistemaTrampas.disparar(juego, posicion, externa));

        assertTrue(registrada.isActiva());
        assertTrue(externa.isActiva());
        assertEquals(saludInicial, juego.getJugador().getSalud());
        assertTrue(eventos.isEmpty());
    }

    @Test
    void rechazaDisparoEnPosicionFueraDelMapa() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Mina mina = new Mina("remota", 20, 3, true);
        juego.getMapa().getCelda(new Posicion(1, 1)).agregarElemento(mina);
        int saludInicial = juego.getJugador().getSalud();

        assertFalse(SistemaTrampas.disparar(juego, new Posicion(-1, 0), mina));
        assertFalse(SistemaTrampas.detonar(juego, new Posicion(300, 300), mina));
        assertTrue(mina.isActiva());
        assertEquals(saludInicial, juego.getJugador().getSalud());
    }

    @Test
    void detonacionLegitimaFuncionaUnaSolaVez() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion posicion = new Posicion(1, 1);
        Mina mina = new Mina("remota", 10, 3, true);
        juego.getMapa().getCelda(posicion).agregarElemento(mina);
        int saludInicial = juego.getJugador().getSalud();
        List<TrampaActivada> eventos = escuchar(juego);

        assertTrue(SistemaTrampas.detonar(juego, posicion, mina));
        assertEquals(saludInicial - 10, juego.getJugador().getSalud());
        assertFalse(mina.isActiva());
        assertFalse(eventos.isEmpty());
        int eventosIniciales = eventos.size();
        assertFalse(SistemaTrampas.detonar(juego, posicion, mina));
        assertEquals(eventosIniciales, eventos.size());
    }

    private static List<TrampaActivada> escuchar(Juego juego) {
        List<TrampaActivada> eventos = new ArrayList<>();
        juego.getBusEventos().suscribir(TrampaActivada.class, eventos::add);
        return eventos;
    }
}
