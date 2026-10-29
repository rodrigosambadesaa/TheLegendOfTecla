package com.legendoftecla.ai;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.events.RuidoGenerado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Scout;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ControladorIAMemoriaInmutableTest {
    @Test
    void ruidoRecuerdaLaPosicionOriginalAunqueLaFuenteSeMueva() {
        Scout scout = scout();
        ControladorIA controlador = scout.getControladorIA();
        Posicion fuente = new Posicion(0, 0);

        assertTrue(controlador.percibir(new RuidoGenerado(
                Instant.EPOCH, fuente, 6, "movimiento")));
        fuente.setFila(1);
        fuente.setColumna(1);

        assertEquals(new Posicion(0, 0), controlador.getUltimaPosicionConocida());
    }

    @Test
    void consultaDeMemoriaNoPermiteModificarElEstadoInterno() {
        ControladorIA controlador = scout().getControladorIA();
        controlador.alertar(new Posicion(0, 0), 6);

        Posicion observada = controlador.getUltimaPosicionConocida();
        observada.setFila(2);
        observada.setColumna(2);

        assertEquals(new Posicion(0, 0), controlador.getUltimaPosicionConocida());
        assertEquals(NivelAlerta.ALERTA, controlador.getEstado());
    }

    @Test
    void unaAlertaExternaNoConservaLaReferenciaMutable() {
        ControladorIA controlador = scout().getControladorIA();
        Posicion alerta = new Posicion(0, 0);
        controlador.alertar(alerta, 6);
        alerta.setColumna(2);

        assertEquals(new Posicion(0, 0), controlador.getUltimaPosicionConocida());
    }

    @Test
    void modificarElObjetivoDeLaAccionNoAlteraLaMemoria() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Scout scout = scout();
        ControladorIA controlador = scout.getControladorIA();
        controlador.alertar(new Posicion(0, 0), 6);

        AccionIA accion = controlador.decidir(
                new ContextoIA(scout, juego, false, null, false, false));
        accion.objetivo().setColumna(2);

        assertEquals(new Posicion(0, 0), controlador.getUltimaPosicionConocida());
    }

    @Test
    void laMemoriaDeUnaVisionNoSigueAlJugadorDespues() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Scout scout = scout();
        ControladorIA controlador = scout.getControladorIA();

        controlador.decidir(new ContextoIA(scout, juego, true, null, false, false));
        juego.getJugador().getPosicion().setColumna(2);

        assertEquals(new Posicion(0, 0), controlador.getUltimaPosicionConocida());
    }

    private static Scout scout() {
        return new Scout("Observador", new Posicion(0, 1), new Mochila(2, 10), 4);
    }
}
