package com.legendoftecla.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Los replays no admiten datos malformados ni lecturas que superen su limite. */
class ReplayLecturaSeguraTest {
    @TempDir Path temporal;

    @Test
    void rechazoDeUtf8InvalidoDentroDeUnReplayEstructuralmenteValido() throws Exception {
        Path archivo = temporal.resolve("invalid.json");
        byte[] prefijo = ("{\"version\":1,\"seed\":1,\"comandos\":[],"
                + "\"hashFinal\":\"").getBytes(StandardCharsets.UTF_8);
        byte[] sufijo = "\"}".getBytes(StandardCharsets.UTF_8);
        byte[] corrupto = new byte[prefijo.length + 1 + sufijo.length];
        System.arraycopy(prefijo, 0, corrupto, 0, prefijo.length);
        corrupto[prefijo.length] = (byte) 0xC3;
        System.arraycopy(sufijo, 0, corrupto, prefijo.length + 1, sufijo.length);
        Files.write(archivo, corrupto);

        assertThrows(IOException.class, () -> ServicioReplay.cargar(archivo));
    }

    @Test
    void replaySobredimensionadoFallaDeFormaControlada() throws Exception {
        Path archivo = temporal.resolve("grande.json");
        try (SeekableByteChannel canal = Files.newByteChannel(archivo,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            canal.position(16L * 1024 * 1024);
            canal.write(ByteBuffer.wrap(new byte[] {'x'}));
        }

        IOException error = assertThrows(IOException.class,
                () -> ServicioReplay.cargar(archivo));

        assertTrue(error.getMessage().contains("16 MiB"));
    }

    @Test
    void replayHistoricoValidoConservaLaCompatibilidad() throws Exception {
        Path archivo = temporal.resolve("replay.json");
        ReplayPartida original = new ReplayPartida(1, 31L,
                List.of("mirar", "inventario"), "huella-historica");

        ServicioReplay.guardar(original, archivo);
        ReplayPartida restaurado = ServicioReplay.cargar(archivo);

        assertEquals(original.version(), restaurado.version());
        assertEquals(original.seed(), restaurado.seed());
        assertEquals(original.comandos(), restaurado.comandos());
        assertEquals(original.hashFinal(), restaurado.hashFinal());
    }
}
