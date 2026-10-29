package com.legendoftecla.progression;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.engine.ServicioExperiencia;
import com.legendoftecla.engine.SistemaCombate;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecompensasExperienciaUnicasTest {
    @Test
    void repetirLaRecompensaDeUnaBajaNoPermiteFarmearXp() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Sectoid enemigo = enemigo("Blanco");
        enemigo.setSalud(1);
        SistemaCombate.atacar(juego, juego.getJugador(), enemigo, new Random(7));
        int nivel = juego.getJugador().getProgresion().getNivel();
        int experiencia = juego.getJugador().getProgresion().getExperiencia();
        assertTrue(experiencia > 0);

        for (int i = 0; i < 50; i++) {
            ServicioExperiencia.recompensarBaja(juego, juego.getJugador(), enemigo);
        }

        assertEquals(nivel, juego.getJugador().getProgresion().getNivel());
        assertEquals(experiencia, juego.getJugador().getProgresion().getExperiencia());
    }

    @Test
    void distintosEnemigosConElMismoNombreDanRecompensasIndependientes() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Sectoid primero = enemigo("Igual");
        Sectoid segundo = enemigo("Igual");
        primero.setSalud(0);
        segundo.setSalud(0);

        ServicioExperiencia.recompensarBaja(juego, juego.getJugador(), primero);
        ServicioExperiencia.recompensarBaja(juego, juego.getJugador(), segundo);
        ServicioExperiencia.recompensarBaja(juego, juego.getJugador(), primero);

        assertEquals(1, juego.getJugador().getProgresion().getNivel());
        assertEquals(96, juego.getJugador().getProgresion().getExperiencia());
    }

    @Test
    void premioDeVictoriaSoloSeConcedeUnaVezAJugadorYAliados() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Aliado aliado = new Aliado("Aliado", new Posicion(1, 1),
                new Mochila(2, 5), 2);
        juego.agregarAliado(aliado);

        ServicioExperiencia.recompensarVictoria(juego);
        ServicioExperiencia.recompensarVictoria(juego);
        ServicioExperiencia.recompensarVictoria(juego);

        assertEquals(2, juego.getJugador().getProgresion().getNivel());
        assertEquals(0, juego.getJugador().getProgresion().getExperiencia());
        assertEquals(1, aliado.getProgresion().getNivel());
        assertEquals(60, aliado.getProgresion().getExperiencia());
    }

    @Test
    void unaRecompensaPrematuraNoInvalidaLaBajaLegitima() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Sectoid enemigo = enemigo("Resistente");
        ServicioExperiencia.recompensarBaja(juego, juego.getJugador(), enemigo);
        assertEquals(0, juego.getJugador().getProgresion().getExperiencia());

        enemigo.setSalud(0);
        ServicioExperiencia.recompensarBaja(juego, juego.getJugador(), enemigo);

        assertEquals(48, juego.getJugador().getProgresion().getExperiencia());
    }

    @Test
    void losRegistrosDeRecompensasSonIndependientesEntrePartidas() {
        Juego primera = TestFixtures.juegoBasico(TestFixtures.consola());
        Juego segunda = TestFixtures.juegoBasico(TestFixtures.consola());
        Sectoid enemigo = enemigo("Compartido");
        enemigo.setSalud(0);

        ServicioExperiencia.recompensarBaja(primera, primera.getJugador(), enemigo);
        ServicioExperiencia.recompensarBaja(segunda, segunda.getJugador(), enemigo);
        ServicioExperiencia.recompensarVictoria(primera);
        ServicioExperiencia.recompensarVictoria(segunda);

        assertEquals(2, primera.getJugador().getProgresion().getNivel());
        assertEquals(48, primera.getJugador().getProgresion().getExperiencia());
        assertEquals(2, segunda.getJugador().getProgresion().getNivel());
        assertEquals(48, segunda.getJugador().getProgresion().getExperiencia());
    }

    private static Sectoid enemigo(String nombre) {
        return new Sectoid(nombre, new Posicion(0, 1), new Mochila(2, 5), 2);
    }
}
