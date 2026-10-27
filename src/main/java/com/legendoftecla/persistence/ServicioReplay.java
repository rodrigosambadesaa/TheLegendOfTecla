package com.legendoftecla.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.legendoftecla.engine.MotorPartida;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.validation.Limites;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Guarda, carga y verifica replays sin invalidar los archivos historicos v1.
 *
 * <p>La version 2 comprueba el estado persistible completo, mientras que la
 * version 1 conserva deliberadamente su huella historica limitada.
 */
public final class ServicioReplay {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int MAX_BYTES = 16 * 1024 * 1024;
    private static final int MAX_COMANDOS = 1_000_000;

    private ServicioReplay() {
    }

    /** Guarda un replay valido usando reemplazo atomico. */
    public static void guardar(ReplayPartida replay, Path archivo) throws IOException {
        validar(replay);
        String json = GSON.toJson(replay);
        if (json.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            throw new IOException("El replay supera el limite de 16 MiB.");
        }
        EscrituraAtomica.escribir(archivo, json);
    }

    /** Lee un replay acotado y traduce corrupcion JSON a IOException. */
    public static ReplayPartida cargar(Path archivo) throws IOException {
        if (Files.size(archivo) > MAX_BYTES) {
            throw new IOException("El replay supera el limite de 16 MiB.");
        }
        try {
            ReplayPartida replay = GSON.fromJson(
                    Files.readString(archivo, StandardCharsets.UTF_8), ReplayPartida.class);
            validar(replay);
            return replay;
        } catch (RuntimeException error) {
            throw new IOException("Replay corrupto o de version no compatible", error);
        }
    }

    /**
     * Reproduce comandos mientras el motor siga activo; nunca ignora ordenes
     * posteriores a una finalizacion anticipada.
     */
    public static boolean reproducir(ReplayPartida replay, Supplier<MotorPartida> fabrica) {
        try {
            validar(replay);
        } catch (IOException error) {
            return false;
        }
        MotorPartida motor = Objects.requireNonNull(
                Objects.requireNonNull(fabrica, "Fabrica").get(), "Motor");
        for (int i = 0; i < replay.comandos().size(); i++) {
            boolean continua = motor.ejecutarComando(replay.comandos().get(i));
            if (!continua && i + 1 < replay.comandos().size()) {
                return false;
            }
        }
        String calculada = replay.version() == 1
                ? hash(motor.getJuego()) : hashCompleto(motor.getJuego());
        return replay.hashFinal().equalsIgnoreCase(calculada);
    }

    /**
     * SHA-256 historico de replay v1 (no cubre inventario ni NPC).
     *
     * @return huella antigua, para verificar replays ya existentes
     */
    public static String hash(Juego juego) {
        String estado = juego.getMapa().renderAscii(juego.getJugador().getPosicion())
                + juego.getJugador().getSalud() + ":" + juego.getJugador().getEnergia()
                + ":" + juego.getPasos();
        return sha256(estado);
    }

    /**
     * SHA-256 del estado persistible completo para replays de version 2.
     *
     * @return huella canonica de mapa, equipo, personajes y progresion
     */
    public static String hashCompleto(Juego juego) {
        return HuellaEstadoReplay.calcular(juego);
    }

    private static String sha256(String estado) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(estado.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException imposible) {
            throw new IllegalStateException(imposible);
        }
    }

    private static void validar(ReplayPartida replay) throws IOException {
        if (replay == null || (replay.version() != 1 && replay.version() != 2)
                || replay.comandos() == null || replay.hashFinal() == null) {
            throw new IOException("Replay corrupto o de version no compatible");
        }
        if (replay.comandos().size() > MAX_COMANDOS
                || replay.comandos().stream().anyMatch(comando ->
                        comando == null || comando.isBlank()
                        || comando.length() > Limites.DESCRIPCION)) {
            throw new IOException("Replay con comandos no validos.");
        }
        if (replay.version() == 2
                && !replay.hashFinal().matches("(?i)[0-9a-f]{64}")) {
            throw new IOException("La huella SHA-256 del replay v2 no es valida.");
        }
    }
}
