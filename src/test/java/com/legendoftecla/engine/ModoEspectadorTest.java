package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.model.world.SistemaPuntuacion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModoEspectadorTest {
    @Test
    void losAliadosContinuanAutomaticamenteTrasMorirElJugador() {
        TestFixtures.CapturingConsole consola = TestFixtures.consola();
        Juego juego = TestFixtures.juegoBasico(consola);
        Aliado aliado = new Aliado("Superviviente", juego.getMapa().getInicio(),
                new Mochila(4, 20), 3);
        juego.agregarAliado(aliado);
        juego.getMapa().getCelda(aliado.getPosicion()).agregarAliado(aliado);
        MotorPartida motor = new MotorPartida(juego);
        Posicion inicio = aliado.getPosicion();

        juego.getJugador().recibirDanio(juego.getJugador().getSaludMaxima());
        assertFalse(motor.ejecutarComando("mirar"));

        assertFalse(motor.isFinalizada());
        assertTrue(motor.isModoEspectadorDisponible());
        assertTrue(consola.salida().contains("Pulsa Play"));
        assertTrue(motor.avanzarTurnoEspectador());
        assertNotEquals(inicio, aliado.getPosicion());

        for (int turno = 0; turno < 12 && !motor.isFinalizada(); turno++) {
            motor.avanzarTurnoEspectador();
        }

        assertTrue(motor.isFinalizada());
        assertFalse(motor.isModoEspectadorDisponible());
        assertEquals(1, juego.getAliadosExtraidos());
        assertEquals(SistemaPuntuacion.EstadoFinalPartida.VICTORIA, motor.getEstadoFinal());
        assertEquals(MotorPartida.ResultadoBatalla.VICTORIA_HUMANA,
                motor.getResultadoBatalla());
        assertTrue(consola.salida().contains("VICTORIA HUMANA"));
    }

    @Test
    void laEliminacionDelEscuadronProduceVictoriaEnemiga() {
        TestFixtures.CapturingConsole consola = TestFixtures.consola();
        Juego juego = TestFixtures.juegoBasico(consola);
        Aliado aliado = new Aliado("Ultimo aliado", juego.getMapa().getInicio(),
                new Mochila(4, 20), 3);
        juego.agregarAliado(aliado);
        juego.getMapa().getCelda(aliado.getPosicion()).agregarAliado(aliado);
        MotorPartida motor = new MotorPartida(juego);

        juego.getJugador().recibirDanio(juego.getJugador().getSaludMaxima());
        motor.ejecutarComando("mirar");
        aliado.recibirDanio(aliado.getSaludMaxima());

        assertFalse(motor.avanzarTurnoEspectador());
        assertEquals(SistemaPuntuacion.EstadoFinalPartida.MUERTE, motor.getEstadoFinal());
        assertEquals(MotorPartida.ResultadoBatalla.VICTORIA_ENEMIGA,
                motor.getResultadoBatalla());
        assertTrue(consola.salida().contains("VICTORIA ENEMIGA"));
    }
}
