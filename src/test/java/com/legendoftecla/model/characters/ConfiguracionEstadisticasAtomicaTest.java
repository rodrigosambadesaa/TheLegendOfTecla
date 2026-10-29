package com.legendoftecla.model.characters;

import com.legendoftecla.TestFixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfiguracionEstadisticasAtomicaTest {
    @Test
    void visionInvalidaNoCambiaSaludNiEnergia() {
        Marine marine = personaje();
        marine.setSalud(85);
        marine.setEnergia(45);
        marine.setVisionTemporal(7);

        assertThrows(IllegalArgumentException.class,
                () -> marine.configurarEstadisticas(600, 500, 0));

        assertEquals(120, marine.getSaludMaxima());
        assertEquals(85, marine.getSalud());
        assertEquals(90, marine.getEnergiaMaxima());
        assertEquals(45, marine.getEnergia());
        assertEquals(2, marine.getVisionBase());
        assertEquals(7, marine.getVisionTemporal());
    }

    @Test
    void energiaInvalidaTampocoCambiaLaSalud() {
        Marine marine = personaje();
        assertThrows(IllegalArgumentException.class,
                () -> marine.configurarEstadisticas(800, -1, 10));
        assertEquals(120, marine.getSaludMaxima());
        assertEquals(120, marine.getSalud());
        assertEquals(90, marine.getEnergiaMaxima());
        assertEquals(90, marine.getEnergia());
        assertEquals(2, marine.getVisionBase());
    }

    @Test
    void configuracionValidaSeAplicaCompletaYReiniciaVisionTemporal() {
        Marine marine = personaje();
        marine.setVisionTemporal(10);
        marine.configurarEstadisticas(200, 150, 5);

        assertEquals(200, marine.getSaludMaxima());
        assertEquals(200, marine.getSalud());
        assertEquals(150, marine.getEnergiaMaxima());
        assertEquals(150, marine.getEnergia());
        assertEquals(5, marine.getVisionBase());
        assertEquals(0, marine.getVisionTemporal());
    }

    private static Marine personaje() {
        return (Marine) TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
    }
}
