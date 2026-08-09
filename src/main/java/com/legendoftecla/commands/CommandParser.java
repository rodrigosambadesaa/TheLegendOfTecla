package com.legendoftecla.commands;

import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.constants.FormacionAliada;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;

import java.util.HashMap;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
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
        registrar(comandos, partes -> {
            if (partes.length != 1) {
                throw new ComandoException("Uso: descansar");
            }
            return new ComandoDescansar(context);
        }, "descansar", "reposar");
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
        registrar(comandos, this::parseEquipar, "equipar");
        registrar(comandos, partes -> {
            requiereArg(partes);
            return new ComandoDesequipar(context, unir(partes, 1));
        }, "desequipar");
        registrar(comandos, this::parseAtacar, "atacar");
        registrar(comandos, this::parseLanzarExplosivo, "lanzar");
        registrar(comandos, partes -> {
            requiereArg(partes);
            return new ComandoCargar(context, unir(partes, 1));
        }, "cargar");
        registrar(comandos, this::parsePedirAyuda, "pedir");
        registrar(comandos, partes -> new ComandoPedirAyuda(context), "socorro", "asistir");
        registrar(comandos, this::parseReagrupar, "reagrupar", "formacion");
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

        if (esAlcance(partes[1])) {
            Alcance alcance = parseAlcance(partes[1]);
            String detalle = partes.length > 2 ? unir(partes, 2) : null;
            return new ComandoMirar(context, alcance.direccion(), alcance.pasos(), detalle);
        }

        Direccion direccion = Direccion.desdeTexto(partes[1]);
        if (direccion == null) {
            return new ComandoMirar(context, null, 0, unir(partes, 1));
        }

        int pasos = 1;
        String detalle = null;
        if (partes.length >= 3) {
            pasos = parseEntero(partes[2]);
        }
        if (partes.length > 3) {
            detalle = unir(partes, 3);
        }

        return new ComandoMirar(context, direccion, pasos, detalle);
    }

    private Comando parseMover(String[] partes) throws ComandoException {
        if (partes.length < 2 || partes.length > 3) {
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

    private Comando parseEquipar(String[] partes) throws ComandoException {
        requiereArg(partes);
        for (int separador = 2; separador < partes.length; separador++) {
            String nuevo = unir(partes, 1, separador);
            String anterior = unir(partes, separador, partes.length);
            boolean nuevoEnMochila = context.getJuego().getJugador().getMochila().getObjetos().stream()
                    .anyMatch(objeto -> objeto.getNombre().equalsIgnoreCase(nuevo));
            boolean anteriorEquipado = context.getJuego().getJugador().getArmasEquipadas().stream()
                    .anyMatch(arma -> arma.getNombre().equalsIgnoreCase(anterior))
                    || context.getJuego().getJugador().getArmaduraEquipada() != null
                    && context.getJuego().getJugador().getArmaduraEquipada().getNombre()
                            .equalsIgnoreCase(anterior)
                    || context.getJuego().getJugador().getBinocularEquipado() != null
                    && context.getJuego().getJugador().getBinocularEquipado().getNombre()
                            .equalsIgnoreCase(anterior);
            if (nuevoEnMochila && anteriorEquipado) {
                ComandoCompuesto compuesto = new ComandoCompuesto();
                compuesto.agregar(new ComandoDesequipar(context, anterior));
                compuesto.agregar(new ComandoEquipar(context, nuevo));
                return compuesto;
            }
        }
        return new ComandoEquipar(context, unir(partes, 1));
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

    private Comando parseReagrupar(String[] partes) throws ComandoException {
        if (partes.length != 2) {
            throw new ComandoException("Uso: reagrupar <defensiva|ofensiva>");
        }
        FormacionAliada formacion = switch (partes[1].toLowerCase()) {
            case "defensiva" -> FormacionAliada.DEFENSIVA;
            case "ofensiva" -> FormacionAliada.OFENSIVA;
            default -> throw new ComandoException("Formacion invalida: " + partes[1]);
        };
        return new ComandoReagrupar(context, formacion);
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

    private Alcance parseAlcance(String token) throws ComandoException {
        Matcher matcher = PATRON_ALCANCE.matcher(token);
        if (!matcher.matches()) {
            throw new ComandoException("Alcance invalido: " + token);
        }
        return new Alcance(parseEntero(matcher.group(1)), Direccion.desdeTexto(matcher.group(2)));
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

    private record Alcance(int pasos, Direccion direccion) { }
}
