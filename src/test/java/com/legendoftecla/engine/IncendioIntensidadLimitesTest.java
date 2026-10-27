package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.events.IncendioIniciado;
import com.legendoftecla.events.RuidoGenerado;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** La ignicion solo genera eventos si incrementa la intensidad real del fuego. */
class IncendioIntensidadLimitesTest {
    @Test
    void todasLasTransicionesDeIntensidadPublicanEventosSoloCuandoCambiaElFuego() {
        Posicion foco = new Posicion(1, 1);
        int[] intensidades = {-10, -1, 0, 1, 2, 3, 4, 100, Integer.MAX_VALUE};
        for (int inicial = 0; inicial <= 3; inicial++) {
            for (int solicitada : intensidades) {
                Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
                juego.getMapa().getCelda(foco).setNivelFuego(inicial);
                List<IncendioIniciado> inicios = new ArrayList<>();
                List<RuidoGenerado> ruidos = new ArrayList<>();
                juego.getBusEventos().suscribir(IncendioIniciado.class, inicios::add);
                juego.getBusEventos().suscribir(RuidoGenerado.class, ruidos::add);

                SistemaIncendios.iniciar(juego, foco, solicitada);

                int acotada = Math.max(0, Math.min(3, solicitada));
                int esperada = Math.max(inicial, acotada);
                int eventosEsperados = esperada > inicial ? 1 : 0;
                String caso = "inicial=" + inicial + " solicitada=" + solicitada;
                assertEquals(esperada, juego.getMapa().getCelda(foco).getNivelFuego(), caso);
                assertEquals(eventosEsperados, inicios.size(), caso);
                assertEquals(eventosEsperados, ruidos.size(), caso);
            }
        }
    }

    @Test
    void fuegoYaAlMaximoNoEmiteEventosConIntensidadExcesiva() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion foco = new Posicion(1, 1);
        juego.getMapa().getCelda(foco).setNivelFuego(3);
        List<IncendioIniciado> inicios = new ArrayList<>();
        List<RuidoGenerado> ruidos = new ArrayList<>();
        juego.getBusEventos().suscribir(IncendioIniciado.class, inicios::add);
        juego.getBusEventos().suscribir(RuidoGenerado.class, ruidos::add);

        for (int i = 0; i < 100; i++) {
            SistemaIncendios.iniciar(juego, foco, Integer.MAX_VALUE);
        }

        assertEquals(3, juego.getMapa().getCelda(foco).getNivelFuego());
        assertTrue(inicios.isEmpty());
        assertTrue(ruidos.isEmpty());
    }

    @Test
    void incrementoRealPublicaExactamenteUnEventoYUnaSenalDeRuido() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion foco = new Posicion(1, 1);
        juego.getMapa().getCelda(foco).setNivelFuego(2);
        List<IncendioIniciado> inicios = new ArrayList<>();
        List<RuidoGenerado> ruidos = new ArrayList<>();
        juego.getBusEventos().suscribir(IncendioIniciado.class, inicios::add);
        juego.getBusEventos().suscribir(RuidoGenerado.class, ruidos::add);

        SistemaIncendios.iniciar(juego, foco, 10);
        SistemaIncendios.iniciar(juego, foco, 10);
        SistemaIncendios.iniciar(juego, foco, 3);

        assertEquals(3, juego.getMapa().getCelda(foco).getNivelFuego());
        assertEquals(1, inicios.size());
        assertEquals(1, ruidos.size());
        assertEquals(foco, inicios.get(0).posicion());
    }
}
