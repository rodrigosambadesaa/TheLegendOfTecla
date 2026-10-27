package com.legendoftecla.progression;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Propiedades de conservacion de XP y proteccion contra desbordamientos. */
class ProgresionExperienciaLimitesTest {
    @Test
    void acumulaExperienciaMaximaSinPerderNiveles() {
        ProgresionPersonaje progresion = new ProgresionPersonaje();

        int ganados = progresion.ganarExperiencia(Integer.MAX_VALUE);

        assertEquals(progresion.getNivel() - 1, ganados);
        assertEquals((long) Integer.MAX_VALUE, experienciaTotal(progresion));
        assertTrue(progresion.getExperiencia() >= 0);
        assertTrue(progresion.getExperiencia() < progresion.experienciaSiguienteNivel());
    }

    @Test
    void sumaLaExperienciaPreexistenteSinDesbordarInt() {
        ProgresionPersonaje progresion = new ProgresionPersonaje();
        progresion.restaurar(2, 150, Set.of());

        progresion.ganarExperiencia(Integer.MAX_VALUE);

        assertEquals((long) Integer.MAX_VALUE + 250, experienciaTotal(progresion));
        assertTrue(progresion.getExperiencia() >= 0);
    }

    @Test
    void aceptaNivelesAltosConUmbralesNoDesbordados() {
        ProgresionPersonaje progresion = new ProgresionPersonaje();
        progresion.restaurar(Integer.MAX_VALUE, 0, Set.of("resistencia"));

        assertEquals(Integer.MAX_VALUE, progresion.experienciaSiguienteNivel());
        assertEquals(0, progresion.ganarExperiencia(500));
        assertEquals(Integer.MAX_VALUE, progresion.getNivel());
        assertEquals(500, progresion.getExperiencia());
    }

    @Test
    void restauraSoloLaExperienciaPermitidaParaNivelUno() {
        ProgresionPersonaje progresion = new ProgresionPersonaje();
        assertThrows(IllegalArgumentException.class,
                () -> progresion.restaurar(1, 100, Set.of()));
        assertThrows(IllegalArgumentException.class,
                () -> progresion.ganarExperiencia(-1));
        assertEquals(1, progresion.getNivel());
        assertEquals(0, progresion.getExperiencia());
    }

    private static long experienciaTotal(ProgresionPersonaje progreso) {
        long nivel = progreso.getNivel();
        return (nivel - 1) * nivel * 50 + progreso.getExperiencia();
    }
}
