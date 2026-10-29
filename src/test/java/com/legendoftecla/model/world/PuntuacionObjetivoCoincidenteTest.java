package com.legendoftecla.model.world;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Mochila;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PuntuacionObjetivoCoincidenteTest {
    @Test
    void noPremiaProgresoSiInicioYObjetivoCoincidenPeroJugadorEstaLejos() {
        Juego juego = juegoConObjetivoCoincidente();
        juego.getJugador().setPosicion(new Posicion(1, 1));

        var puntuacion = SistemaPuntuacion.calcular(juego,
                SistemaPuntuacion.EstadoFinalPartida.SALIDA_MANUAL);

        assertEquals(0, puntuacion.getProgreso());
    }

    @Test
    void premiaProgresoCompletoSiJugadorEstaEnElObjetivoCoincidente() {
        Juego juego = juegoConObjetivoCoincidente();

        var puntuacion = SistemaPuntuacion.calcular(juego,
                SistemaPuntuacion.EstadoFinalPartida.VICTORIA);

        assertEquals(150, puntuacion.getProgreso());
    }

    @Test
    void aplicaLaMismaReglaAUnAliadoFueraYDentroDelObjetivo() {
        Juego juego = juegoConObjetivoCoincidente();
        Aliado aliado = new Aliado("Apoyo", new Posicion(2, 1), new Mochila(3, 10), 2);
        juego.agregarAliado(aliado);

        assertEquals(0, SistemaPuntuacion.calcularAliado(juego, aliado).progreso());

        aliado.setPosicion(new Posicion(0, 0));
        assertEquals(200, SistemaPuntuacion.calcularAliado(juego, aliado).progreso());
    }

    @Test
    void mantieneLosPorcentajesHabitualesSiLaRutaTieneLongitudPositiva() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        juego.getJugador().setPosicion(new Posicion(1, 0));
        Aliado aliado = new Aliado("Apoyo", new Posicion(1, 0), new Mochila(3, 10), 2);
        juego.agregarAliado(aliado);

        assertEquals(38, SistemaPuntuacion.calcular(juego,
                SistemaPuntuacion.EstadoFinalPartida.SALIDA_MANUAL).getProgreso());
        assertEquals(50, SistemaPuntuacion.calcularAliado(juego, aliado).progreso());
    }

    private static Juego juegoConObjetivoCoincidente() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        juego.getMapa().setObjetivo(new Posicion(0, 0));
        return juego;
    }
}
