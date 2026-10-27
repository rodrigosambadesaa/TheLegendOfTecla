package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.events.EventoJuego;
import com.legendoftecla.events.PersonajeAtacado;
import com.legendoftecla.events.RuidoGenerado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.TipoMunicion;
import com.legendoftecla.model.world.Juego;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtaqueMultipleVacioTest {
    @Test
    void ningunObjetivoNoEmiteEventosNiConsumeMunicion() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Arma arma = new Arma("Rifle", "Prueba", 2, 10, false,
                TipoMunicion.RIFLE, 5, 3);
        juego.getJugador().setArmasEquipadas(List.of(arma));
        List<EventoJuego> eventos = new ArrayList<>();
        juego.getBusEventos().suscribir(EventoJuego.class, eventos::add);
        int salud = juego.getJugador().getSalud();

        for (int intento = 0; intento < 200; intento++) {
            assertTrue(SistemaCombate.atacarTodos(juego, juego.getJugador(),
                    List.of(), new Random(intento)).isEmpty());
        }

        assertTrue(eventos.isEmpty());
        assertEquals(3, arma.getMunicionActual());
        assertEquals(salud, juego.getJugador().getSalud());
        assertEquals(0, juego.getEstadisticas().getDisparos());
    }

    @Test
    void ataqueContraObjetivoRealConservaEventosYConsumeMunicion() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Arma arma = new Arma("Rifle", "Prueba", 2, 10, false,
                TipoMunicion.RIFLE, 5, 3);
        juego.getJugador().setArmasEquipadas(List.of(arma));
        Sectoid enemigo = new Sectoid("Objetivo", juego.getJugador().getPosicion(),
                new Mochila(2, 10), 2);
        juego.agregarEnemigo(enemigo);
        List<EventoJuego> eventos = new ArrayList<>();
        juego.getBusEventos().suscribir(EventoJuego.class, eventos::add);

        List<ResultadoAtaque> resultados = SistemaCombate.atacarTodos(
                juego, juego.getJugador(), List.of(enemigo), new Random(7));

        assertEquals(1, resultados.size());
        assertEquals(2, arma.getMunicionActual());
        assertTrue(enemigo.getSalud() < enemigo.getSaludMaxima());
        assertFalse(eventos.stream().filter(PersonajeAtacado.class::isInstance).toList().isEmpty());
        assertEquals(1, eventos.stream().filter(RuidoGenerado.class::isInstance).count());
    }
}
