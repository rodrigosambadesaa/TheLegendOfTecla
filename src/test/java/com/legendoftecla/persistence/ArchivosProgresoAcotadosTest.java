package com.legendoftecla.persistence;

import com.legendoftecla.missions.AlcanzarSalida;
import com.legendoftecla.missions.Campana;
import com.legendoftecla.missions.Mision;
import com.legendoftecla.progression.ProgresionPersonaje;
import com.legendoftecla.stats.EstadisticasGlobales;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchivosProgresoAcotadosTest {
    @TempDir Path directorio;

    @Test
    void rechazaEstadisticasGigantesAunqueElJsonSeaValido() throws Exception {
        EstadisticasGlobales globales = new EstadisticasGlobales();
        globales.registrarPartida(true, 7);
        Path archivo = directorio.resolve("estadisticas.json");
        PersistenciaEstadisticas.guardar(globales, archivo);
        String base = Files.readString(archivo);
        Files.writeString(archivo, base + " ".repeat(ArchivoJsonAcotado.MAX_BYTES));

        IOException error = assertThrows(IOException.class,
                () -> PersistenciaEstadisticas.cargar(archivo));

        assertTrue(error.getMessage().contains("1 MiB"));
    }

    @Test
    void rechazaCampanasGigantesSinModificarElProgreso() throws Exception {
        Campana campana = campana();
        Path archivo = directorio.resolve("campana.json");
        PersistenciaCampana.guardar(campana, archivo);
        String base = Files.readString(archivo);
        Files.writeString(archivo, base + " ".repeat(ArchivoJsonAcotado.MAX_BYTES));

        IOException error = assertThrows(IOException.class,
                () -> PersistenciaCampana.restaurar(campana, archivo));

        assertTrue(error.getMessage().contains("1 MiB"));
        assertEquals(1, campana.getIndice());
        assertEquals(2, campana.getProgresion().getNivel());
        assertEquals(13, campana.getProgresion().getExperiencia());
    }

    @Test
    void aceptaExactamenteElLimiteYRechazaElByteSiguiente() throws Exception {
        Path archivo = directorio.resolve("al-limite.json");
        PersistenciaEstadisticas.guardar(new EstadisticasGlobales(), archivo);
        String base = Files.readString(archivo);
        int relleno = ArchivoJsonAcotado.MAX_BYTES
                - base.getBytes(StandardCharsets.UTF_8).length;
        String maximo = base + " ".repeat(relleno);
        Files.writeString(archivo, maximo);

        assertEquals(ArchivoJsonAcotado.MAX_BYTES, Files.size(archivo));
        assertEquals(0, PersistenciaEstadisticas.cargar(archivo).getPartidas());

        Files.writeString(archivo, maximo + " ");
        assertThrows(IOException.class, () -> PersistenciaEstadisticas.cargar(archivo));
    }

    @Test
    void rechazaUtf8MalformadoSinSustituirCaracteres() throws Exception {
        Path archivo = directorio.resolve("bytes-invalidos.json");
        Files.write(archivo, new byte[] {(byte) 0xc3, (byte) 0x28});

        assertThrows(IOException.class, () -> PersistenciaEstadisticas.cargar(archivo));
        assertThrows(IOException.class,
                () -> PersistenciaCampana.restaurar(campana(), archivo));
    }

    @Test
    void escrituraSobredimensionadaNoTruncaArchivoExistente() throws Exception {
        Path archivo = directorio.resolve("preservado.json");
        Files.writeString(archivo, "anterior");

        assertThrows(IOException.class, () -> ArchivoJsonAcotado.escribir(
                archivo, "x".repeat(ArchivoJsonAcotado.MAX_BYTES + 1)));

        assertEquals("anterior", Files.readString(archivo));
    }

    private static Campana campana() {
        Mision mision = new Mision("m", "M", new AlcanzarSalida(), List.of(), List.of());
        ProgresionPersonaje progreso = new ProgresionPersonaje();
        progreso.restaurar(2, 13, Set.of("resistencia"));
        Campana campana = new Campana("campana", List.of(mision, mision), progreso);
        campana.avanzar();
        return campana;
    }
}
