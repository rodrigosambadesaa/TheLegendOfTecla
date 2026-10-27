package com.legendoftecla.commands;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.items.Granada;
import com.legendoftecla.model.items.TipoGranada;
import com.legendoftecla.model.world.Juego;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComandosDistanciaExtremaTest {
    @Test
    void ataqueRechazaDesbordamientosSinExcepcionesInternas() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        CommandContext contexto = new CommandContext(juego);
        int vida = juego.getJugador().getSalud();
        int energia = juego.getJugador().getEnergia();

        assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
            for (int digitos = 11; digitos <= 80; digitos += 3) {
                String alcance = "9".repeat(digitos) + "e";
                assertThrows(ComandoException.class,
                        () -> new ComandoAtacar(contexto, alcance, "todos").ejecutar(),
                        "longitud " + digitos);
            }
            assertThrows(ComandoException.class,
                    () -> new ComandoAtacar(contexto, "2147483647e", "todos").ejecutar());
            assertThrows(ComandoException.class,
                    () -> new ComandoAtacar(contexto, "2147483648e", "todos").ejecutar());
        });
        assertEquals(vida, juego.getJugador().getSalud());
        assertEquals(energia, juego.getJugador().getEnergia());
    }

    @Test
    void explosivosRechazanDesbordamientosSinConsumirInventario() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        CommandContext contexto = new CommandContext(juego);
        Granada granada = new Granada("Prueba", "Ensayo", 1, TipoGranada.FRAGMENTACION);
        assertTrue(juego.getJugador().getMochila().guardar(granada));

        assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
            for (int digitos = 11; digitos <= 80; digitos += 3) {
                String alcance = "9".repeat(digitos) + "n";
                assertThrows(ComandoException.class,
                        () -> new ComandoLanzarExplosivo(contexto, alcance, "Prueba")
                                .ejecutar(), "longitud " + digitos);
            }
            assertThrows(ComandoException.class,
                    () -> new ComandoLanzarExplosivo(contexto, "2147483647n", "Prueba")
                            .ejecutar());
            assertThrows(ComandoException.class,
                    () -> new ComandoLanzarExplosivo(contexto, "2147483648n", "Prueba")
                            .ejecutar());
        });
        assertEquals(1, juego.getJugador().getMochila().getObjetos().size());
        assertEquals(granada, juego.getJugador().getMochila().getObjetos().get(0));
    }

    @Test
    void erroresDeFormatoNoSeTransformanEnAtaques() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        CommandContext contexto = new CommandContext(juego);
        assertThrows(IllegalArgumentException.class,
                () -> new ComandoAtacar(contexto, "-1e", "todos"));
        assertThrows(IllegalArgumentException.class,
                () -> new ComandoLanzarExplosivo(contexto, "-1e", "Prueba"));
    }
}
