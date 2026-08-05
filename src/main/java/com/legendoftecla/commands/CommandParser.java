package com.legendoftecla.commands;

import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;

import java.util.HashMap;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Representa la entidad CommandParser del juego.
 */
public class CommandParser {
    private static final Pattern PATRON_ALCANCE = Pattern.compile("^(\\d+)([nseoNSEO])$");

    private CommandContext context;
    private Map<String, ConstructorComando> rutas;

    /**
     * Ejecuta CommandParser.
      * @param context valor de {@code context}
     */
    public CommandParser(CommandContext context) {
        setContext(context);
        setRutas(crearRutas());
    }

    /** @return contexto de los comandos generados */
    public CommandContext getContext() {
        return context;
    }

    /** @param context contexto no nulo */
    public void setContext(CommandContext context) {
        this.context = Validaciones.noNulo(context, "Contexto");
    }

    /** @return vista de solo lectura de las rutas registradas */
    public Map<String, ?> getRutas() {
        return Collections.unmodifiableMap(rutas);
    }

    /**
     * Sustituye las rutas por una copia validada.
     *
     * @param rutas rutas internas no nulas y acotadas
     */
    public void setRutas(Map<String, ?> rutas) {
        Validaciones.noNulo(rutas, "Rutas de comandos");
        if (rutas.size() > 1_000 || rutas.entrySet().stream()
                .anyMatch(entrada -> entrada.getKey() == null || entrada.getValue() == null)) {
            throw new IllegalArgumentException("Las rutas de comandos no son validas.");
        }
        Map<String, ConstructorComando> copia = new HashMap<>();
        for (Map.Entry<String, ?> entrada : rutas.entrySet()) {
            if (!(entrada.getValue() instanceof ConstructorComando constructor)) {
                throw new IllegalArgumentException("La ruta " + entrada.getKey() + " no crea comandos.");
            }
            copia.put(Validaciones.textoObligatorio(
                    entrada.getKey(), "Nombre de ruta", Limites.TEXTO_CORTO), constructor);
        }
        this.rutas = Map.copyOf(copia);
    }

    /**
     * Ejecuta parse.
      * @param linea valor de {@code linea}
      * @return resultado de la operacion
      * @throws com.legendoftecla.exceptions.ComandoException si la operacion no puede completarse
     */
    public Comando parse(String linea) throws ComandoException {
        if (linea == null) {
            throw new ComandoException("Comando vacio.");
        }
        if (linea.length() > Limites.DESCRIPCION) {
            throw new ComandoException("El comando es demasiado largo.");
        }
        String[] partes = linea.trim().split("\\s+");
        if (partes.length == 0 || partes[0].isBlank()) {
            throw new ComandoException("Comando vacio.");
        }
        String nombre = partes[0].toLowerCase();
        ConstructorComando constructor = rutas.get(nombre);
        if (constructor == null) {
            throw new ComandoException("Comando desconocido: " + nombre);
        }
        return constructor.crear(partes);
    }

    private Map<String, ConstructorComando> crearRutas() {
        Map<String, ConstructorComando> comandos = new HashMap<>();
        registrar(comandos, partes -> new ComandoAyuda(context), "ayuda", "comandos");
        registrar(comandos, this::parseMirar, "mirar");
        registrar(comandos, partes -> new ComandoInventario(context), "inventario", "mochila");
        registrar(comandos, partes -> new ComandoRecorrido(context), "recorrido");
        registrar(comandos, this::parseMover, "mover", "avanzar");
        registrar(comandos, partes -> {
            requiereArg(partes);
            return new ComandoCoger(context, unir(partes, 1));
        }, "coger");
        registrar(comandos, partes -> {
            requiereArg(partes);
            return new ComandoTirar(context, unir(partes, 1));
        }, "tirar");
        registrar(comandos, partes -> {
            requiereArg(partes);
            return new ComandoUsar(context, unir(partes, 1));
        }, "usar");
        registrar(comandos, partes -> {
            requiereArg(partes);
            return new ComandoEquipar(context, unir(partes, 1));
        }, "equipar");
        registrar(comandos, partes -> {
            requiereArg(partes);
            return new ComandoDesequipar(context, unir(partes, 1));
        }, "desequipar");
        registrar(comandos, this::parseAtacar, "atacar");
        registrar(comandos, this::parseLanzarExplosivo, "lanzar");
        registrar(comandos, this::parsePedirAyuda, "pedir");
        registrar(comandos, partes -> new ComandoPedirAyuda(context), "socorro", "asistir");
        registrar(comandos, partes -> new ComandoSalir(), "salir");
        return Map.copyOf(comandos);
    }

