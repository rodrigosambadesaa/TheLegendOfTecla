package com.legendoftecla;

import com.legendoftecla.config.OpcionesInicio;
import com.legendoftecla.console.Consola;
import com.legendoftecla.console.ConsolaNormal;
import com.legendoftecla.console.TipoMensaje;
import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.engine.ConfiguracionPartida;
import com.legendoftecla.engine.FabricaJuego;
import com.legendoftecla.engine.MotorPartida;
import com.legendoftecla.exceptions.FinEntradaException;
import com.legendoftecla.exceptions.JuegoException;
import com.legendoftecla.gui.VentanaPrincipal;
import com.legendoftecla.model.world.DimensionesMapa;
import com.legendoftecla.model.world.Juego;

import java.nio.file.Path;
import java.awt.GraphicsEnvironment;
import javax.swing.SwingUtilities;

/** Punto de entrada de la version de consola y de la interfaz grafica. */
public final class Main {
    private Main() {
    }

    /**
     * Crea una instancia de {@code Main}.
      * @param args valor de {@code args}
     */
    public static void main(String[] args) {
        OpcionesInicio opciones;
        try {
            opciones = OpcionesInicio.desdeArgumentos(args);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println();
            System.err.println(OpcionesInicio.ayuda());
            return;
        }

        if (opciones.mostrarAyuda()) {
            System.out.println(OpcionesInicio.ayuda());
            return;
        }

        if (opciones.gui()) {
            if (GraphicsEnvironment.isHeadless()) {
                System.err.println("La interfaz grafica necesita un entorno de escritorio.");
                return;
            }
            SwingUtilities.invokeLater(() -> VentanaPrincipal.iniciar(opciones.editor()));
            return;
        }

        try {
            ejecutarConsola(opciones);
        } catch (FinEntradaException e) {
            System.out.println("Entrada cerrada. Partida finalizada.");
        }
    }

    private static void ejecutarConsola(OpcionesInicio opciones) {
        Consola consola = new ConsolaNormal();
        consola.imprimir("Bienvenido a The Legend of Tecla", TipoMensaje.INFO);

        String nombre = opciones.nombre() != null
                ? opciones.nombre()
                : leerNombre(consola);
        String clase = opciones.clase() != null
                ? opciones.clase()
                : leerClase(consola);
        String modo = opciones.modo() != null
                ? opciones.modo()
                : leerModo(consola);
        Dificultad dificultad = opciones.dificultad() != null
                ? opciones.dificultad()
                : leerDificultad(consola);
        DimensionesMapa dimensiones = opciones.dimensiones() != null || opciones.rapido()
                ? opciones.dimensiones()
                : leerDimensiones(consola);
        Path directorio = opciones.directorioDatos();
        if ("ficheros".equals(modo) && directorio == null) {
            directorio = Path.of(consola.leer(
                    "Ruta del directorio con escenario.json o mapa.txt, objetos.txt y enemigos.txt:"));
        }
        boolean conAliados = opciones.conAliados() != null
                ? opciones.conAliados()
                : leerAliados(consola);
        int varianteMapa = opciones.varianteMapa() != null
                ? opciones.varianteMapa()
                : ("grande".equals(modo) ? leerVariante(consola) : 1);

        try {
            ConfiguracionPartida configuracion = new ConfiguracionPartida(
                    nombre, clase, modo, dificultad, dimensiones, directorio, conAliados, varianteMapa);
            Juego juego = FabricaJuego.crear(consola, configuracion);
            MotorPartida motor = new MotorPartida(juego);

            while (!motor.isFinalizada()) {
                consola.imprimir(juego.getMapa().renderAscii(
                        juego.getJugador().getPosicion(),
                        motor.getEnemigosVisibles(),
                        motor.getAliadosVisibles()));
                consola.imprimir(motor.getEstadoJugador(), TipoMensaje.ESTADO);
                motor.ejecutarComando(consola.leer("accion>"));
            }
        } catch (JuegoException | IllegalArgumentException e) {
            consola.imprimir("No se pudo iniciar el juego: " + e.getMessage(), TipoMensaje.ERROR);
        }
    }

