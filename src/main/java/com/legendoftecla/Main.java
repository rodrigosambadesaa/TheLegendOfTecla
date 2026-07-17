package com.legendoftecla;

import com.legendoftecla.commands.*;
import com.legendoftecla.console.Consola;
import com.legendoftecla.console.ConsolaNormal;
import com.legendoftecla.console.TipoMensaje;
import com.legendoftecla.config.OpcionesInicio;
import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.exceptions.FinEntradaException;
import com.legendoftecla.exceptions.JuegoException;
import com.legendoftecla.loader.CargadorJuego;
import com.legendoftecla.loader.CargadorJuegoDeFicheros;
import com.legendoftecla.loader.CargadorJuegoGrandeConAliados;
import com.legendoftecla.loader.CargadorJuegoPorDefecto;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.items.Binocular;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.DimensionesMapa;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.model.world.SistemaPuntuacion;

import java.util.HashSet;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Representa la entidad Main del juego.
 */
public class Main {
    /**
     * Ejecuta main.
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

        try {
            ejecutar(opciones);
        } catch (FinEntradaException e) {
            System.out.println("Entrada cerrada. Partida finalizada.");
        }
    }

    private static void ejecutar(OpcionesInicio opciones) {
        Consola consola = new ConsolaNormal();
        consola.imprimir("Bienvenido a The Legend of Tecla", TipoMensaje.INFO);

        String nombre = opciones.nombre() != null
                ? opciones.nombre()
                : consola.leer("Introduce nombre del personaje:");
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

        try {
            CargadorJuego cargador;
            if ("3".equalsIgnoreCase(modo) || "ficheros".equalsIgnoreCase(modo)) {
                Path directorio = opciones.directorioDatos() != null
                        ? opciones.directorioDatos()
                        : Path.of(consola.leer("Ruta del directorio con mapa.txt, objetos.txt, enemigos.txt:"));
                cargador = new CargadorJuegoDeFicheros(consola, nombre, clase,
                        directorio, dificultad, dimensiones);
            } else if ("2".equalsIgnoreCase(modo) || "grande".equalsIgnoreCase(modo)) {
                cargador = new CargadorJuegoGrandeConAliados(consola, nombre, clase, dificultad, dimensiones);
            } else {
                cargador = new CargadorJuegoPorDefecto(consola, nombre, clase, dificultad, dimensiones);
            }

            Juego juego = cargador.cargarJuego();
            CommandContext context = new CommandContext(juego);
            CommandParser parser = new CommandParser(context);

            consola.imprimir("Mapa: " + juego.getMapa().getNombre(), TipoMensaje.INFO);
            consola.imprimir(juego.getMapa().getDescripcion(), TipoMensaje.INFO);
            if (!juego.getAliados().isEmpty()) {
                consola.imprimir("Aliados desplegados: " + juego.getAliados().size(), TipoMensaje.INFO);
            }

            boolean salir = false;
            while (!salir && !juego.jugadorGano() && !juego.jugadorMuerto() && !juego.excedioPasos()) {
                consola.imprimir(juego.getMapa().renderAscii(juego.getJugador().getPosicion(),
                        calcularEnemigosVisibles(juego), calcularAliadosVisibles(juego)));
                ComandoEstado.imprimirEstado(context);
                String linea = consola.leer("accion>");
                try {
                    Comando comando = parser.parse(linea);
                    comando.ejecutar();
                    if (comando instanceof ComandoSalir) {
                        salir = true;
                        continue;
                    }
                    ejecutarTurnoAliados(juego);
                    ejecutarTurnoNPC(juego);
                } catch (ComandoException e) {
                    consola.imprimir("Error de comando: " + e.getMessage(), TipoMensaje.ERROR);
                } catch (Exception e) {
                    consola.imprimir("Error inesperado en el bucle principal: " + e.getMessage(), TipoMensaje.ERROR);
                } finally {
                    juego.getJugador().resetTurno();
                }
            }

            SistemaPuntuacion.EstadoFinalPartida estadoFinal;
            if (juego.jugadorGano()) {
                consola.imprimir("Has llegado al objetivo. Victoria.", TipoMensaje.EXITO);
                estadoFinal = SistemaPuntuacion.EstadoFinalPartida.VICTORIA;
            } else if (juego.jugadorMuerto()) {
                consola.imprimir("Has muerto o te has quedado sin energia.", TipoMensaje.ERROR);
                estadoFinal = SistemaPuntuacion.EstadoFinalPartida.MUERTE;
            } else if (juego.excedioPasos()) {
                consola.imprimir("Superaste el numero maximo de pasos.", TipoMensaje.ADVERTENCIA);
                estadoFinal = SistemaPuntuacion.EstadoFinalPartida.SIN_PASOS;
            } else {
                consola.imprimir("Partida finalizada.", TipoMensaje.INFO);
                estadoFinal = SistemaPuntuacion.EstadoFinalPartida.SALIDA_MANUAL;
            }

            SistemaPuntuacion.ResultadoPuntuacion puntuacion = SistemaPuntuacion.calcular(juego, estadoFinal);
            for (String linea : puntuacion.formatearDesglose()) {
                consola.imprimir(linea, TipoMensaje.INFO);
            }

            new ComandoRecorrido(context).ejecutar();
        } catch (JuegoException e) {
            consola.imprimir("No se pudo iniciar el juego: " + e.getMessage(), TipoMensaje.ERROR);
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
                    "Modo (1=default sin aliados, 2=grande 50x50 con aliados, 3=ficheros):").trim().toLowerCase();
            if (modo.equals("1") || modo.equals("default")
                    || modo.equals("2") || modo.equals("grande")
                    || modo.equals("3") || modo.equals("ficheros")) {
                return modo;
            }
            consola.imprimir("Modo invalido.", TipoMensaje.ERROR);
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
                int filas = Integer.parseInt(partes[0].trim());
                int columnas = Integer.parseInt(partes[1].trim());
                return new DimensionesMapa(filas, columnas);
            } catch (RuntimeException e) {
                consola.imprimir("Tamano invalido: " + e.getMessage(), TipoMensaje.ERROR);
            }
        }
    }

    private static Set<Posicion> calcularAliadosVisibles(Juego juego) {
        Set<Posicion> visibles = new HashSet<>();
        for (Aliado aliado : juego.getAliados()) {
            if (aliado.getSalud() > 0) {
                visibles.add(aliado.getPosicion());
            }
        }
        return visibles;
    }

    private static Set<Posicion> calcularEnemigosVisibles(Juego juego) {
        Set<Posicion> visibles = new HashSet<>();
        Posicion jugadorPos = juego.getJugador().getPosicion();
        int vision = juego.getJugador().getRangoVision();
        for (Enemigo enemigo : juego.getEnemigos()) {
            if (enemigo.getSalud() <= 0) {
                continue;
            }
            if (jugadorPos.distanciaManhattan(enemigo.getPosicion()) <= vision) {
                visibles.add(enemigo.getPosicion());
            }
        }
        return visibles;
    }

    private static void ejecutarTurnoNPC(Juego juego) {
        Random random = new Random();
        List<Enemigo> snapshot = List.copyOf(juego.getEnemigos());
        for (Enemigo enemigo : snapshot) {
            if (enemigo.getSalud() <= 0) {
                continue;
            }
            int movimientos = random.nextInt(3);
            for (int i = 0; i < movimientos; i++) {
                int distancia = enemigo.getPosicion().distanciaManhattan(juego.getJugador().getPosicion());
                if (distancia <= enemigo.getRangoVision()
                        && juego.getMapa().hayLineaAtaque(enemigo.getPosicion(), juego.getJugador().getPosicion())) {
                    enemigo.atacar(juego.getJugador());
                    juego.getConsola().imprimir(enemigo.getNombre() + " te ataca.");
                    break;
                }
                Direccion d = Direccion.values()[random.nextInt(Direccion.values().length)];
                Posicion origen = enemigo.getPosicion();
                Posicion destino = origen.mover(d);
                if (juego.getMapa().esTransitable(destino)) {
                    juego.getMapa().getCelda(origen).quitarEnemigo(enemigo);
                    try {
                        enemigo.mover(d, juego);
                        juego.getMapa().getCelda(enemigo.getPosicion()).agregarEnemigo(enemigo);
                    } catch (Exception ignored) {
                        juego.getMapa().getCelda(origen).agregarEnemigo(enemigo);
                    }
                }
            }
        }
    }

    private static void ejecutarTurnoAliados(Juego juego) {
        List<Aliado> aliados = List.copyOf(juego.getAliados());
        Random random = new Random();
        for (Aliado aliado : aliados) {
            if (aliado.getSalud() <= 0) {
                continue;
            }

            if (aliado.getPosicion().equals(juego.getMapa().getObjetivo())) {
                juego.getMapa().getCelda(aliado.getPosicion()).quitarAliado(aliado);
                if (juego.extraerAliado(aliado)) {
                    juego.getConsola().imprimirInfo(aliado.getNombre() + " sale del mapa con vida."
                            + " (" + juego.getAliadosExtraidos() + "/" + juego.getAliadosIniciales() + ")");
                }
                continue;
            }

            Enemigo objetivo = buscarEnemigoMasCercano(juego, aliado);
            if (objetivo == null) {
                Posicion destino = juego.getJugador().getPosicion().equals(juego.getMapa().getObjetivo())
                        ? juego.getMapa().getObjetivo()
                        : juego.getJugador().getPosicion();
                moverAliadoHaciaObjetivo(juego, aliado, destino);
                if (aliado.getPosicion().equals(juego.getMapa().getObjetivo())) {
                    juego.getMapa().getCelda(aliado.getPosicion()).quitarAliado(aliado);
                    if (juego.extraerAliado(aliado)) {
                        juego.getConsola().imprimirInfo(aliado.getNombre() + " sale del mapa con vida."
                                + " (" + juego.getAliadosExtraidos() + "/" + juego.getAliadosIniciales() + ")");
                    }
                }
                continue;
            }
            int distancia = aliado.getPosicion().distanciaManhattan(objetivo.getPosicion());
            if (distancia <= 1) {
                if (!debeAliadoAtacarConRadar(juego, aliado, objetivo, random)) {
                    continue;
                }
                aliado.atacar(objetivo);
                if (objetivo.getSalud() <= 0) {
                    juego.getMapa().getCelda(objetivo.getPosicion()).quitarEnemigo(objetivo);
                    juego.getConsola().imprimirExito(aliado.getNombre() + " elimina a " + objetivo.getNombre() + ".");
                }
                continue;
            }
            moverAliadoHaciaObjetivo(juego, aliado, objetivo.getPosicion());
        }
    }

    private static boolean debeAliadoAtacarConRadar(Juego juego, Aliado aliado, Enemigo objetivo, Random random) {
        if (!tieneRadar(aliado)) {
            return true;
        }

        double saludRelativa = (double) aliado.getSalud() / Math.max(1, aliado.getSaludMaxima());
        int riesgo = estimarRiesgoRecibido(juego, aliado);
        if (saludRelativa < 0.55) {
            juego.getConsola().imprimirInfo(
                    aliado.getNombre() + " evalua con radar y no se suma al ataque contra " + objetivo.getNombre()
                            + " (salud baja).");
            return false;
        }
        if (riesgo >= aliado.getSalud()) {
            juego.getConsola().imprimirInfo(
                    aliado.getNombre() + " evalua con radar y evita el ataque contra " + objetivo.getNombre()
                            + " (riesgo alto de caer).");
            return false;
        }

        double probabilidadAtacar = saludRelativa >= 0.8 ? 0.70 : 0.50;
        if (riesgo > aliado.getSalud() * 0.35) {
            probabilidadAtacar -= 0.20;
        }
        boolean ataca = random.nextDouble() < probabilidadAtacar;
        if (!ataca) {
            juego.getConsola().imprimirInfo(
                    aliado.getNombre() + " detecta amenazas con radar y decide no atacar contigo este turno.");
        }
        return ataca;
    }

    private static boolean tieneRadar(Aliado aliado) {
        for (Objeto objeto : aliado.getMochila().getObjetos()) {
            if (objeto instanceof Binocular || objeto.getNombre().toLowerCase().contains("radar")) {
                return true;
            }
        }
        return false;
    }

    private static int estimarRiesgoRecibido(Juego juego, Aliado aliado) {
        int defensa = aliado.getArmaduraEquipada() != null ? aliado.getArmaduraEquipada().getDefensa() : 0;
        int golpeEstimado = Math.max(1, 4 - defensa);
        int riesgo = 0;
        for (Enemigo enemigo : juego.getEnemigos()) {
            if (enemigo.getSalud() <= 0) {
                continue;
            }
            int distancia = enemigo.getPosicion().distanciaManhattan(aliado.getPosicion());
            if (distancia <= enemigo.getRangoVision()
                    && juego.getMapa().hayLineaAtaque(enemigo.getPosicion(), aliado.getPosicion())) {
                riesgo += golpeEstimado;
            }
        }
        return riesgo;
    }

    private static Enemigo buscarEnemigoMasCercano(Juego juego, Aliado aliado) {
        Enemigo mejor = null;
        int mejorDistancia = Integer.MAX_VALUE;
        for (Enemigo enemigo : juego.getEnemigos()) {
            if (enemigo.getSalud() <= 0) {
                continue;
            }
            int d = aliado.getPosicion().distanciaManhattan(enemigo.getPosicion());
            if (d < mejorDistancia) {
                mejorDistancia = d;
                mejor = enemigo;
            }
        }
        return mejor;
    }

    private static void moverAliadoHaciaObjetivo(Juego juego, Aliado aliado, Posicion objetivo) {
        Posicion origen = aliado.getPosicion();
        Direccion mejor = null;
        int mejorDist = origen.distanciaManhattan(objetivo);
        for (Direccion d : Direccion.values()) {
            Posicion cand = origen.mover(d);
            if (!juego.getMapa().esTransitable(cand)) {
                continue;
            }
            if (!juego.getMapa().getCelda(cand).getAliados().isEmpty()) {
                continue;
            }
            int dCand = cand.distanciaManhattan(objetivo);
            if (dCand < mejorDist) {
                mejorDist = dCand;
                mejor = d;
            }
        }
        if (mejor == null) {
            return;
        }
        try {
            juego.getMapa().getCelda(origen).quitarAliado(aliado);
            aliado.mover(mejor, juego);
            juego.getMapa().getCelda(aliado.getPosicion()).agregarAliado(aliado);
        } catch (Exception e) {
            juego.getMapa().getCelda(origen).agregarAliado(aliado);
        }
    }
}