    private void registrar(Map<String, ConstructorComando> comandos,
            ConstructorComando constructor, String... nombres) {
        for (String nombre : nombres) {
            comandos.put(nombre, constructor);
        }
    }

    private Comando parseMirar(String[] partes) throws ComandoException {
        if (partes.length == 1) {
            return new ComandoMirar(context);
        }

        Direccion direccion = Direccion.desdeTexto(partes[1]);
        if (direccion == null) {
            throw new ComandoException("Uso: mirar [norte|sur|este|oeste] [pasos]");
        }

        int pasos = 1;
        if (partes.length >= 3) {
            pasos = parseEntero(partes[2]);
        }
        if (partes.length > 3) {
            throw new ComandoException("Uso: mirar [norte|sur|este|oeste] [pasos]");
        }

        return new ComandoMirar(context, direccion, pasos);
    }

    private Comando parseMover(String[] partes) throws ComandoException {
        if (partes.length < 2) {
            throw new ComandoException("Uso: mover <norte|sur|este|oeste> [repeticiones]");
        }
        Direccion direccion = Direccion.desdeTexto(partes[1]);
        if (direccion == null) {
            throw new ComandoException("Direccion invalida: " + partes[1]);
        }
        Comando base = new ComandoMover(context, direccion);
        if (partes.length >= 3) {
            int repeticiones = parseEntero(partes[2]);
            return new ComandoRepetido(base, repeticiones);
        }
        return base;
    }

    private Comando parseAtacar(String[] partes) throws ComandoException {
        int fin = partes.length;
        int repeticiones = 1;
        if (partes.length > 1) {
            String ultimo = partes[partes.length - 1];
            try {
                repeticiones = parseEntero(ultimo);
                fin = partes.length - 1;
            } catch (ComandoException ignored) {
                repeticiones = 1;
                fin = partes.length;
            }
        }

        String alcance = null;
        String objetivo = null;
        if (fin > 1) {
            String primerArg = partes[1];
            if (esAlcance(primerArg)) {
                alcance = primerArg;
                if (fin > 2) {
                    objetivo = unir(partes, 2, fin);
                }
            } else {
                objetivo = unir(partes, 1, fin);
            }
        }

        Comando base = new ComandoAtacar(context, alcance, objetivo);
        if (repeticiones > 1) {
            return new ComandoRepetido(base, repeticiones);
        }
        return base;
    }

    private Comando parseLanzarExplosivo(String[] partes) throws ComandoException {
        if (partes.length < 3 || !esAlcance(partes[1])) {
            throw new ComandoException("Uso: lanzar <distancia><direccion> <explosivo>");
        }
        return new ComandoLanzarExplosivo(context, partes[1], unir(partes, 2));
    }

    private Comando parsePedirAyuda(String[] partes) throws ComandoException {
        if (partes.length != 2 || !"ayuda".equalsIgnoreCase(partes[1])) {
            throw new ComandoException("Uso: pedir ayuda");
        }
        return new ComandoPedirAyuda(context);
    }

    private int parseEntero(String valor) throws ComandoException {
        try {
            int numero = Integer.parseInt(valor);
            if (numero <= 0 || numero > 1_000) {
                throw new ComandoException("El numero debe estar entre 1 y 1000.");
            }
            return numero;
        } catch (NumberFormatException e) {
            throw new ComandoException("Valor numerico invalido: " + valor);
        }
    }

    private void requiereArg(String[] partes) throws ComandoException {
        if (partes.length < 2) {
            throw new ComandoException("Falta argumento para el comando.");
        }
    }

    private boolean esAlcance(String token) {
        return PATRON_ALCANCE.matcher(token).matches();
    }

    private String unir(String[] partes, int inicio) {
        return unir(partes, inicio, partes.length);
    }

    private String unir(String[] partes, int inicio, int finExclusivo) {
        StringBuilder sb = new StringBuilder();
        for (int i = inicio; i < finExclusivo; i++) {
            if (i > inicio) {
                sb.append(' ');
            }
            sb.append(partes[i]);
        }
        return sb.toString();
    }

    @FunctionalInterface
    private interface ConstructorComando {
        Comando crear(String[] partes) throws ComandoException;
    }
}