    private static String leerNombre(Consola consola) {
        while (true) {
            String nombre = consola.leer("Introduce nombre del personaje:").trim();
            if (!nombre.isBlank()) {
                return nombre;
            }
            consola.imprimir("El nombre no puede estar vacio.", TipoMensaje.ERROR);
        }
    }

    private static String leerClase(Consola consola) {
        while (true) {
            String clase = consola.leer("Elige clase (marine/francotirador/zapador):").trim().toLowerCase();
            if (clase.equals("marine") || clase.equals("francotirador") || clase.equals("zapador")) {
                return clase;
            }
            consola.imprimir("Clase invalida.", TipoMensaje.ERROR);
        }
    }

    private static String leerModo(Consola consola) {
        while (true) {
            String modo = consola.leer(
                    "Modo (1=predeterminado, 2=grande con 50 variantes, 3=ficheros/JSON):")
                    .trim().toLowerCase();
            switch (modo) {
                case "1", "default" -> {
                    return "default";
                }
                case "2", "grande" -> {
                    return "grande";
                }
                case "3", "ficheros" -> {
                    return "ficheros";
                }
                default -> consola.imprimir("Modo invalido.", TipoMensaje.ERROR);
            }
        }
    }

    private static Dificultad leerDificultad(Consola consola) {
        while (true) {
            String entrada = consola.leer(
                    "Dificultad (muy facil, facil, normal, dificil, muy dificil, pesadilla, demente) [normal]:");
            if (entrada == null || entrada.isBlank()) {
                return Dificultad.NORMAL;
            }
            Dificultad dificultad = Dificultad.desdeTexto(entrada);
            if (dificultad != null) {
                return dificultad;
            }
            consola.imprimir("Dificultad invalida.", TipoMensaje.ERROR);
        }
    }

    private static DimensionesMapa leerDimensiones(Consola consola) {
        while (true) {
            String entrada = consola.leer("Tamano global del mapa <filas>x<columnas> (ENTER = por defecto):");
            if (entrada == null || entrada.isBlank()) {
                return null;
            }
            String[] partes = entrada.trim().toLowerCase().split("x");
            if (partes.length != 2) {
                consola.imprimir("Formato invalido. Usa por ejemplo 12x20.", TipoMensaje.ERROR);
                continue;
            }
            try {
                return new DimensionesMapa(
                        Integer.parseInt(partes[0].trim()),
                        Integer.parseInt(partes[1].trim()));
            } catch (RuntimeException e) {
                consola.imprimir("Tamano invalido: " + e.getMessage(), TipoMensaje.ERROR);
            }
        }
    }

    private static boolean leerAliados(Consola consola) {
        while (true) {
            String entrada = consola.leer("¿Incluir aliados calculados automaticamente? (si/no) [no]:");
            if (entrada == null || entrada.isBlank() || "no".equalsIgnoreCase(entrada.trim())) {
                return false;
            }
            if ("si".equalsIgnoreCase(entrada.trim()) || "sí".equalsIgnoreCase(entrada.trim())) {
                return true;
            }
            consola.imprimir("Respuesta invalida. Escribe si o no.", TipoMensaje.ERROR);
        }
    }

    private static int leerVariante(Consola consola) {
        while (true) {
            String entrada = consola.leer("Variante del mapa grande (1-50) [1]:");
            if (entrada == null || entrada.isBlank()) {
                return 1;
            }
            try {
                int variante = Integer.parseInt(entrada.trim());
                if (variante >= 1 && variante <= 50) {
                    return variante;
                }
            } catch (NumberFormatException ignored) {
                // Se informa con el mismo mensaje para cualquier valor no valido.
            }
            consola.imprimir("La variante debe ser un numero entre 1 y 50.", TipoMensaje.ERROR);
        }
    }
}
