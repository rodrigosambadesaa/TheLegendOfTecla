package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.items.Botiquin;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrioridadAliadosTest {
    @Test
    void ayudarAlJugadorTienePrioridadSobreExplorar() throws Exception {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Aliado aliado = agregarAliadoEnInicio(juego);
        aliado.getMochila().guardar(new Botiquin("apoyo", "Cura al jugador", 1, 25));
        juego.getJugador().recibirDanio(20);
        Posicion inicio = juego.getMapa().getInicio();

        new MotorPartida(juego).ejecutarComando("mirar");

        assertEquals(juego.getJugador().getSaludMaxima(), juego.getJugador().getSalud());
        assertEquals(inicio, aliado.getPosicion());
    }

    @Test
    void sinAyudaPendienteElAliadoExploraUnaCeldaTransitable() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Aliado aliado = agregarAliadoEnInicio(juego);
        Posicion inicio = juego.getMapa().getInicio();

        new MotorPartida(juego).ejecutarComando("mirar");

        assertNotEquals(inicio, aliado.getPosicion());
        assertTrue(juego.getMapa().esTransitable(aliado.getPosicion()));
    }

    private Aliado agregarAliadoEnInicio(Juego juego) {
        Posicion inicio = juego.getMapa().getInicio();
        Aliado aliado = new Aliado("Apoyo", inicio, new Mochila(6, 30), 3);
        juego.agregarAliado(aliado);
        juego.getMapa().getCelda(inicio).agregarAliado(aliado);
        return aliado;
    }
}
