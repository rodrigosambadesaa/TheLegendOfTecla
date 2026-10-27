package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.elements.EstadoPuerta;
import com.legendoftecla.model.elements.ParedDebil;
import com.legendoftecla.model.elements.Puerta;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DestruccionImpactoNuloTest {
    @Test
    void ceroDanioNoNotificaCambiosNiModificaResistencia() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion posicion = new Posicion(0, 1);
        ParedDebil pared = new ParedDebil("pared", 25);
        Puerta puerta = new Puerta("puerta", EstadoPuerta.BLINDADA,
                null, true, 30);
        juego.getMapa().getCelda(posicion).agregarElemento(pared);
        juego.getMapa().getCelda(posicion).agregarElemento(puerta);
        boolean transitable = juego.getMapa().esTransitable(posicion);

        for (int i = 0; i < 100; i++) {
            SistemaDestruccion.ResultadoDestruccion resultado =
                    SistemaDestruccion.danar(juego, posicion, 0);
            assertFalse(resultado.modificoMapa());
            assertTrue(resultado.danados().isEmpty());
            assertTrue(resultado.destruidos().isEmpty());
            assertEquals(25, pared.getResistencia());
            assertEquals(30, puerta.getResistencia());
            assertEquals(transitable, juego.getMapa().esTransitable(posicion));
        }
    }

    @Test
    void impactoPositivoInformaDanioSoloEnElementosDestructiblesIntactos() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion posicion = new Posicion(0, 1);
        ParedDebil pared = new ParedDebil("pared", 5);
        Puerta indestructible = new Puerta("puerta", EstadoPuerta.BLINDADA,
                null, false, 30);
        juego.getMapa().getCelda(posicion).agregarElemento(pared);
        juego.getMapa().getCelda(posicion).agregarElemento(indestructible);

        SistemaDestruccion.ResultadoDestruccion primero =
                SistemaDestruccion.danar(juego, posicion, 2);
        SistemaDestruccion.ResultadoDestruccion segundo =
                SistemaDestruccion.danar(juego, posicion, 3);
        SistemaDestruccion.ResultadoDestruccion tercero =
                SistemaDestruccion.danar(juego, posicion, 100);

        assertEquals(List.of("pared"), primero.danados());
        assertEquals(List.of(), primero.destruidos());
        assertEquals(List.of("pared"), segundo.destruidos());
        assertFalse(tercero.modificoMapa());
        assertEquals(0, pared.getResistencia());
        assertEquals(30, indestructible.getResistencia());
    }

    @Test
    void validacionesSiguenRechazandoDanioNegativoYPosicionesInvalidas() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        assertThrows(IllegalArgumentException.class,
                () -> SistemaDestruccion.danar(juego, new Posicion(0, 0), -1));
        assertThrows(IllegalArgumentException.class,
                () -> SistemaDestruccion.danar(juego, new Posicion(100, 100), 0));
    }
}
