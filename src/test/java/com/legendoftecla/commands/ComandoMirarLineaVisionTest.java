package com.legendoftecla.commands;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.elements.EstadoPuerta;
import com.legendoftecla.model.elements.Puerta;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComandoMirarLineaVisionTest {
    @Test
    void noPermiteObservarDetrasDeUnMuroAunqueElDestinoSeaTransitable() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        juego.getMapa().getCelda(new Posicion(0, 1)).setTransitable(false);
        ComandoMirar mirar = new ComandoMirar(
                new CommandContext(juego), Direccion.ESTE, 2);

        ComandoException error = assertThrows(ComandoException.class, mirar::ejecutar);

        assertTrue(error.getMessage().contains("vision"));
    }

    @Test
    void unaPuertaCerradaBloqueaLaMiradaPeroUnaAbiertaLaPermite() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Puerta puerta = new Puerta("cerradura", EstadoPuerta.CERRADA,
                null, true, 10);
        juego.getMapa().getCelda(new Posicion(0, 1)).agregarElemento(puerta);
        ComandoMirar mirar = new ComandoMirar(
                new CommandContext(juego), Direccion.ESTE, 2);

        assertThrows(ComandoException.class, mirar::ejecutar);
        assertTrue(puerta.abrir(null));
        assertDoesNotThrow(mirar::ejecutar);
    }

    @Test
    void obstaculosFueraDeLaLineaNoImpidenObservar() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        juego.getMapa().getCelda(new Posicion(1, 1)).setTransitable(false);
        ComandoMirar mirar = new ComandoMirar(
                new CommandContext(juego), Direccion.ESTE, 2);

        assertDoesNotThrow(mirar::ejecutar);
    }

    @Test
    void mirarCeldaActualNoRequiereLineaDeTiro() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        juego.getMapa().getCelda(new Posicion(0, 1)).setTransitable(false);

        assertDoesNotThrow(() -> new ComandoMirar(new CommandContext(juego)).ejecutar());
    }
}
