package com.legendoftecla.commands;

import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.world.Direccion;

import java.util.regex.Pattern;

/**
 * Representa la entidad CommandParser del juego.
 */
public class CommandParser {
    private static final Pattern PATRON_ALCANCE = Pattern.compile("^(\\d+)([nseoNSEO])$");

    private final CommandContext context;

    /**
     * Ejecuta CommandParser.
      * @param context valor de {@code context}
     */
    public CommandParser(CommandContext context) {
        this.context = context;
    }

    /**
     * Ejecuta parse.
      * @param linea valor de {@code linea}
      * @return resultado de la operacion
      * @throws com.legendoftecla.exceptions.ComandoException si la operacion no puede completarse
     */
    public Comando parse(String linea) throws ComandoException {
        String[] partes = linea.trim().split("\\s+");
        if (partes.length == 0 || partes[0].isBlank()) {
            throw new ComandoException("Comando vacio.");
        }
        String cmd = partes[0].toLowerCase();
        return switch (cmd) {
            case "ayuda", "comandos" -> new ComandoAyuda(context);
            case "mirar" -> parseMirar(partes);
            case "inventario", "mochila" -> new ComandoInventario(context);
            case "recorrido" -> new ComandoRecorrido(context);
            case "mover", "avanzar" -> parseMover(partes);
            case "coger" -> requiereArg(partes, new ComandoCoger(context, unir(partes, 1)));
            case "tirar" -> requiereArg(partes, new ComandoTirar(context, unir(partes, 1)));
            case "usar" -> requiereArg(partes, new ComandoUsar(context, unir(partes, 1)));
            case "equipar" -> requiereArg(partes, new ComandoEquipar(context, unir(partes, 1)));
            case "desequipar" -> requiereArg(partes, new ComandoDesequipar(context, unir(partes, 1)));
            case "atacar" -> parseAtacar(partes);
            case "lanzar" -> parseLanzarExplosivo(partes);
            case "salir" -> new ComandoSalir();
            default -> throw new ComandoException("Comando desconocido: " + cmd);
        };
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

    private int parseEntero(String valor) throws ComandoException {
        try {
            int numero = Integer.parseInt(valor);
            if (numero <= 0) {
                throw new ComandoException("El numero debe ser mayor que 0.");
            }
            return numero;
        } catch (NumberFormatException e) {
            throw new ComandoException("Valor numerico invalido: " + valor);
        }
    }

    private Comando requiereArg(String[] partes, Comando comando) throws ComandoException {
        if (partes.length < 2) {
            throw new ComandoException("Falta argumento para el comando.");
        }
        return comando;
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
}
