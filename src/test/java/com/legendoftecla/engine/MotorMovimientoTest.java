package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MotorMovimientoTest {
    @Test
    void noMueveFueraDelMapaYComunicaElErrorSinFinalizarLaPartida() {
        TestFixtures.CapturingConsole consola = TestFixtures.consola();
        Juego juego = TestFixtures.juegoBasico(consola);
        MotorPartida motor = new MotorPartida(juego);
        consola.limpiar();

        boolean continua = motor.ejecutarComando("mover norte");

        assertTrue(continua);
        assertEquals(new Posicion(0, 0), juego.getJugador().getPosicion());
        assertTrue(consola.salida().contains("Error de comando"));
    }

    @Test
    void noMueveACeldaNoTransitableYMantieneElEstado() {
        TestFixtures.CapturingConsole consola = TestFixtures.consola();
        Juego juego = TestFixtures.juegoBasico(consola);
        juego.getMapa().getCelda(new Posicion(0, 1)).setTransitable(false);
        MotorPartida motor = new MotorPartida(juego);
        consola.limpiar();

        boolean continua = motor.ejecutarComando("mover este");

        assertTrue(continua);
        assertEquals(new Posicion(0, 0), juego.getJugador().getPosicion());
        assertTrue(consola.salida().contains("Error de comando"));
    }
}
