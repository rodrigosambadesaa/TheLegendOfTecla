package com.legendoftecla.loader;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.exceptions.JuegoException;
import com.legendoftecla.model.world.DimensionesMapa;
import com.legendoftecla.model.world.Juego;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneracionEnemigosSinBloqueosTest {
    @TempDir Path temporal;

    @Test
    void escenarioDeFicherosSaturadoFallaSinBucleInfinito() throws Exception {
        Files.writeString(temporal.resolve("mapa.txt"), "3x3\n0,0\n2,2\n");
        Files.writeString(temporal.resolve("objetos.txt"), "");
        StringBuilder enemigos = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            enemigos.append("sectoid;E").append(i).append(";-1;-1\n");
        }
        Files.writeString(temporal.resolve("enemigos.txt"), enemigos.toString());

        CargadorJuegoDeFicheros cargador = new CargadorJuegoDeFicheros(
                TestFixtures.consola(), "Tecla", "marine", temporal,
                Dificultad.NORMAL, null, false);

        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            JuegoException error = assertThrows(JuegoException.class, cargador::cargarJuego);
            assertTrue(error.getMessage().contains("No quedan casillas"), error::getMessage);
        });
    }

    @Test
    void escenarioDeFicherosConEspacioDisponibleSeGenera() throws Exception {
        Files.writeString(temporal.resolve("mapa.txt"), "3x3\n0,0\n2,2\n");
        Files.writeString(temporal.resolve("objetos.txt"), "");
        Files.writeString(temporal.resolve("enemigos.txt"), "sectoid;E0;-1;-1\n");
        CargadorJuegoDeFicheros cargador = new CargadorJuegoDeFicheros(
                TestFixtures.consola(), "Tecla", "marine", temporal,
                Dificultad.NORMAL, null, false);

        Juego juego = assertTimeoutPreemptively(Duration.ofSeconds(5),
                cargador::cargarJuego);

        assertEquals(1, juego.getEnemigos().size());
        assertTrue(juego.getMapa().estaDentro(juego.getEnemigos().get(0).getPosicion()));
    }

    @Test
    void generadorAtlasEnMapaSaturadoFallaDeFormaControlada() {
        CargadorJuegoGrandeConAliados cargador = new CargadorJuegoGrandeConAliados(
                TestFixtures.consola(), "Tecla", "marine",
                Dificultad.NORMAL, new DimensionesMapa(3, 3), false, 1);

        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    cargador::cargarJuego);
            assertTrue(error.getMessage().contains("No quedan casillas"),
                    error::getMessage);
        });
    }

    @Test
    void generadorPorDefectoNoBloqueaUnMapaDemasiadoPequeno() {
        CargadorJuegoPorDefecto cargador = new CargadorJuegoPorDefecto(
                TestFixtures.consola(), "Tecla", "marine",
                Dificultad.NORMAL, new DimensionesMapa(3, 3), false);

        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    cargador::cargarJuego);
            assertTrue(error.getMessage().contains("No quedan casillas"),
                    error::getMessage);
        });
    }

    @Test
    void generadorPorDefectoConEspacioDisponibleSeCompleta() {
        CargadorJuegoPorDefecto cargador = new CargadorJuegoPorDefecto(
                TestFixtures.consola(), "Tecla", "marine",
                Dificultad.NORMAL, new DimensionesMapa(15, 15), false);

        Juego juego = assertTimeoutPreemptively(Duration.ofSeconds(5),
                cargador::cargarJuego);
        assertTrue(!juego.getEnemigos().isEmpty());
    }

    @Test
    void atlasConEspacioSuficienteMantieneGeneracionNormal() {
        CargadorJuegoGrandeConAliados cargador = new CargadorJuegoGrandeConAliados(
                TestFixtures.consola(), "Tecla", "marine",
                Dificultad.NORMAL, new DimensionesMapa(15, 15), false, 1);

        Juego juego = assertTimeoutPreemptively(Duration.ofSeconds(5),
                cargador::cargarJuego);

        assertEquals(18, juego.getEnemigos().size());
    }
}
