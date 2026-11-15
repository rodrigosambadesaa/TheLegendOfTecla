package com.legendoftecla.persistence;

import com.legendoftecla.exceptions.JuegoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Contrato de seguridad de archivos compartido por las tres variantes. */
class PersistenciaParidadSeguridadTest {
    @TempDir Path temporal;

    @Test
    void reemplazoAtomicoConservaResultadoYNoDejaTemporales() throws Exception {
        Path destino = temporal.resolve("anidado").resolve("partida.json");
        EscrituraAtomica.escribir(destino, "primera version");
        EscrituraAtomica.escribir(destino, "segunda version");
        assertEquals("segunda version", Files.readString(destino));
        try (Stream<Path> archivos = Files.list(destino.getParent())) {
            assertFalse(archivos.anyMatch(path ->
                    path.getFileName().toString().startsWith(".tecla-save-")));
        }
    }

    @Test
    void jsonExcesivoNoSobrescribeElContenidoAnterior() throws Exception {
        Path destino = temporal.resolve("progreso.json");
        Files.writeString(destino, "valor anterior");
        assertThrows(IOException.class, () -> ArchivoJsonAcotado.escribir(
                destino, "x".repeat(ArchivoJsonAcotado.MAX_BYTES + 1)));
        assertEquals("valor anterior", Files.readString(destino));
    }

    @Test
    void jsonNoAceptaUtf8Malformado() throws Exception {
        Path destino = temporal.resolve("estadisticas.json");
        Files.write(destino, new byte[] {(byte) 0xc3, (byte) 0x28});
        assertThrows(IOException.class, () -> ArchivoJsonAcotado.leer(destino));
    }

    @Test
    void savegameNoAceptaUtf8Malformado() throws Exception {
        Path destino = temporal.resolve("partida.json");
        Files.write(destino, new byte[] {(byte) 0xc3, (byte) 0x28});
        assertThrows(JuegoException.class, () -> PersistenciaPartida.cargar(destino, null));
    }

    @Test
    void savegameExcesivoSeRechazaAntesDeDeserializar() throws Exception {
        Path destino = temporal.resolve("partida-gigante.json");
        try (var canal = Files.newByteChannel(destino, StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE)) {
            canal.position(PersistenciaPartida.MAX_PARTIDA_BYTES);
            canal.write(ByteBuffer.wrap(new byte[] {'x'}));
        }
        assertThrows(JuegoException.class, () -> PersistenciaPartida.cargar(destino, null));
    }
}
