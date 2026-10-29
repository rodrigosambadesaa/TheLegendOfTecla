package com.legendoftecla.progression;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.engine.ServicioEquilibrioInicial;
import com.legendoftecla.engine.ServicioExperiencia;
import com.legendoftecla.engine.ServicioNivelInicial;
import com.legendoftecla.engine.SistemaCombate;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.persistence.PersistenciaPartida;
import com.legendoftecla.persistence.PersistenciaProgresoLocal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProgresionEnPartidaTest {
    @TempDir Path temporal;

    @AfterEach
    void restablecerMultiplicador() {
        Enemigo.setMultiplicadorDanioGlobal(1.0);
    }

    @Test
    void unEnemigoDerrotadoDaXpSoloUnaVez() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Sectoid enemigo = new Sectoid("Blanco", new Posicion(0, 1),
                new Mochila(4, 20), 2);
        enemigo.setSalud(1);
        int saludInicial = juego.getJugador().getSaludMaxima();

        SistemaCombate.atacar(juego, juego.getJugador(), enemigo, new Random(2));
        int xp = juego.getJugador().getProgresion().getExperiencia();
        assertTrue(xp > 0);
        assertEquals(1, juego.getJugador().getProgresion().getNivel());

        // Una segunda llamada sobre el mismo objetivo muerto no es otra baja.
        try {
            SistemaCombate.atacar(juego, juego.getJugador(), enemigo, new Random(2));
        } catch (IllegalArgumentException | IllegalStateException esperado) {
            // Las validaciones de combate pueden rechazar el ataque al muerto.
        }
        assertEquals(xp, juego.getJugador().getProgresion().getExperiencia());
        assertEquals(saludInicial, juego.getJugador().getSaludMaxima());
    }

    @Test
    void aliadoObtieneBajaYJugadorCercanoComparteExperiencia() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Aliado aliado = new Aliado("A", new Posicion(0, 0), new Mochila(4, 20), 2);
        juego.agregarAliado(aliado);
        Sectoid enemigo = new Sectoid("Blanco", new Posicion(0, 1), new Mochila(4, 20), 2);
        enemigo.setSalud(1);
        SistemaCombate.atacar(juego, aliado, enemigo, new Random(9));
        assertTrue(aliado.getProgresion().getExperiencia() > 0);
        assertTrue(juego.getJugador().getProgresion().getExperiencia() > 0);
    }

    @Test
    void subirNivelMejoraEstadisticasYRespetaElCien() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        var jugador = juego.getJugador();
        int vida = jugador.getSaludMaxima();
        assertEquals(1, ServicioExperiencia.ganarExperiencia(jugador, 120));
        assertEquals(2, jugador.getProgresion().getNivel());
        assertEquals(20, jugador.getProgresion().getExperiencia());
        assertEquals(vida + 3, jugador.getSaludMaxima());

        jugador.getProgresion().restaurar(99, 9899, Set.of());
        assertEquals(1, ServicioExperiencia.ganarExperiencia(jugador, 500));
        assertEquals(100, jugador.getProgresion().getNivel());
        assertEquals(0, jugador.getProgresion().getExperiencia());
        assertEquals(0, ServicioExperiencia.ganarExperiencia(jugador, 500));

        Aliado aliado = new Aliado("A", new Posicion(0, 0), new Mochila(4, 20), 2);
        int saludAliada = aliado.getSaludMaxima();
        assertEquals(1, ServicioExperiencia.ganarExperiencia(aliado, 105));
        assertEquals(2, aliado.getNivel());
        assertEquals(5, aliado.getProgresion().getExperiencia());
        assertEquals(saludAliada + 8, aliado.getSaludMaxima());
    }

    @Test
    void guardaYRecuperaExperienciaAliadaEnPartidaCompleta() throws Exception {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Aliado aliado = new Aliado("A", new Posicion(0, 0), new Mochila(4, 20), 2);
        juego.agregarAliado(aliado);
        juego.getMapa().getCelda(aliado.getPosicion()).agregarAliado(aliado);
        ServicioExperiencia.ganarExperiencia(aliado, 123);
        ServicioExperiencia.ganarExperiencia(juego.getJugador(), 132);
        Path partida = temporal.resolve("save.json");
        PersistenciaPartida.guardar(juego, partida, 7);
        Juego reanudada = PersistenciaPartida.cargar(partida, TestFixtures.consola());
        assertEquals(2, reanudada.getJugador().getProgresion().getNivel());
        assertEquals(32, reanudada.getJugador().getProgresion().getExperiencia());
        assertEquals(2, reanudada.getAliadosRegistrados().get(0).getNivel());
        assertEquals(23, reanudada.getAliadosRegistrados().get(0)
                .getProgresion().getExperiencia());
    }

    @Test
    void perfilLocalConservaNivelesDeJugadorYAliadosEntrePartidas() throws Exception {
        Juego original = TestFixtures.juegoBasico(TestFixtures.consola());
        Aliado aliado = new Aliado("A", new Posicion(0, 0), new Mochila(4, 20), 2);
        original.agregarAliado(aliado);
        ServicioExperiencia.ganarExperiencia(original.getJugador(), 160);
        ServicioExperiencia.ganarExperiencia(aliado, 135);
        Path perfil = temporal.resolve("profile.json");
        PersistenciaProgresoLocal.guardar(original, perfil);

        Juego otra = TestFixtures.juegoBasico(TestFixtures.consola());
        Aliado nuevo = new Aliado("A", new Posicion(0, 0), new Mochila(4, 20), 2);
        otra.agregarAliado(nuevo);
        PersistenciaProgresoLocal.aplicar(otra, perfil);
        assertEquals(2, otra.getJugador().getProgresion().getNivel());
        assertEquals(60, otra.getJugador().getProgresion().getExperiencia());
        assertEquals(2, nuevo.getNivel());
        assertEquals(35, nuevo.getProgresion().getExperiencia());
        assertTrue(nuevo.getSaludMaxima() > 90);
    }

    @Test
    void equilibraAliadosElegidosYRefuerzaEnemigosAlPartirDeNivelAlto() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Aliado demasiadoFuerte = new Aliado("A", new Posicion(0, 0), new Mochila(4, 20), 2);
        demasiadoFuerte.setNivel(100);
        juego.agregarAliado(demasiadoFuerte);
        ServicioEquilibrioInicial.aplicar(juego, Dificultad.NORMAL);
        assertEquals(3, demasiadoFuerte.getNivel());

        ServicioNivelInicial.aplicar(juego.getJugador(), 30);
        Sectoid enemigo = new Sectoid("Blanco", new Posicion(0, 1), new Mochila(4, 20), 2);
        juego.agregarEnemigo(enemigo);
        int vida = enemigo.getSaludMaxima();
        ServicioEquilibrioInicial.aplicar(juego, Dificultad.NORMAL);
        assertEquals(28, demasiadoFuerte.getNivel());
        assertTrue(enemigo.getSaludMaxima() > vida);
        assertEquals(1 + 29 * 0.015, Enemigo.getMultiplicadorDanioGlobal(), 0.001);
    }
}
