package com.legendoftecla.config;

import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.model.world.DimensionesMapa;

import java.nio.file.Path;
import java.util.Locale;

/**
 * Opciones de arranque para iniciar el juego sin completar el asistente interactivo.
 */
public record OpcionesInicio(
        String nombre,
        String clase,
        String modo,
        Dificultad dificultad,
        DimensionesMapa dimensiones,
        Path directorioDatos,
        boolean rapido,
        boolean mostrarAyuda) {

    public static OpcionesInicio desdeArgumentos(String[] args) {
        String nombre = null;
        String clase = null;
        String modo = null;
        Dificultad dificultad = null;
        DimensionesMapa dimensiones = null;
        Path directorioDatos = null;
        boolean rapido = false;
        boolean mostrarAyuda = false;

        for (int i = 0; i < args.length; i++) {
            String argumento = args[i];
            switch (argumento) {
                case "--rapido" -> rapido = true;
                case "--interactivo" -> rapido = false;
                case "--help", "-h" -> mostrarAyuda = true;
                case "--nombre" -> nombre = siguienteValor(args, ++i, argumento);
                case "--clase" -> clase = normalizarClase(siguienteValor(args, ++i, argumento));
                case "--modo" -> modo = normalizarModo(siguienteValor(args, ++i, argumento));
                case "--dificultad" -> dificultad = parsearDificultad(siguienteValor(args, ++i, argumento));
                case "--dimensiones" -> dimensiones = parsearDimensiones(siguienteValor(args, ++i, argumento));
                case "--datos" -> directorioDatos = Path.of(siguienteValor(args, ++i, argumento));
                default -> throw new IllegalArgumentException("Opcion desconocida: " + argumento);
            }
        }

        if (rapido) {
            nombre = nombre == null ? "Tecla" : nombre;
            clase = clase == null ? "marine" : clase;
            modo = modo == null ? "default" : modo;
            dificultad = dificultad == null ? Dificultad.NORMAL : dificultad;
            if ("ficheros".equals(modo) && directorioDatos == null) {
                directorioDatos = Path.of("data", "escenario_basico");
            }
        }

        return new OpcionesInicio(nombre, clase, modo, dificultad, dimensiones,
                directorioDatos, rapido, mostrarAyuda);
    }

    public static String ayuda() {
        return """
                Uso: java -jar the-legend-of-tecla.jar [opciones]

                  --rapido                 Inicia con valores predeterminados, sin asistente inicial
                  --interactivo            Fuerza el asistente inicial (util en Docker)
                  --nombre <nombre>         Nombre del personaje
                  --clase <clase>           marine, francotirador o zapador
                  --modo <modo>             default, grande o ficheros
                  --dificultad <nivel>      muy_facil, facil, normal, dificil,
                                            muy_dificil, pesadilla o demente
                  --dimensiones <FxC>       Tamano del mapa; por ejemplo, 12x20
                  --datos <directorio>      Directorio con mapa.txt, objetos.txt y enemigos.txt
                  --help, -h                Muestra esta ayuda

                Las opciones indicadas reemplazan sus preguntas del asistente. Combina
                --rapido con otras opciones para cambiar solamente los valores deseados.
                """;
    }

    private static String siguienteValor(String[] args, int indice, String opcion) {
        if (indice >= args.length || args[indice].startsWith("--")) {
            throw new IllegalArgumentException("Falta el valor de " + opcion + ".");
        }
        return args[indice].trim();
    }

    private static String normalizarClase(String valor) {
        String normalizado = valor.toLowerCase(Locale.ROOT);
        return switch (normalizado) {
            case "marine", "francotirador", "zapador" -> normalizado;
            default -> throw new IllegalArgumentException("Clase invalida: " + valor + ".");
        };
    }

    private static String normalizarModo(String valor) {
        String normalizado = valor.toLowerCase(Locale.ROOT);
        return switch (normalizado) {
            case "1", "default" -> "default";
            case "2", "grande" -> "grande";
            case "3", "ficheros" -> "ficheros";
            default -> throw new IllegalArgumentException("Modo invalido: " + valor + ".");
        };
    }

    private static Dificultad parsearDificultad(String valor) {
        Dificultad dificultad = Dificultad.desdeTexto(valor);
        if (dificultad == null) {
            throw new IllegalArgumentException("Dificultad invalida: " + valor + ".");
        }
        return dificultad;
    }

    private static DimensionesMapa parsearDimensiones(String valor) {
        String[] partes = valor.toLowerCase(Locale.ROOT).split("x");
        if (partes.length != 2) {
            throw new IllegalArgumentException("Dimensiones invalidas: usa el formato filasxcolumnas.");
        }
        try {
            return new DimensionesMapa(
                    Integer.parseInt(partes[0].trim()),
                    Integer.parseInt(partes[1].trim()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Dimensiones invalidas: " + valor + ".", e);
        }
    }
}
