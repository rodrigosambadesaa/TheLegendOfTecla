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
            if (celda.getElementoTipo() != null && !celda.getElementoTipo().isBlank()
                    && (celda.getElementoId() == null || celda.getElementoId().isBlank())) {
                throw new JuegoException("Cada elemento de mapa necesita un ID.");
            }
        }
        for (EscenarioDefinicion.PersonajeDef enemigo : escenario.getEnemigos()) {
            validarPersonaje(escenario, enemigo, "enemigo");
        }
        for (EscenarioDefinicion.ObjetoDef objeto : escenario.getObjetos()) {
            validarPunto(escenario, objeto, "objeto");
            if (objeto.getNombre().isBlank() || objeto.getPeso() < 0) {
                throw new JuegoException("Todos los objetos necesitan nombre y peso no negativo.");
            }
            validarMunicion(objeto);
        }
        EscenarioDefinicion.CeldaDef inicio = escenario.celda(
                escenario.getInicio().getFila(), escenario.getInicio().getColumna());
        EscenarioDefinicion.CeldaDef objetivo = escenario.celda(
                escenario.getObjetivo().getFila(), escenario.getObjetivo().getColumna());
        if ((inicio != null && !inicio.isTransitable())
                || (objetivo != null && !objetivo.isTransitable())) {
            throw new JuegoException("Las celdas de inicio y objetivo deben ser transitables.");
        }
        java.util.List<String> ids = escenario.getCeldas().stream()
                .map(EscenarioDefinicion.CeldaDef::getElementoId)
                .filter(java.util.Objects::nonNull).filter(id -> !id.isBlank()).toList();
        if (ids.stream().distinct().count() != ids.size()) {
            throw new JuegoException("Los IDs de elementos no pueden estar duplicados.");
        }
        java.util.Set<String> idsConocidos = new java.util.HashSet<>(ids);
        java.util.Set<String> tipos = java.util.Set.of("puerta", "terminal", "interruptor",
                "cofre", "barricada", "cobertura", "mina", "trampa",
                "trampa_fuego", "trampa_veneno", "trampa_electrica", "alarma");
        for (EscenarioDefinicion.CeldaDef celda : escenario.getCeldas()) {
            String tipo = celda.getElementoTipo();
            if (tipo == null || tipo.isBlank()) continue;
            String normalizado = tipo.toLowerCase(java.util.Locale.ROOT);
            if (!tipos.contains(normalizado)) {
                throw new JuegoException("Tipo de elemento desconocido: " + tipo);
            }
            if ((normalizado.equals("terminal") || normalizado.equals("interruptor"))
                    && celda.getReferencia() != null && !celda.getReferencia().isBlank()
                    && !idsConocidos.contains(celda.getReferencia())) {
                throw new JuegoException("Referencia de elemento inexistente: "
                        + celda.getReferencia());
            }
            if (normalizado.equals("puerta") && celda.getElementoEstado() != null) {
                try {
                    com.legendoftecla.model.elements.EstadoPuerta.valueOf(
                            celda.getElementoEstado().toUpperCase(java.util.Locale.ROOT));
                } catch (IllegalArgumentException error) {
                    throw new JuegoException("Estado de puerta invalido: "
                            + celda.getElementoEstado());
                }
            }
        }
        validarConectividad(escenario);
    }

    private static void validarMunicion(EscenarioDefinicion.ObjetoDef objeto)
            throws JuegoException {
        boolean esArma = "arma".equalsIgnoreCase(objeto.getTipo());
        boolean esMunicion = "municion".equalsIgnoreCase(objeto.getTipo());
        if (!esArma && !esMunicion) {
            return;
        }
        if (objeto.getTipoMunicion() == null || objeto.getTipoMunicion().isBlank()) {
            if (esMunicion) {
                throw new JuegoException("La municion necesita tipoMunicion.");
            }
            return;
        }
        com.legendoftecla.model.items.TipoMunicion tipo;
        try {
            tipo = com.legendoftecla.model.items.TipoMunicion.valueOf(
                    objeto.getTipoMunicion().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException error) {
            throw new JuegoException("Tipo de municion invalido: "
                    + objeto.getTipoMunicion());
        }
        if (esMunicion && (tipo == com.legendoftecla.model.items.TipoMunicion.INFINITA
                || objeto.getCantidad() <= 0)) {
            throw new JuegoException("El paquete de municion debe ser finito y no vacio.");
        }
        if (esArma && tipo != com.legendoftecla.model.items.TipoMunicion.INFINITA
                && (objeto.getCapacidadCargador() <= 0
                        || objeto.getMunicionActual() > objeto.getCapacidadCargador())) {
            throw new JuegoException("El cargador del arma es invalido.");
        }
    }

    private static void validarConectividad(EscenarioDefinicion escenario) throws JuegoException {
        java.util.Set<String> bloqueadas = escenario.getCeldas().stream()
                .filter(c -> !c.isTransitable()).map(c -> c.getFila() + ":" + c.getColumna())
                .collect(java.util.stream.Collectors.toSet());
        java.util.ArrayDeque<EscenarioDefinicion.Punto> pendientes = new java.util.ArrayDeque<>();
        java.util.Set<String> visitadas = new java.util.HashSet<>();
        pendientes.add(escenario.getInicio());
        while (!pendientes.isEmpty()) {
            EscenarioDefinicion.Punto actual = pendientes.remove();
            String clave = actual.getFila() + ":" + actual.getColumna();
            if (!visitadas.add(clave) || bloqueadas.contains(clave)) continue;
            for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                int f = actual.getFila() + d[0]; int c = actual.getColumna() + d[1];
                if (f >= 0 && f < escenario.getFilas() && c >= 0 && c < escenario.getColumnas()) {
                    pendientes.add(new EscenarioDefinicion.Punto(f, c));
                }
            }
        }
        String objetivo = escenario.getObjetivo().getFila() + ":" + escenario.getObjetivo().getColumna();
        if (!visitadas.contains(objetivo)) throw new JuegoException("El objetivo no es alcanzable.");
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
