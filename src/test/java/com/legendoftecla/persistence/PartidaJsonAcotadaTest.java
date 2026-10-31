package com.legendoftecla.persistence;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.exceptions.JuegoException;
import com.legendoftecla.model.world.Juego;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Los savegames externos se leen de manera acotada y con decodificacion estricta. */
class PartidaJsonAcotadaTest {
    @TempDir Path directorio;

    @Test
    void rechazoRapidoDeUnaPartidaSobredimensionada() throws Exception {
        Path archivo = directorio.resolve("gigante.json");
        try (SeekableByteChannel canal = Files.newByteChannel(archivo,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            canal.position(PersistenciaPartida.MAX_PARTIDA_BYTES);
            canal.write(ByteBuffer.wrap(new byte[] {'x'}));
        }

        JuegoException error = assertThrows(JuegoException.class,
                () -> PersistenciaPartida.cargar(archivo, TestFixtures.consola()));

        assertTrue(error.getMessage().contains("32 MiB"));
    }

    @Test
    void bytesUtf8MalformadosNoSeSustituyenSilenciosamente() throws Exception {
        Path archivo = directorio.resolve("malformada.json");
        Files.write(archivo, new byte[] {(byte) 0xC3, (byte) 0x28});

        assertThrows(JuegoException.class,
                () -> PersistenciaPartida.cargar(archivo, TestFixtures.consola()));
    }

    @Test
    void partidaValidaSigueGuardandoseYCargandoseSinCambios() throws Exception {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Path archivo = directorio.resolve("valida.json");
        juego.getJugador().getProgresion().ganarExperiencia(175);

        PersistenciaPartida.guardar(juego, archivo, 31L);
        Juego restaurado = PersistenciaPartida.cargar(archivo, TestFixtures.consola());

        assertTrue(Files.size(archivo) < PersistenciaPartida.MAX_PARTIDA_BYTES);
        assertEquals(juego.getJugador().getNombre(), restaurado.getJugador().getNombre());
        assertEquals(juego.getJugador().getProgresion().getNivel(),
                restaurado.getJugador().getProgresion().getNivel());
        assertEquals(juego.getJugador().getProgresion().getExperiencia(),
                restaurado.getJugador().getProgresion().getExperiencia());
    }
}
