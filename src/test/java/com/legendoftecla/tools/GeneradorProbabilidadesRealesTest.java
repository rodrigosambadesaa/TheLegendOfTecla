package com.legendoftecla.tools;

import com.legendoftecla.audio.GestorSonido;
import com.legendoftecla.constants.CondicionVictoria;
import com.legendoftecla.constants.Dificultad;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneradorProbabilidadesRealesTest {
    @BeforeAll
    static void silenciarAudio() {
        System.setProperty(GestorSonido.PROPIEDAD_DESACTIVADO, "true");
    }

    @Test
    void matrizCubreDificultadPoblacionMapaNivelesYCondicion() {
        List<GeneradorProbabilidadesReales.Escenario> matriz =
                GeneradorProbabilidadesReales.construirMatriz();

        assertEquals(32, matriz.size());
        assertEquals(32, matriz.stream().map(
                GeneradorProbabilidadesReales.Escenario::id).distinct().count());
        assertEquals(List.of("condicion", "dificultad", "mapa", "nivel_aliados",
                        "nivel_jugador", "poblacion"),
                matriz.stream().map(GeneradorProbabilidadesReales.Escenario::eje)
                        .distinct().sorted().toList());
    }

    @Test
    void mismaSemillaRepiteLaPartidaCompleta() throws Exception {
        GeneradorProbabilidadesReales.Escenario escenario =
                new GeneradorProbabilidadesReales.Escenario(
                        "TEST", "test", "10x10", Dificultad.NORMAL,
                        10, 10, 2, 10, 10, CondicionVictoria.SOLO_JUGADOR);

        var primera = GeneradorProbabilidadesReales.simular(escenario, 814L,
                GeneradorProbabilidadesReales.ConsolaMedicion.silenciosa());
        var segunda = GeneradorProbabilidadesReales.simular(escenario, 814L,
                GeneradorProbabilidadesReales.ConsolaMedicion.silenciosa());

        assertEquals(primera, segunda);
    }

    @Test
    void intervaloWilsonContieneLaProporcionObservada() {
        double[] intervalo = GeneradorProbabilidadesReales.wilson(50, 100);
        assertTrue(intervalo[0] < 0.5);
        assertTrue(intervalo[1] > 0.5);
        assertTrue(intervalo[0] > 0.39);
        assertTrue(intervalo[1] < 0.61);
    }
}
