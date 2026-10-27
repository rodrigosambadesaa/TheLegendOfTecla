package com.legendoftecla.persistence;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.engine.MotorPartida;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.items.Botiquin;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicioReplayRobustezTest {
    @TempDir
    Path temporal;

    @Test
    void v2DetectaCambiosQueLaHuellaHistoricaNoRegistraba() {
        Juego primero = partida();
        Juego segundo = partida();

        assertEquals(ServicioReplay.hash(primero), ServicioReplay.hash(segundo));
        String original = ServicioReplay.hashCompleto(primero);
        assertEquals(original, ServicioReplay.hashCompleto(segundo));
        assertTrue(segundo.getJugador().getMochila()
                .guardar(new Botiquin("Extra", "Botiquin extra", 1, 10)));

        assertEquals(ServicioReplay.hash(primero), ServicioReplay.hash(segundo));
        assertNotEquals(original, ServicioReplay.hashCompleto(segundo));

        Juego conEnemigo = partida();
        conEnemigo.agregarEnemigo(new Sectoid("X", new Posicion(1, 1),
                new Mochila(1, 1), 2));
        assertNotEquals(original, ServicioReplay.hashCompleto(conEnemigo));

        Juego conFuego = partida();
        conFuego.getMapa().getCelda(new Posicion(1, 1)).setNivelFuego(2);
        assertNotEquals(original, ServicioReplay.hashCompleto(conFuego));

        Juego conPuntuacion = partida();
        conPuntuacion.setPuntuacion(12);
        assertNotEquals(original, ServicioReplay.hashCompleto(conPuntuacion));
    }

    @Test
    void v2GuardaCargaYReproduceSinPerderCompatibilidadV1() throws Exception {
        Juego juego = partida();
        ReplayPartida v2 = new ReplayPartida(2, 42, Map.of("modo", "basico"),
                List.of(), ServicioReplay.hashCompleto(juego));
        Path archivo = temporal.resolve("v2").resolve("replay.json");
        ServicioReplay.guardar(v2, archivo);
        ReplayPartida cargado = ServicioReplay.cargar(archivo);

        assertEquals(2, cargado.version());
        assertEquals(v2.hashFinal(), cargado.hashFinal());
        assertTrue(ServicioReplay.reproducir(cargado, () -> new MotorPartida(partida())));
        assertFalse(ServicioReplay.reproducir(new ReplayPartida(2, 42,
                Map.of(), List.of(), "0".repeat(64)), () -> new MotorPartida(partida())));

        ReplayPartida v1 = new ReplayPartida(1, 42, List.of(), ServicioReplay.hash(juego));
        assertTrue(ServicioReplay.reproducir(v1, () -> new MotorPartida(partida())));
    }

    @Test
    void noAceptaComandosDespuesDeTerminarLaPartida() {
        Juego juego = partida();
        ReplayPartida alterado = new ReplayPartida(1, 8, List.of("salir", "mover este"),
                ServicioReplay.hash(juego));
        assertFalse(ServicioReplay.reproducir(alterado,
                () -> new MotorPartida(partida())));
    }

    @Test
    void rechazaFicherosMalformadosConExcepcionControlada() throws Exception {
        Path archivo = temporal.resolve("corrupto.json");
        for (String json : List.of(
                "no-json",
                "null",
                "{}",
                "{\"version\":2,\"comandos\":null,\"hashFinal\":\"0\"}",
                "{\"version\":2,\"comandos\":[null],\"hashFinal\":\"0\"}",
                "{\"version\":99,\"comandos\":[],\"hashFinal\":\"0\"}",
                "{\"version\":2,\"comandos\":[],\"hashFinal\":\"ilegible\"}"
        )) {
            Files.writeString(archivo, json);
            assertThrows(IOException.class, () -> ServicioReplay.cargar(archivo), json);
        }
    }

    @Test
    void rechazaComandosInvalidosAntesDeSobrescribirUnReplayValido() throws Exception {
        Path archivo = temporal.resolve("replay.json");
        Files.writeString(archivo, "partida anterior");
        ReplayPartida invalido = new ReplayPartida(2, 1, Map.of(),
                List.of("mover este", " ".repeat(10)), "0".repeat(64));
        assertThrows(IOException.class, () -> ServicioReplay.guardar(invalido, archivo));
        assertEquals("partida anterior", Files.readString(archivo));

        ReplayPartida muyLargo = new ReplayPartida(2, 1, Map.of(),
                List.of("x".repeat(2001)), "0".repeat(64));
        assertThrows(IOException.class, () -> ServicioReplay.guardar(muyLargo, archivo));
        assertEquals("partida anterior", Files.readString(archivo));
    }

    private static Juego partida() {
        return TestFixtures.juegoBasico(TestFixtures.consola());
    }
}
