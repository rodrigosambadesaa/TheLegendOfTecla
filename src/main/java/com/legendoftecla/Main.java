package com.legendoftecla;

import com.legendoftecla.config.OpcionesInicio;
import com.legendoftecla.console.Consola;
import com.legendoftecla.console.ConsolaNormal;
import com.legendoftecla.console.TipoMensaje;
import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.constants.CondicionVictoria;
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
        int cantidadAliados = opciones.cantidadAliados() != null
                ? opciones.cantidadAliados()
                : leerAliados(consola);
        boolean conAliados = cantidadAliados != 0;
        int nivelAliados = !conAliados ? 0 : opciones.nivelAliados() != null
                ? opciones.nivelAliados() : leerNivelAliados(consola);
        int nivelJugador = opciones.nivelJugador() != null
                ? opciones.nivelJugador() : leerNivelJugador(consola);
        CondicionVictoria condicionVictoria = opciones.condicionVictoria() != null
                ? opciones.condicionVictoria()
                : (conAliados ? leerCondicionVictoria(consola) : CondicionVictoria.JUGADOR_Y_ALIADOS);
        int varianteMapa = opciones.varianteMapa() != null
                ? opciones.varianteMapa()
                : ("grande".equals(modo) ? leerVariante(consola) : 1);

        try {
            ConfiguracionPartida configuracion = new ConfiguracionPartida(
                    nombre, clase, modo, dificultad, dimensiones, directorio, cantidadAliados,
                    condicionVictoria, varianteMapa);
            configuracion.setNivelAliados(nivelAliados);
            configuracion.setNivelJugador(nivelJugador);
            configuracion.setMejorasEquipoAliado(opciones.isMejorasEquipoAliado());
            configuracion.setMunicionAliadaAutomatica(opciones.isMunicionAliadaAutomatica());
            if (opciones.seed() != null) configuracion.setSeed(opciones.seed());
            Juego juego = FabricaJuego.crear(consola, configuracion);
            MotorPartida motor = new MotorPartida(juego);

            while (!motor.isFinalizada()) {
                Juego juegoActual = motor.getJuego();
                consola.imprimir(juegoActual.getMapa().renderAscii(
                        juegoActual.getJugador().getPosicion(),
                        motor.getEnemigosVisibles(),
                        motor.getAliadosVisibles(),
                        juegoActual.getCeldasInspeccionadas(),
                        motor.getCeldasIluminadas()));
                consola.imprimir("Leyenda: J=jugador E=enemigo A=aliado F=fuego ?=oscuridad "
                        + "T=antorcha U=fuente ==madera o=objeto X=objetivo", TipoMensaje.INFO);
                consola.imprimir(motor.getEstadoJugador(), TipoMensaje.ESTADO);
                consola.imprimir(motor.getEstadoAliados(), TipoMensaje.ESTADO);
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
                    "Modo (1=predeterminado, 2=grande con 50 variantes, 3=ficheros/JSON, 4=procedural):")
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
                case "4", "procedural" -> {
                    return "procedural";
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

    private static int leerAliados(Consola consola) {
        while (true) {
            String entrada = consola.leer(
                    "Aliados (no=ninguno, auto=calculados, o escribe una cantidad) [no]:");
            if (entrada == null || entrada.isBlank() || "no".equalsIgnoreCase(entrada.trim())) {
                return 0;
            }
            String normalizada = entrada.trim();
            if ("si".equalsIgnoreCase(normalizada) || "sí".equalsIgnoreCase(normalizada)
                    || "auto".equalsIgnoreCase(normalizada)
                    || "automatico".equalsIgnoreCase(normalizada)
                    || "automático".equalsIgnoreCase(normalizada)) {
                return -1;
            }
            try {
                int cantidad = Integer.parseInt(normalizada);
                if (cantidad >= 1 && cantidad <= com.legendoftecla.validation.Limites.ALIADOS_MAXIMOS) {
                    return cantidad;
                }
            } catch (NumberFormatException ignored) {
                // El mensaje comun explica las alternativas admitidas.
            }
            consola.imprimir("Respuesta invalida. Escribe no, auto o una cantidad entre 1 y "
                    + com.legendoftecla.validation.Limites.ALIADOS_MAXIMOS + ".", TipoMensaje.ERROR);
        }
    }

    private static CondicionVictoria leerCondicionVictoria(Consola consola) {
        while (true) {
            String entrada = consola.leer(
                    "Condicion de victoria (1=solo jugador, 2=jugador y todos los aliados) [2]:");
            if (entrada == null || entrada.isBlank()) {
                return CondicionVictoria.JUGADOR_Y_ALIADOS;
            }
            CondicionVictoria condicion = CondicionVictoria.desdeTexto(entrada);
            if (condicion != null) {
                return condicion;
            }
            consola.imprimir("Condicion invalida. Escribe 1 o 2.", TipoMensaje.ERROR);
        }
    }

    private static int leerNivelAliados(Consola consola) {
        while (true) {
            String entrada = consola.leer(
                    "Nivel de todos los aliados (auto o 1-100) [auto]:");
            if (entrada == null || entrada.isBlank() || "auto".equalsIgnoreCase(entrada.trim())) {
                return 0;
            }
            try {
                int nivel = Integer.parseInt(entrada.trim());
                if (nivel >= 1
                        && nivel <= com.legendoftecla.validation.Limites.NIVEL_ALIADO_MAXIMO) {
                    return nivel;
                }
            } catch (NumberFormatException ignored) {
                // El mensaje comun informa del formato valido.
            }
            consola.imprimir("Nivel invalido. Escribe auto o un valor entre 1 y "
                    + com.legendoftecla.validation.Limites.NIVEL_ALIADO_MAXIMO + ".",
                    TipoMensaje.ERROR);
        }
    }

    private static int leerNivelJugador(Consola consola) {
        while (true) {
            String entrada = consola.leer("Nivel inicial del jugador (1-100) [1]:").trim();
            if (entrada.isBlank()) return 1;
            try {
                int nivel = Integer.parseInt(entrada);
                if (nivel >= 1 && nivel <= 100) return nivel;
            } catch (NumberFormatException ignored) {
                // Se informa de forma uniforme bajo estas lineas.
            }
            consola.imprimir("Nivel invalido: indica un valor entre 1 y 100.", TipoMensaje.ERROR);
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
