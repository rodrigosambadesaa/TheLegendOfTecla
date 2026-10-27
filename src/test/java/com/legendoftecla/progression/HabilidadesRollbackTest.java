package com.legendoftecla.progression;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.characters.Jugador;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HabilidadesRollbackTest {
    @Test
    void unaMejoraInvalidaNoQuedaMarcadaComoDesbloqueada() {
        Jugador jugador = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        ProgresionPersonaje progreso = jugador.getProgresion();
        progreso.restaurar(2, 0, Set.of());
        ArbolHabilidades arbol = CatalogoHabilidades.para(jugador);
        jugador.setSaludMaxima(1_000_000);

        assertThrows(IllegalArgumentException.class, () ->
                progreso.desbloquear(CatalogoHabilidades.RESISTENCIA, arbol, jugador));

        assertFalse(progreso.tiene(CatalogoHabilidades.RESISTENCIA));
        assertTrue(progreso.getDesbloqueadas().isEmpty());
        assertEquals(1_000_000, jugador.getSaludMaxima());

        jugador.setSaludMaxima(120);
        assertTrue(progreso.desbloquear(CatalogoHabilidades.RESISTENCIA, arbol, jugador));
        assertTrue(progreso.tiene(CatalogoHabilidades.RESISTENCIA));
        assertEquals(135, jugador.getSaludMaxima());
    }

    @Test
    void falloDeUnEfectoExternoNoDesbloqueaHabilidadesDependientes() {
        Jugador jugador = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        ProgresionPersonaje progreso = jugador.getProgresion();
        ArbolHabilidades arbol = new ArbolHabilidades();
        arbol.agregar(new Habilidad("previa", "Inicial",
                new RequisitoHabilidad(1, null),
                ignorado -> { throw new IllegalStateException("Efecto rechazado"); }));
        arbol.agregar(new Habilidad("posterior", "Dependiente",
                new RequisitoHabilidad(1, "previa"), ignorado -> { }));

        assertThrows(IllegalStateException.class,
                () -> progreso.desbloquear("previa", arbol, jugador));
        assertFalse(progreso.tiene("previa"));
        assertFalse(progreso.desbloquear("posterior", arbol, jugador));
        assertTrue(progreso.getDesbloqueadas().isEmpty());
    }

    @Test
    void desbloqueoCorrectoAplicaElEfectoUnaSolaVez() {
        Jugador jugador = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        ProgresionPersonaje progreso = jugador.getProgresion();
        AtomicInteger aplicaciones = new AtomicInteger();
        ArbolHabilidades arbol = new ArbolHabilidades();
        arbol.agregar(new Habilidad("efecto", "Valida",
                new RequisitoHabilidad(1, null),
                ignorado -> aplicaciones.incrementAndGet()));

        assertTrue(progreso.desbloquear("efecto", arbol, jugador));
        assertFalse(progreso.desbloquear("efecto", arbol, jugador));
        assertEquals(1, aplicaciones.get());
        assertEquals(Set.of("efecto"), progreso.getDesbloqueadas());
    }
}
