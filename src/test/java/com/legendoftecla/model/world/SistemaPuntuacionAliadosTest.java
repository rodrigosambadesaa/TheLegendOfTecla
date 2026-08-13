package com.legendoftecla.model.world;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.engine.MotorPartida;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Mochila;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SistemaPuntuacionAliadosTest {
    @Test
    void puntuaSaludEnergiaProgresoYEvacuacionDeCadaAliado() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Aliado aliado = agregar(juego);

        assertEquals(600, SistemaPuntuacion.calcularAliado(juego, aliado).total());

        aliado.setPosicion(juego.getMapa().getObjetivo());
        assertTrue(juego.extraerAliado(aliado));
        assertEquals(1_000, SistemaPuntuacion.calcularAliado(juego, aliado).total());
    }

    @Test
    void elEstadoMuestraPuntuacionIndividualYTotalDelEscuadron() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        agregar(juego);

        String estado = new MotorPartida(juego).getEstadoAliados();

        assertTrue(estado.contains("puntuacion total=600"));
        assertTrue(estado.contains("Puntuacion 600"));
    }

    private Aliado agregar(Juego juego) {
        Posicion inicio = juego.getMapa().getInicio();
        Aliado aliado = new Aliado("Apoyo", inicio, new Mochila(4, 20), 2);
        juego.agregarAliado(aliado);
        juego.getMapa().getCelda(inicio).agregarAliado(aliado);
        return aliado;
    }
}
