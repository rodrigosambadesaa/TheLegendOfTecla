package com.legendoftecla.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.legendoftecla.engine.ServicioExperiencia;
import com.legendoftecla.engine.ServicioNivelInicial;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.progression.ProgresionPersonaje;
import com.legendoftecla.validation.Limites;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Perfil voluntario entre partidas, separado de los savegames completos. */
public final class PersistenciaProgresoLocal {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int VERSION = 1;

    private record Nivel(int nivel, int experiencia) { }
    private record Perfil(int version, String jugador, String clase,
            Nivel progresoJugador, Map<String, Nivel> aliados) { }

    private PersistenciaProgresoLocal() { }

    /** Archivo privado por usuario del sistema operativo. */
    public static Path rutaPredeterminada() {
        return Path.of(System.getProperty("user.home"), ".legendoftecla", "progreso.json");
    }

    /** Aplica el progreso valido si existe; nunca impide empezar sin perfil. */
    public static void aplicarSiExiste(Juego juego) {
        Path archivo = rutaPredeterminada();
        if (!Files.exists(archivo)) return;
        try {
            aplicar(juego, archivo);
            juego.getConsola().imprimirInfo("Progresion local consultada: " + archivo);
        } catch (IOException | RuntimeException error) {
            juego.getConsola().imprimirAdvertencia(
                    "No se pudo cargar el progreso local; se usan los niveles elegidos: "
                            + error.getMessage());
        }
    }

    /** Restablece XP/niveles sin mezclar perfiles de personajes diferentes. */
    public static void aplicar(Juego juego, Path archivo) throws IOException {
        Perfil perfil;
        try {
            perfil = GSON.fromJson(ArchivoJsonAcotado.leer(archivo), Perfil.class);
            if (perfil == null || perfil.version() != VERSION
                    || perfil.progresoJugador() == null || perfil.aliados() == null) {
                throw new IllegalArgumentException("Perfil de progreso incompatible");
            }
            validar(perfil.progresoJugador());
            for (var entrada : perfil.aliados().entrySet()) {
                if (entrada.getKey() == null || entrada.getKey().isBlank()) {
                    throw new IllegalArgumentException("Aliado sin identificador");
                }
                validar(entrada.getValue());
            }
        } catch (RuntimeException error) {
            throw new IOException("Perfil local corrupto o incompatible", error);
        }
        if (!juego.getJugador().getNombre().equals(perfil.jugador())
                || !juego.getJugador().getClass().getSimpleName().equals(perfil.clase())) return;

        Nivel guardado = perfil.progresoJugador();
        int nivel = Math.max(juego.getJugador().getProgresion().getNivel(), guardado.nivel());
        ServicioNivelInicial.aplicar(juego.getJugador(), nivel);
        if (nivel == guardado.nivel()) {
            juego.getJugador().getProgresion().restaurar(nivel, guardado.experiencia(), Set.of());
        }
        for (Aliado aliado : juego.getAliadosRegistrados()) {
            Nivel progreso = perfil.aliados().get(aliado.getNombre());
            if (progreso == null) continue;
            int elegido = Math.max(aliado.getNivel(), progreso.nivel());
            ServicioExperiencia.ajustarNivelInicial(aliado, elegido);
            if (elegido == progreso.nivel()) {
                aliado.getProgresion().restaurar(elegido, progreso.experiencia(), Set.of());
            }
        }
    }

    private static void validar(Nivel progreso) {
        if (progreso == null || progreso.nivel() < 1
                || progreso.nivel() > Limites.NIVEL_ALIADO_MAXIMO
                || progreso.experiencia() < 0
                || progreso.experiencia() >= (long) progreso.nivel() * 100) {
            throw new IllegalArgumentException("XP o nivel fuera de rango");
        }
    }

    /** Guarda el progreso despues de cada baja y cada victoria, solo con consentimiento. */
    public static void guardarSiActivado(Juego juego) {
        if (!juego.isUsarPerfilLocal()) return;
        try {
            guardar(juego, rutaPredeterminada());
        } catch (IOException | RuntimeException error) {
            juego.getConsola().imprimirAdvertencia(
                    "No se pudo conservar la progresion local: " + error.getMessage());
        }
    }

    /** Guarda solo niveles y XP, no datos de mapa ni inventario. */
    public static void guardar(Juego juego, Path archivo) throws IOException {
        Map<String, Nivel> aliados = new LinkedHashMap<>();
        for (Aliado aliado : juego.getAliadosRegistrados()) {
            aliados.put(aliado.getNombre(), snapshot(aliado.getProgresion()));
        }
        Perfil perfil = new Perfil(VERSION, juego.getJugador().getNombre(),
                juego.getJugador().getClass().getSimpleName(),
                snapshot(juego.getJugador().getProgresion()), aliados);
        ArchivoJsonAcotado.escribir(archivo, GSON.toJson(perfil));
    }

    private static Nivel snapshot(ProgresionPersonaje progreso) {
        return new Nivel(progreso.getNivel(), progreso.getExperiencia());
    }
}
