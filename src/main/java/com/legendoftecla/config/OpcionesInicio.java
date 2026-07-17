package com.legendoftecla.config;

import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.model.world.DimensionesMapa;

import java.nio.file.Path;
import java.util.Locale;

/**
 * Opciones de arranque para iniciar el juego sin completar el asistente interactivo.
  * @param clase valor de {@code clase}
  * @param dificultad valor de {@code dificultad}
  * @param dimensiones valor de {@code dimensiones}
  * @param directorioDatos valor de {@code directorioDatos}
  * @param editor valor de {@code editor}
  * @param gui valor de {@code gui}
  * @param modo valor de {@code modo}
  * @param mostrarAyuda valor de {@code mostrarAyuda}
  * @param nombre valor de {@code nombre}
  * @param rapido valor de {@code rapido}
  * @param conAliados indica si se solicitan aliados; {@code null} conserva la pregunta interactiva
  * @param varianteMapa variante del mapa generado; {@code null} conserva la pregunta interactiva
 */
public record OpcionesInicio(
        String nombre,
        String clase,
        String modo,
        Dificultad dificultad,
        DimensionesMapa dimensiones,
        Path directorioDatos,
        Boolean conAliados,
        Integer varianteMapa,
        boolean rapido,
        boolean mostrarAyuda,
        boolean gui,
        boolean editor) {

    /**
     * Ejecuta la operacion publica {@code desdeArgumentos}.
      * @param args valor de {@code args}
      * @return resultado de la operacion
     */
    public static OpcionesInicio desdeArgumentos(String[] args) {
        String nombre = null;
        String clase = null;
        String modo = null;
        Dificultad dificultad = null;
        DimensionesMapa dimensiones = null;
        Path directorioDatos = null;
        Boolean conAliados = null;
        Integer varianteMapa = null;
        boolean rapido = false;
        boolean mostrarAyuda = false;
        boolean gui = false;
        boolean editor = false;

        for (int i = 0; i < args.length; i++) {
            String argumento = args[i];
            switch (argumento) {
                case "--rapido" -> rapido = true;
                case "--interactivo" -> rapido = false;
                case "--help", "-h" -> mostrarAyuda = true;
                case "--gui" -> gui = true;
                case "--editor" -> {
                    gui = true;
                    editor = true;
                }
                case "--nombre" -> nombre = siguienteValor(args, ++i, argumento);
                case "--clase" -> clase = normalizarClase(siguienteValor(args, ++i, argumento));
                case "--modo" -> modo = normalizarModo(siguienteValor(args, ++i, argumento));
                case "--dificultad" -> dificultad = parsearDificultad(siguienteValor(args, ++i, argumento));
                case "--dimensiones" -> dimensiones = parsearDimensiones(siguienteValor(args, ++i, argumento));
                case "--datos" -> directorioDatos = Path.of(siguienteValor(args, ++i, argumento));
                case "--aliados" -> conAliados = parsearSiNo(siguienteValor(args, ++i, argumento));
                case "--variante" -> varianteMapa = parsearVariante(siguienteValor(args, ++i, argumento));
                default -> throw new IllegalArgumentException("Opcion desconocida: " + argumento);
            }
        }

        if (rapido) {
            nombre = nombre == null ? "Tecla" : nombre;
            clase = clase == null ? "marine" : clase;
            modo = modo == null ? "default" : modo;
            dificultad = dificultad == null ? Dificultad.NORMAL : dificultad;
            conAliados = conAliados == null ? Boolean.FALSE : conAliados;
            varianteMapa = varianteMapa == null ? 1 : varianteMapa;
            if ("ficheros".equals(modo) && directorioDatos == null) {
                directorioDatos = Path.of("data", "escenario_basico");
            }
        }

        return new OpcionesInicio(nombre, clase, modo, dificultad, dimensiones,
                directorioDatos, conAliados, varianteMapa, rapido, mostrarAyuda, gui, editor);
    }

    /**
     * Ejecuta la operacion publica {@code ayuda}.
      * @return resultado de la operacion
     */
    public static String ayuda() {
        return """
                Uso: java -jar the-legend-of-tecla.jar [opciones]

                  --rapido                 Inicia con valores predeterminados, sin asistente inicial
                  --interactivo            Fuerza el asistente inicial (util en Docker)
                  --gui                     Abre la interfaz grafica completa
                  --editor                  Abre directamente el editor grafico de mapas
                  --nombre <nombre>         Nombre del personaje
                  --clase <clase>           marine, francotirador o zapador
                  --modo <modo>             default, grande o ficheros
                  --dificultad <nivel>      muy_facil, facil, normal, dificil,
                                            muy_dificil, pesadilla o demente
                  --dimensiones <FxC>       Tamano del mapa; por ejemplo, 12x20
                  --datos <directorio>      Directorio con escenario.json o los tres ficheros TXT
                  --aliados <si|no>         Activa o desactiva aliados calculados automaticamente
                  --variante <1-50>         Variante determinista del mapa grande
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

    private static Boolean parsearSiNo(String valor) {
        return switch (valor.trim().toLowerCase(Locale.ROOT)) {
            case "si", "sí", "s", "true", "1" -> Boolean.TRUE;
            case "no", "n", "false", "0" -> Boolean.FALSE;
            default -> throw new IllegalArgumentException("Valor de aliados invalido: usa si o no.");
        };
    }

    private static int parsearVariante(String valor) {
        try {
            int variante = Integer.parseInt(valor);
            if (variante < 1 || variante > 50) {
                throw new IllegalArgumentException("La variante debe estar entre 1 y 50.");
            }
            return variante;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Variante invalida: " + valor + ".", e);
        }
    }
}
