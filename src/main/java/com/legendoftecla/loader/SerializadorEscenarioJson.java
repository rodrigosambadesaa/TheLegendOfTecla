package com.legendoftecla.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.legendoftecla.exceptions.JuegoException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Lee, valida y escribe escenarios editables en JSON. */
public final class SerializadorEscenarioJson {
    /**
     * Valor publico {@code NOMBRE_ARCHIVO} utilizado por el modelo del juego.
     */
    public static final String NOMBRE_ARCHIVO = "escenario.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private SerializadorEscenarioJson() {
    }

    /**
     * Ejecuta la operacion publica {@code cargar}.
      * @param directorio valor de {@code directorio}
      * @return resultado de la operacion
      * @throws com.legendoftecla.exceptions.JuegoException si la operacion no puede completarse
     */
    public static EscenarioDefinicion cargar(Path directorio) throws JuegoException {
        Path archivo = resolverArchivo(directorio);
        try (Reader reader = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            EscenarioDefinicion escenario = GSON.fromJson(reader, EscenarioDefinicion.class);
            if (escenario == null) {
                throw new JuegoException("El escenario JSON esta vacio.");
            }
            escenario.normalizar();
            validar(escenario);
            return escenario;
        } catch (IOException | JsonParseException | IllegalArgumentException e) {
            throw new JuegoException("No se pudo leer " + archivo + ": " + e.getMessage());
        }
    }

    /**
     * Ejecuta la operacion publica {@code guardar}.
      * @param directorio valor de {@code directorio}
      * @param escenario valor de {@code escenario}
      * @return resultado de la operacion
      * @throws com.legendoftecla.exceptions.JuegoException si la operacion no puede completarse
      * @throws java.io.IOException si la operacion no puede completarse
     */
    public static Path guardar(EscenarioDefinicion escenario, Path directorio) throws IOException, JuegoException {
        escenario.normalizar();
        validar(escenario);
        Path archivo = resolverArchivo(directorio);
        Path padre = archivo.getParent();
        if (padre != null) {
            Files.createDirectories(padre);
        }
        try (Writer writer = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
            GSON.toJson(escenario, writer);
        }
        return archivo;
    }

    /**
     * Ejecuta la operacion publica {@code validar}.
      * @param escenario valor de {@code escenario}
      * @throws com.legendoftecla.exceptions.JuegoException si la operacion no puede completarse
     */
    public static void validar(EscenarioDefinicion escenario) throws JuegoException {
        if (escenario.getFilas() < 3 || escenario.getColumnas() < 3) {
            throw new JuegoException("El escenario debe medir al menos 3x3.");
        }
        if (escenario.getPasosMaximos() <= 0) {
            throw new JuegoException("El numero maximo de pasos debe ser mayor que cero.");
        }
        validarPunto(escenario, escenario.getInicio(), "inicio");
        validarPunto(escenario, escenario.getObjetivo(), "objetivo");
        if (escenario.getInicio().getFila() == escenario.getObjetivo().getFila()
                && escenario.getInicio().getColumna() == escenario.getObjetivo().getColumna()) {
            throw new JuegoException("Inicio y objetivo deben estar en celdas diferentes.");
        }
        for (EscenarioDefinicion.CeldaDef celda : escenario.getCeldas()) {
            validarPunto(escenario, celda, "celda");
        }
        for (EscenarioDefinicion.PersonajeDef enemigo : escenario.getEnemigos()) {
            validarPersonaje(escenario, enemigo, "enemigo");
        }
        for (EscenarioDefinicion.ObjetoDef objeto : escenario.getObjetos()) {
            validarPunto(escenario, objeto, "objeto");
            if (objeto.getNombre().isBlank() || objeto.getPeso() < 0) {
                throw new JuegoException("Todos los objetos necesitan nombre y peso no negativo.");
            }
        }
        EscenarioDefinicion.CeldaDef inicio = escenario.celda(
                escenario.getInicio().getFila(), escenario.getInicio().getColumna());
        EscenarioDefinicion.CeldaDef objetivo = escenario.celda(
                escenario.getObjetivo().getFila(), escenario.getObjetivo().getColumna());
        if ((inicio != null && !inicio.isTransitable())
                || (objetivo != null && !objetivo.isTransitable())) {
            throw new JuegoException("Las celdas de inicio y objetivo deben ser transitables.");
        }
    }

    private static void validarPersonaje(EscenarioDefinicion escenario,
            EscenarioDefinicion.PersonajeDef personaje, String etiqueta) throws JuegoException {
        validarPunto(escenario, personaje, etiqueta);
        if (personaje.getNombre().isBlank()
                || personaje.getSalud() <= 0 || personaje.getEnergia() <= 0
                || personaje.getVision() <= 0) {
            throw new JuegoException("El " + etiqueta + " tiene atributos invalidos.");
        }
    }

    private static void validarPunto(EscenarioDefinicion escenario,
            EscenarioDefinicion.Punto punto, String etiqueta) throws JuegoException {
        if (punto == null || punto.getFila() < 0 || punto.getFila() >= escenario.getFilas()
                || punto.getColumna() < 0 || punto.getColumna() >= escenario.getColumnas()) {
            throw new JuegoException("Posicion de " + etiqueta + " fuera del mapa.");
        }
    }

    private static Path resolverArchivo(Path ruta) {
        return ruta.getFileName() != null && ruta.getFileName().toString().toLowerCase().endsWith(".json")
                ? ruta
                : ruta.resolve(NOMBRE_ARCHIVO);
    }
}
