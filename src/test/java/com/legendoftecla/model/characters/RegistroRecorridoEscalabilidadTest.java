package com.legendoftecla.model.characters;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class RegistroRecorridoEscalabilidadTest {
    @Test
    void registrarVeinteMilPasosSinCopiarTodoElHistorial() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Jugador jugador = juego.getJugador();

        assertTimeoutPreemptively(Duration.ofSeconds(8), () -> {
            for (int i = 0; i < 20_000; i++) {
                jugador.registrarPosicion();
            }
        });

        List<Posicion> recorrido = jugador.getRecorrido();
        assertEquals(20_001, recorrido.size());
        assertEquals(new Posicion(0, 0), recorrido.get(0));
        assertEquals(new Posicion(0, 0), recorrido.get(20_000));
    }

    @Test
    void laPosicionOriginalPuedeCambiarSinCorromperPasosPrevios() {
        Jugador jugador = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        Posicion posicion = new Posicion(1, 1);
        jugador.setPosicion(posicion);
        jugador.registrarPosicion();

        posicion.setFila(2);
        jugador.registrarPosicion();

        assertEquals(List.of(new Posicion(0, 0), new Posicion(1, 1),
                new Posicion(2, 1)), jugador.getRecorrido());

        Posicion expuesta = jugador.getRecorrido().get(1);
        expuesta.setColumna(2);
        assertEquals(new Posicion(1, 1), jugador.getRecorrido().get(1));
    }

    @Test
    void establecerRecorridoYAnadirPosicionesRespetaOrdenEIndependencia() {
        Jugador jugador = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        Posicion externa = new Posicion(1, 0);
        jugador.setRecorrido(List.of(externa));
        externa.setFila(2);
        jugador.setPosicion(new Posicion(1, 2));

        jugador.registrarPosicion();

        assertEquals(List.of(new Posicion(1, 0), new Posicion(1, 2)),
                jugador.getRecorrido());
        assertEquals(2, jugador.getRecorrido().size());
    }
}
