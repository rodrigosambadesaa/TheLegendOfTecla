package com.legendoftecla.persistence;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.world.Juego;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EscrituraAtomicaTest {
    @TempDir
    Path temporal;

    @Test
    void sobreescribeSinDejarTemporalesYAdmiteDirectoriosAnidados() throws Exception {
        Path carpeta = temporal.resolve("partidas").resolve("campaña");
        Path archivo = carpeta.resolve("progreso.json");
        EscrituraAtomica.escribir(archivo, "{\"version\":1}");
        EscrituraAtomica.escribir(archivo, "{\"version\":2, \"nombre\":\"Tecla\"}");

        assertEquals("{\"version\":2, \"nombre\":\"Tecla\"}",
                Files.readString(archivo, StandardCharsets.UTF_8));
        try (var contenido = Files.list(carpeta)) {
            assertEquals(List.of(archivo), contenido.toList());
        }
    }

    @Test
    void noDejaTemporalesNiDestruyeElDestinoAnteUnErrorDeRenombrado()
            throws Exception {
        Path destino = temporal.resolve("destino.json");
        Files.createDirectories(destino);
        Path valioso = destino.resolve("partida-anterior.txt");
        Files.writeString(valioso, "datos existentes", StandardCharsets.UTF_8);

        assertThrows(IOException.class,
                () -> EscrituraAtomica.escribir(destino, "{\"partida\":2}"));

        assertEquals("datos existentes", Files.readString(valioso));
        try (var contenido = Files.list(temporal)) {
            assertEquals(List.of(destino), contenido.toList());
        }
    }

    @Test
    void preservaArchivosSiSeRechazaUnArgumentoAntesDeEscribir()
            throws Exception {
        Path destino = temporal.resolve("save.json");
        Files.writeString(destino, "valor anterior");

        assertThrows(NullPointerException.class,
                () -> EscrituraAtomica.escribir(destino, null));

        assertEquals("valor anterior", Files.readString(destino));
        assertFalse(Files.exists(temporal.resolve(".tecla-save-.tmp")));
    }

    @Test
    void persisteUnSavegameDelJuegoSinArchivosAuxiliares() throws Exception {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Path carpeta = temporal.resolve("mis-partidas");
        Path archivo = carpeta.resolve("partida.json");

        PersistenciaPartida.guardar(juego, archivo, 123);
        juego.setPasos(4);
        PersistenciaPartida.guardar(juego, archivo, 123);

        Juego restaurado = PersistenciaPartida.cargar(archivo, TestFixtures.consola());
        assertEquals(4, restaurado.getPasos());
        assertTrue(Files.size(archivo) > 0);
        try (var contenido = Files.list(carpeta)) {
            assertEquals(List.of(archivo), contenido.toList());
        }
    }

    @Test
    void replaySePuedeGuardarYVolverACargarConElMismoMecanismo()
            throws Exception {
        Path archivo = temporal.resolve("replays").resolve("tecla.json");
        ReplayPartida replay = new ReplayPartida(1, 4L,
                List.of("mover este", "descansar"), "hash-original");

        ServicioReplay.guardar(replay, archivo);
        ReplayPartida recargado = ServicioReplay.cargar(archivo);

        assertEquals(replay.comandos(), recargado.comandos());
        assertEquals(replay.hashFinal(), recargado.hashFinal());
        assertEquals(1, recargado.version());
    }
}
