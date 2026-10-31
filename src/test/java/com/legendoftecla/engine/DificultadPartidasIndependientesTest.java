package com.legendoftecla.engine;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.legendoftecla.TestFixtures;
import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.persistence.PersistenciaPartida;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DificultadPartidasIndependientesTest {
    @TempDir Path temporal;

    @AfterEach
    void restablecerDificultadGlobal() {
        Enemigo.setMultiplicadorDanioGlobal(1.0);
    }

    @Test
    void iniciarOtraPartidaNoModificaElDanioDeEnemigosExistentes() {
        Juego dificil = juegoConEnemigo("Dificil");
        ServicioNivelInicial.aplicar(dificil.getJugador(), 30);
        ServicioEquilibrioInicial.aplicar(dificil, Dificultad.DIFICIL);
        Enemigo atacante = dificil.getEnemigos().get(0);
        double multiplicador = atacante.getMultiplicadorDanioPartida();
        assertTrue(multiplicador > 1);

        Juego facil = juegoConEnemigo("Facil");
        ServicioEquilibrioInicial.aplicar(facil, Dificultad.MUY_FACIL);
        assertTrue(Enemigo.getMultiplicadorDanioGlobal() < multiplicador);
        assertEquals(multiplicador, atacante.getMultiplicadorDanioPartida());

        int saludAnterior = dificil.getJugador().getSalud();
        atacante.atacar(dificil.getJugador());
        assertEquals((int) Math.round(4 * multiplicador),
                saludAnterior - dificil.getJugador().getSalud());
    }

    @Test
    void guardarYCargarConservaLaDificultadDeLosEnemigos() throws Exception {
        Juego original = juegoConEnemigo("Persistido");
        ServicioNivelInicial.aplicar(original.getJugador(), 40);
        ServicioEquilibrioInicial.aplicar(original, Dificultad.PESADILLA);
        double multiplicador = original.getEnemigos().get(0).getMultiplicadorDanioPartida();
        Path archivo = temporal.resolve("partida.json");
        PersistenciaPartida.guardar(original, archivo, 31L);

        Enemigo.setMultiplicadorDanioGlobal(0.5);
        Juego restaurado = PersistenciaPartida.cargar(archivo, TestFixtures.consola());
        Enemigo atacante = restaurado.getEnemigos().get(0);
        assertNotNull(atacante.getMultiplicadorDanioPartida());
        assertEquals(multiplicador, atacante.getMultiplicadorDanioPartida());

        int saludAnterior = restaurado.getJugador().getSalud();
        atacante.atacar(restaurado.getJugador());
        assertEquals((int) Math.round(4 * multiplicador),
                saludAnterior - restaurado.getJugador().getSalud());
    }

    @Test
    void archivosHistoricosSinMultiplicadorSiguenCargando() throws Exception {
        Juego original = juegoConEnemigo("Antiguo");
        Path archivo = temporal.resolve("legado.json");
        PersistenciaPartida.guardar(original, archivo, 31L);
        JsonObject json = JsonParser.parseString(Files.readString(archivo)).getAsJsonObject();
        json.getAsJsonArray("enemigos").get(0).getAsJsonObject()
                .remove("multiplicadorDanio");
        Files.writeString(archivo, json.toString());

        Juego restaurado = PersistenciaPartida.cargar(archivo, TestFixtures.consola());

        assertEquals(1, restaurado.getEnemigos().size());
        assertEquals(null, restaurado.getEnemigos().get(0).getMultiplicadorDanioPartida());
    }

    private static Juego juegoConEnemigo(String nombre) {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        juego.agregarEnemigo(new Sectoid(nombre, new Posicion(0, 1),
                new Mochila(3, 10), 2));
        return juego;
    }
}
