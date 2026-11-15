package com.legendoftecla.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Contrato compatible de replays historicos v1 para las tres variantes. */
class ReplayParidadSeguridadTest {
    @TempDir Path temporal;

    @Test
    void replayV1ValidoSePuedeGuardarYRecuperar() throws Exception {
        ReplayPartida replay = new ReplayPartida(1, 42L, List.of("mirar"), "huella-v1");
        Path destino = temporal.resolve("anidado/replay.json");
        ServicioReplay.guardar(replay, destino);
        assertEquals(replay, ServicioReplay.cargar(destino));
    }

    @Test
    void replayV1InvalidoNoReemplazaElValido() throws Exception {
        ReplayPartida replay = new ReplayPartida(1, 42L, List.of("mirar"), "huella-v1");
        Path destino = temporal.resolve("replay.json");
        ServicioReplay.guardar(replay, destino);
        ReplayPartida invalido = new ReplayPartida(1, 42L, List.of("  "), "huella-v1");
        assertThrows(IOException.class, () -> ServicioReplay.guardar(invalido, destino));
        assertEquals(replay, ServicioReplay.cargar(destino));
    }

    @Test
    void replayNoAceptaUtf8Malformado() throws Exception {
        Path destino = temporal.resolve("replay-malformado.json");
        Files.write(destino, new byte[] {(byte) 0xc3, (byte) 0x28});
        assertThrows(IOException.class, () -> ServicioReplay.cargar(destino));
    }
}
