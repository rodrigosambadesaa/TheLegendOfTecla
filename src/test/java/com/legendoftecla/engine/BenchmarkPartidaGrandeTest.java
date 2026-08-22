package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Scout;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.model.world.Juego;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BenchmarkPartidaGrandeTest {

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void rendimientoPartidaGrandeFluyeSinDemoras() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Mapa mapaGrande = new Mapa("Mapa Gigante", "Escenario masivo de pruebas",
                100, 100, new Posicion(0, 0), new Posicion(99, 99));
        for (int fila = 0; fila < 100; fila++) {
            for (int columna = 0; columna < 100; columna++) {
                mapaGrande.setCelda(fila, columna, new Celda("Celda " + fila + "," + columna, true));
            }
        }
        juego.setMapa(mapaGrande);

        // Añadir 30 aliados
        for (int i = 0; i < 30; i++) {
            Aliado aliado = new Aliado("Aliado_" + i, new Posicion(i % 10, (i * 3) % 100),
                    new Mochila(10, 100), 4);
            juego.agregarAliado(aliado);
            mapaGrande.getCelda(aliado.getPosicion()).agregarAliado(aliado);
        }

        // Añadir 30 enemigos
        for (int i = 0; i < 30; i++) {
            Enemigo enemigo = new Scout("Enemigo_" + i, new Posicion(50 + (i % 20), (i * 4) % 100),
                    new Mochila(10, 100), 4);
            juego.agregarEnemigo(enemigo);
            mapaGrande.getCelda(enemigo.getPosicion()).agregarEnemigo(enemigo);
        }

        MotorPartida motor = new MotorPartida(juego);

        long inicio = System.currentTimeMillis();
        for (int turn = 0; turn < 20; turn++) {
            motor.ejecutarComando("descansar");
            if (motor.isFinalizada()) break;
        }
        long duracion = System.currentTimeMillis() - inicio;

        assertTrue(duracion < 3000, "20 turnos con 60 combatientes en mapa 100x100 tardaron " + duracion + " ms");
    }
}
