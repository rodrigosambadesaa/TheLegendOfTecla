package com.legendoftecla.achievements;

import com.legendoftecla.events.BusEventos;
import com.legendoftecla.events.PersonajeAtacado;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.stats.EstadisticasGlobales;
import com.legendoftecla.stats.EstadisticasPartida;
import com.legendoftecla.persistence.PersistenciaEstadisticas;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogrosYEstadisticasTest {
    @TempDir Path temporal;
    @Test
    void eventosActualizanEstadisticasYLogroNoSeDuplica() {
        BusEventos bus = new BusEventos();
        try (EstadisticasPartida estadisticas = new EstadisticasPartida(bus);
                GestorLogros logros = new GestorLogros(bus, estadisticas)) {
            PersonajeAtacado ataque = new PersonajeAtacado(Instant.EPOCH,
                    "Tecla", "Sectoid", new Posicion(0, 0), new Posicion(0, 1));
            bus.publicar(ataque);
            bus.publicar(ataque);
            assertEquals(2, estadisticas.getDisparos());
            assertTrue(logros.getDesbloqueados().contains("primer-contacto"));
            assertEquals(0, logros.evaluar().size());
        }
    }

    @Test
    void estadisticasGlobalesSeparanVictoriasYDerrotas() {
        EstadisticasGlobales globales = new EstadisticasGlobales();
        globales.registrarPartida(true, 10);
        globales.registrarPartida(false, 5);
        assertEquals(2, globales.getPartidas());
        assertEquals(1, globales.getVictorias());
        assertEquals(1, globales.getDerrotas());
        assertEquals(15, globales.getTurnos());
    }

    @Test
    void estadisticasGlobalesTienenRoundTripOpcional() throws Exception {
        EstadisticasGlobales globales = new EstadisticasGlobales();
        globales.registrarPartida(true, 8);
        Path archivo = temporal.resolve("estadisticas.json");
        PersistenciaEstadisticas.guardar(globales, archivo);

        EstadisticasGlobales cargadas = PersistenciaEstadisticas.cargar(archivo);

        assertEquals(globales.snapshot(), cargadas.snapshot());
    }
}
