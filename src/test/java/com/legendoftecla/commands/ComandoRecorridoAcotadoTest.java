package com.legendoftecla.commands;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComandoRecorridoAcotadoTest {
    @Test
    void recorridoCortoConservaElFormatoTradicional() {
        TestFixtures.CapturingConsole consola = TestFixtures.consola();
        Juego juego = TestFixtures.juegoBasico(consola);
        juego.getJugador().setPosicion(new Posicion(0, 1));
        juego.getJugador().registrarPosicion();

        new ComandoRecorrido(new CommandContext(juego)).ejecutar();

        assertTrue(consola.salida().contains("Recorrido: (0,0) (0,1) "));
        assertFalse(consola.salida().contains("ultimas"));
        assertEquals(2, juego.getJugador().getLongitudRecorrido());
    }

    @Test
    void veinteMilPasosSoloImprimenLosUltimosCien() {
        TestFixtures.CapturingConsole consola = TestFixtures.consola();
        Juego juego = TestFixtures.juegoBasico(consola);
        var jugador = juego.getJugador();
        for (int i = 0; i < 20_000; i++) {
            jugador.registrarPosicion();
        }
        jugador.setPosicion(new Posicion(2, 2));
        jugador.registrarPosicion();
        assertEquals(20_002, jugador.getLongitudRecorrido());

        assertTimeoutPreemptively(Duration.ofSeconds(3), () ->
                new ComandoRecorrido(new CommandContext(juego)).ejecutar());

        String salida = consola.salida();
        assertTrue(salida.contains("ultimas 100 de 20002 posiciones"));
        assertTrue(salida.contains("(2,2)"));
        assertTrue(salida.length() < 2_000, "La consola no debe volcar todo el historial");
        assertEquals(20_002, jugador.getLongitudRecorrido());
    }

    @Test
    void instantaneaRecienteEsDefensivaYValidada() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        var jugador = juego.getJugador();
        jugador.setPosicion(new Posicion(1, 2));
        jugador.registrarPosicion();
        List<Posicion> recientes = jugador.getRecorridoReciente(1);
        assertEquals(List.of(new Posicion(1, 2)), recientes);
        recientes.get(0).setColumna(0);
        assertEquals(new Posicion(1, 2), jugador.getRecorridoReciente(1).get(0));

        assertThrows(IllegalArgumentException.class, () -> jugador.getRecorridoReciente(0));
    }
}
