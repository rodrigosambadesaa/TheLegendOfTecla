package com.legendoftecla.stats;

import com.legendoftecla.persistence.PersistenciaEstadisticas;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Las estadisticas nunca deben pasar a ser negativas por desbordamiento int. */
class EstadisticasGlobalesLimitesTest {
    @TempDir Path directorio;

    @Test
    void saturacionPreservaTodosLosContadoresDePartida() {
        int casiMaximo = Integer.MAX_VALUE - 2;
        EstadisticasGlobales globales = new EstadisticasGlobales();
        globales.restaurar(new EstadisticasGlobales.Snapshot(
                casiMaximo, casiMaximo, casiMaximo, casiMaximo,
                casiMaximo, casiMaximo, casiMaximo, casiMaximo,
                casiMaximo, casiMaximo, casiMaximo, casiMaximo));
        EstadisticasPartida.Snapshot detalle = new EstadisticasPartida.Snapshot(
                50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50);

        globales.registrarPartida(true, 50, detalle);
        globales.registrarPartida(false, 50, detalle);

        assertEquals(Integer.MAX_VALUE, globales.getPartidas());
        assertEquals(Integer.MAX_VALUE - 1, globales.getVictorias());
        assertEquals(Integer.MAX_VALUE - 1, globales.getDerrotas());
        assertEquals(Integer.MAX_VALUE, globales.getTurnos());
        assertEquals(Integer.MAX_VALUE, globales.getMuertes());
        assertEquals(Integer.MAX_VALUE, globales.getEnemigosEliminados());
        assertEquals(Integer.MAX_VALUE, globales.getAliadosEvacuados());
        assertEquals(Integer.MAX_VALUE, globales.getDanoCausado());
        assertEquals(Integer.MAX_VALUE, globales.getDanoRecibido());
        assertEquals(Integer.MAX_VALUE, globales.getIncendiosApagados());
        assertEquals(Integer.MAX_VALUE, globales.getObjetosUsados());
        assertEquals(Integer.MAX_VALUE, globales.getPasos());
    }

    @Test
    void valoresNegativosNoReducenTotalesAcumulados() {
        EstadisticasGlobales globales = new EstadisticasGlobales();
        globales.registrarPartida(true, 10, new EstadisticasPartida.Snapshot(
                9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 0));
        EstadisticasGlobales.Snapshot inicial = globales.snapshot();

        globales.registrarPartida(false, -100, new EstadisticasPartida.Snapshot(
                -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 0));

        assertEquals(2, globales.getPartidas());
        assertEquals(1, globales.getVictorias());
        assertEquals(1, globales.getDerrotas());
        assertEquals(inicial.turnos(), globales.getTurnos());
        assertEquals(inicial.danoCausado(), globales.getDanoCausado());
        assertEquals(inicial.muertes(), globales.getMuertes());
        assertEquals(inicial.pasos(), globales.getPasos());
    }

    @Test
    void resultadosSaturadosSiguenPudiendoGuardarseYCargarse() throws Exception {
        EstadisticasGlobales globales = new EstadisticasGlobales();
        globales.restaurar(new EstadisticasGlobales.Snapshot(
                Integer.MAX_VALUE, 0, 0, Integer.MAX_VALUE,
                0, 0, 0, 0, 0, 0, 0, 0));
        globales.registrarPartida(true, 10);
        Path archivo = directorio.resolve("nested/global.json");

        PersistenciaEstadisticas.guardar(globales, archivo);
        EstadisticasGlobales restauradas = PersistenciaEstadisticas.cargar(archivo);

        assertEquals(globales.snapshot(), restauradas.snapshot());
        assertEquals(Integer.MAX_VALUE, restauradas.getPartidas());
        assertTrue(restauradas.getTurnos() >= 0);
    }
}
