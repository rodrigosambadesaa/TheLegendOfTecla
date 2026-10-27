package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.events.PersonajeDanado;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncendiosCombatientesActivosTest {
    @Test
    void alcanzaAAliadosYEnemigosRegistradosSinPresenciaEnListaDeCelda() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion foco = new Posicion(1, 1);
        Aliado aliado = aliado("Aliado", foco);
        Sectoid enemigo = enemigo("Enemigo", foco);
        juego.agregarAliado(aliado);
        juego.agregarEnemigo(enemigo);
        juego.getMapa().getCelda(foco).setNivelFuego(2);
        assertFalse(juego.getMapa().getCelda(foco).hasAliados());
        assertFalse(juego.getMapa().getCelda(foco).hasEnemigos());
        int saludAliado = aliado.getSalud();
        int saludEnemigo = enemigo.getSalud();
        int saludJugador = juego.getJugador().getSalud();
        List<PersonajeDanado> eventos = escuchar(juego);

        SistemaIncendios.avanzarTurno(juego, new Random(7));

        assertEquals(saludAliado - 10, aliado.getSalud());
        assertEquals(saludEnemigo - 10, enemigo.getSalud());
        assertEquals(saludJugador, juego.getJugador().getSalud());
        assertEquals(2, eventos.size());
    }

    @Test
    void ignoraLaPosicionAntiguaAunqueSigaEnLaListaDeLaCelda() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion antigua = new Posicion(0, 1);
        Posicion actual = new Posicion(2, 2);
        Aliado aliado = aliado("Movido", antigua);
        juego.agregarAliado(aliado);
        juego.getMapa().getCelda(antigua).agregarAliado(aliado);
        aliado.setPosicion(actual);
        juego.getMapa().getCelda(antigua).setNivelFuego(1);
        juego.getMapa().getCelda(actual).setNivelFuego(2);
        int salud = aliado.getSalud();

        SistemaIncendios.avanzarTurno(juego, new Random(7));

        assertEquals(salud - 10, aliado.getSalud());
    }

    @Test
    void noDaniaAOcupantesDesvinculadosDeLaPartida() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion foco = new Posicion(1, 1);
        Aliado desvinculado = aliado("Inactivo", foco);
        Sectoid ajeno = enemigo("Ajeno", foco);
        juego.getMapa().getCelda(foco).agregarAliado(desvinculado);
        juego.getMapa().getCelda(foco).agregarEnemigo(ajeno);
        juego.getMapa().getCelda(foco).setNivelFuego(3);
        int saludAliado = desvinculado.getSalud();
        int saludEnemigo = ajeno.getSalud();
        List<PersonajeDanado> eventos = escuchar(juego);

        SistemaIncendios.avanzarTurno(juego, new Random(7));

        assertEquals(saludAliado, desvinculado.getSalud());
        assertEquals(saludEnemigo, ajeno.getSalud());
        assertTrue(eventos.isEmpty());
    }

    @Test
    void unAliadoEvacuadoNoRecibeDanioDelMapaAunqueQuedeEnCelda() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion foco = new Posicion(1, 1);
        Aliado evacuado = aliado("Evacuado", foco);
        juego.agregarAliado(evacuado);
        juego.getMapa().getCelda(foco).agregarAliado(evacuado);
        juego.setAliados(List.of());
        juego.setAliadosExtraidosDetalle(List.of(evacuado));
        juego.getMapa().getCelda(foco).setNivelFuego(2);
        int salud = evacuado.getSalud();

        SistemaIncendios.avanzarTurno(juego, new Random(7));

        assertEquals(salud, evacuado.getSalud());
    }

    private static Aliado aliado(String nombre, Posicion posicion) {
        return new Aliado(nombre, posicion, new Mochila(3, 10), 2);
    }

    private static Sectoid enemigo(String nombre, Posicion posicion) {
        return new Sectoid(nombre, posicion, new Mochila(3, 10), 2);
    }

    private static List<PersonajeDanado> escuchar(Juego juego) {
        List<PersonajeDanado> eventos = new ArrayList<>();
        juego.getBusEventos().suscribir(PersonajeDanado.class, eventos::add);
        return eventos;
    }
}
