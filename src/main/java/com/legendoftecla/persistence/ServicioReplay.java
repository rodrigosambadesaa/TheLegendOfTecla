package com.legendoftecla.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.legendoftecla.engine.MotorPartida;
import com.legendoftecla.model.world.Juego;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.function.Supplier;

/** Guardado, reproduccion y validacion de replays. */
public final class ServicioReplay {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private ServicioReplay() { }
    public static void guardar(ReplayPartida replay, Path archivo) throws IOException {
        Files.writeString(archivo, GSON.toJson(replay), StandardCharsets.UTF_8);
    }
    public static ReplayPartida cargar(Path archivo) throws IOException {
        ReplayPartida replay = GSON.fromJson(
                Files.readString(archivo, StandardCharsets.UTF_8), ReplayPartida.class);
        if (replay == null || replay.version() != 1 || replay.comandos() == null
                || replay.hashFinal() == null) {
            throw new IOException("Replay corrupto o de version no compatible");
        }
        return replay;
    }
    public static boolean reproducir(ReplayPartida replay, Supplier<MotorPartida> fabrica) {
        if (replay == null || replay.version() != 1) return false;
        MotorPartida motor = fabrica.get();
        replay.comandos().forEach(motor::ejecutarComando);
        return replay.hashFinal().equals(hash(motor.getJuego()));
    }
    public static String hash(Juego juego) {
        String estado = juego.getMapa().renderAscii(juego.getJugador().getPosicion())
                + juego.getJugador().getSalud() + ":" + juego.getJugador().getEnergia()
                + ":" + juego.getPasos();
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(estado.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException imposible) {
            throw new IllegalStateException(imposible);
        }
    }
}
