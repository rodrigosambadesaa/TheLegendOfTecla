package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.characters.Jugador;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.persistence.PersistenciaProgresoLocal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NivelInicialIncrementalTest {
    @TempDir Path temporal;

    @Test
    void nivelesConsecutivosCoincidenConAsignacionDirecta() {
        for (int nivel : new int[] {2, 9, 11, 30, 70, 100}) {
            Jugador directo = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
            Jugador escalonado = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
            ServicioNivelInicial.aplicar(directo, nivel);

            for (int siguiente = 2; siguiente <= nivel; siguiente++) {
                ServicioNivelInicial.aplicar(escalonado, siguiente);
            }
            assertIguales(directo, escalonado);
        }
    }

    @Test
    void pasarDeNivelDiezATreintaSoloAnadeVeinteBonificaciones() {
        Jugador jugador = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        ServicioNivelInicial.aplicar(jugador, 10);
        int salud = jugador.getSaludMaxima();
        int energia = jugador.getEnergiaMaxima();
        int vision = jugador.getVisionBase();

        ServicioNivelInicial.aplicar(jugador, 30);
        assertEquals(salud + 20 * 3, jugador.getSaludMaxima());
        assertEquals(energia + 20 * 4, jugador.getEnergiaMaxima());
        assertEquals(vision + 2, jugador.getVisionBase());
        assertEquals(30, jugador.getProgresion().getNivel());

        ServicioNivelInicial.aplicar(jugador, 30);
        assertEquals(salud + 20 * 3, jugador.getSaludMaxima());
    }

    @Test
    void cargaDelPerfilNoDuplicaMejorasDelNivelConfigurado() throws Exception {
        Juego fuente = TestFixtures.juegoBasico(TestFixtures.consola());
        ServicioNivelInicial.aplicar(fuente.getJugador(), 45);
        Path perfil = temporal.resolve("progreso.json");
        PersistenciaProgresoLocal.guardar(fuente, perfil);

        Juego destino = TestFixtures.juegoBasico(TestFixtures.consola());
        ServicioNivelInicial.aplicar(destino.getJugador(), 20);
        PersistenciaProgresoLocal.aplicar(destino, perfil);

        assertIguales(fuente.getJugador(), destino.getJugador());
        assertEquals(45, destino.getJugador().getProgresion().getNivel());
    }

    private static void assertIguales(Jugador esperado, Jugador actual) {
        assertEquals(esperado.getProgresion().getNivel(), actual.getProgresion().getNivel());
        assertEquals(esperado.getSaludMaxima(), actual.getSaludMaxima());
        assertEquals(esperado.getSalud(), actual.getSalud());
        assertEquals(esperado.getEnergiaMaxima(), actual.getEnergiaMaxima());
        assertEquals(esperado.getEnergia(), actual.getEnergia());
        assertEquals(esperado.getVisionBase(), actual.getVisionBase());
    }
}
