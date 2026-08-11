package com.legendoftecla.loader;

import com.legendoftecla.console.Consola;
import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.constants.GameConstants;
import com.legendoftecla.exceptions.JuegoException;
import com.legendoftecla.model.characters.*;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Botiquin;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.items.CuboAgua;
import com.legendoftecla.model.items.Linterna;
import com.legendoftecla.model.world.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

/**
 * Representa la entidad CargadorJuegoDeFicheros del juego.
 */
public class CargadorJuegoDeFicheros extends CargadorJuegoBase {
    private Path directorio;

    /**
     * Ejecuta CargadorJuegoDeFicheros.
      * @param clase valor de {@code clase}
      * @param consola valor de {@code consola}
      * @param dificultad valor de {@code dificultad}
      * @param dimensiones valor de {@code dimensiones}
      * @param directorio valor de {@code directorio}
      * @param nombreJugador valor de {@code nombreJugador}
      * @param conAliados indica si se deben generar aliados automaticamente
     */
    public CargadorJuegoDeFicheros(Consola consola, String nombreJugador, String clase, Path directorio,
            Dificultad dificultad, DimensionesMapa dimensiones, boolean conAliados) {
        super(consola, nombreJugador, clase, dificultad, dimensiones, conAliados);
        setDirectorio(directorio);
    }

    /** @return directorio de datos normalizado */
    public Path getDirectorio() {
        return directorio;
    }

    /** @param directorio directorio no nulo */
    public void setDirectorio(Path directorio) {
        this.directorio = com.legendoftecla.validation.Validaciones
                .noNulo(directorio, "Directorio de datos").normalize();
    }

    @Override
    /**
     * Ejecuta cargarJuego.
     */
    public Juego cargarJuego() throws JuegoException {
        if (Files.exists(directorio.resolve(SerializadorEscenarioJson.NOMBRE_ARCHIVO))) {
            return new CargadorJuegoJson(consola, nombreJugador, clase, directorio,
                    dificultad, dimensiones, conAliados).cargarJuego();
        }
        try {
            Path mapaPath = directorio.resolve("mapa.txt");
            Path objetosPath = directorio.resolve("objetos.txt");
            Path enemigosPath = directorio.resolve("enemigos.txt");

            List<String> mapaLineas = Files.readAllLines(mapaPath).stream()
                    .filter(l -> !l.trim().startsWith("#") && !l.trim().isEmpty()).toList();
            String[] dims = mapaLineas.get(0).split("x");
            int filasArchivo = Integer.parseInt(dims[0].trim());
            int colsArchivo = Integer.parseInt(dims[1].trim());
            int filas = dimensiones != null ? dimensiones.filas() : filasArchivo;
            int cols = dimensiones != null ? dimensiones.columnas() : colsArchivo;
            Posicion inicio = parsePos(mapaLineas.get(1));
            Posicion objetivo = parsePos(mapaLineas.get(2));
            if (inicio.getFila() >= filas || inicio.getColumna() >= cols
                    || objetivo.getFila() >= filas || objetivo.getColumna() >= cols) {
                throw new JuegoException("Inicio/objetivo quedan fuera del tamano de mapa configurado.");
            }

            Mapa mapa = new Mapa("Mapa desde fichero", "Cargado desde disco", filas, cols, inicio, objetivo);
            for (int f = 0; f < filas; f++) {
                for (int c = 0; c < cols; c++) {
                    mapa.setCelda(f, c, new Celda("Celda " + f + "," + c, true));
                }
            }
            aplicarDirectivasMapa(mapa, mapaLineas.stream().skip(3).toList());

            Jugador jugador = switch (clase.toLowerCase()) {
                case "marine" -> new Marine(nombreJugador, inicio,
                        new Mochila(GameConstants.MOCHILA_CAPACIDAD_MAX, GameConstants.MOCHILA_PESO_MAX),
                        GameConstants.MAX_VISION_BASE);
                case "francotirador" -> new Francotirador(nombreJugador, inicio,
                        new Mochila(GameConstants.MOCHILA_CAPACIDAD_MAX, GameConstants.MOCHILA_PESO_MAX),
                        GameConstants.MAX_VISION_BASE);
                default -> new Zapador(nombreJugador, inicio,
                        new Mochila(GameConstants.MOCHILA_CAPACIDAD_MAX, GameConstants.MOCHILA_PESO_MAX),
                        GameConstants.MAX_VISION_BASE);
            };

            Juego juego = new Juego(consola, mapa, jugador, GameConstants.MAX_STEPS_DEFAULT);
            Enemigo.setMultiplicadorDanioGlobal(dificultad.getMultiplicadorDanioEnemigo());

            for (String l : Files.readAllLines(objetosPath)) {
                String line = l.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] p = line.split(";");
                String nombre = p[0];
                String tipo = p[1];
                int fila = Integer.parseInt(p[2]);
                int col = Integer.parseInt(p[3]);
                Posicion pos = new Posicion(fila, col);
                if (!mapa.estaDentro(pos)) {
                    continue;
                }
                Objeto obj = switch (tipo.toLowerCase()) {
                    case "arma" -> new Arma(nombre, "Arma desde fichero", 3.0, 10, false);
                    case "linterna" -> new Linterna(nombre, "Linterna desde fichero", 0.8, 4);
                    case "cubo", "cuboagua" -> new CuboAgua(nombre, "Cubo desde fichero", 2.0, true);
                    default -> new Botiquin(nombre, "Botiquin desde fichero", 1.0, 15);
                };
                mapa.getCelda(pos).agregarObjeto(obj);
            }
            GeneradorSuministrosDificultad.poblar(mapa, dificultad, new Random(103));
            GeneradorAmbiente.completar(mapa, new Random(109));

            List<String> lineasEnemigos = Files.readAllLines(enemigosPath).stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .toList();
            int cantidadObjetivo = dificultad.ajustarCantidadEnemigos(lineasEnemigos.size());
            Random randomEnemigos = new Random(101);

            int procesados = 0;
            for (String line : lineasEnemigos) {
                if (procesados >= cantidadObjetivo) {
                    break;
                }
                String[] p = line.split(";");
                Enemigo enemigo = crearEnemigoDesdePartes(mapa, p, procesados, randomEnemigos);
                enemigo.escalarSalud(dificultad.getMultiplicadorSaludEnemigo());
                mapa.getCelda(enemigo.getPosicion()).agregarEnemigo(enemigo);
                juego.agregarEnemigo(enemigo);
                procesados++;
            }

            while (procesados < cantidadObjetivo) {
                String tipo = elegirTipoEnemigo(randomEnemigos);
                String[] partes = new String[] { tipo, "Auto_" + procesados, "-1", "-1" };
                Enemigo enemigo = crearEnemigoDesdePartes(mapa, partes, procesados, randomEnemigos);
                enemigo.escalarSalud(dificultad.getMultiplicadorSaludEnemigo());
                mapa.getCelda(enemigo.getPosicion()).agregarEnemigo(enemigo);
                juego.agregarEnemigo(enemigo);
                procesados++;
            }

            consola.imprimirInfo("Dificultad: " + dificultad.getEtiqueta()
                    + " | enemigos archivo=" + lineasEnemigos.size() + " -> " + cantidadObjetivo
                    + " | salud x" + dificultad.getMultiplicadorSaludEnemigo()
                    + " | danio x" + dificultad.getMultiplicadorDanioEnemigo());

            int cantidadAliados = conAliados
                    ? GeneradorAliados.poblar(juego, mapa, dificultad, new Random(77), "AliadoFichero")
                    : 0;
            consola.imprimirInfo("Aliados automaticos=" + cantidadAliados);

            return juego;
        } catch (IOException | RuntimeException e) {
            throw new JuegoException("No se pudo cargar el juego de ficheros: " + e.getMessage());
        }
    }

    private String elegirTipoEnemigo(Random random) {
        return random.nextBoolean() ? "sectoid" : "heavyfloater";
    }

    private void aplicarDirectivasMapa(Mapa mapa, List<String> directivas) {
        for (String directiva : directivas) {
            String[] partes = directiva.split(";");
            if (partes.length != 3) continue;
            Posicion posicion = new Posicion(Integer.parseInt(partes[1]), Integer.parseInt(partes[2]));
            if (!mapa.estaDentro(posicion)) continue;
            Celda celda = mapa.getCelda(posicion);
            switch (partes[0].trim().toLowerCase()) {
                case "oscura" -> celda.setOscuridadPermanente(true);
                case "madera" -> celda.setTipoSuelo(TipoSuelo.MADERA);
                case "antorcha" -> celda.setAntorchaMural(true);
                case "fuente" -> celda.setFuenteAgua(true);
                default -> { }
            }
        }
    }

    private Enemigo crearEnemigoDesdePartes(Mapa mapa, String[] p, int indice, Random random) {
        String tipo = p[0];
        String nombre = p[1];
        Posicion pos;
        if (p.length >= 4) {
            int fila = Integer.parseInt(p[2]);
            int col = Integer.parseInt(p[3]);
            Posicion candidata = new Posicion(fila, col);
            if (mapa.estaDentro(candidata) && mapa.esTransitable(candidata)
                    && mapa.getCelda(candidata).getEnemigos().isEmpty()
                    && mapa.getCelda(candidata).getAliados().isEmpty()
                    && !candidata.equals(mapa.getInicio()) && !candidata.equals(mapa.getObjetivo())) {
                pos = candidata;
            } else {
                pos = randomPosTransitableSinEnemigos(mapa, random, mapa.getInicio(), mapa.getObjetivo());
            }
        } else {
            pos = randomPosTransitableSinEnemigos(mapa, random, mapa.getInicio(), mapa.getObjetivo());
        }
        String nombreFinal = (nombre == null || nombre.isBlank()) ? "EnemigoAuto_" + indice : nombre;
        return tipo.equalsIgnoreCase("sectoid")
                ? new Sectoid(nombreFinal, pos, new Mochila(4, 15), 2)
                : new HeavyFloater(nombreFinal, pos, new Mochila(4, 15), 2);
    }

    private Posicion parsePos(String texto) {
        String[] p = texto.split(",");
        return new Posicion(Integer.parseInt(p[0].trim()), Integer.parseInt(p[1].trim()));
    }

    private Posicion randomPosTransitableSinEnemigos(Mapa mapa, Random random, Posicion inicio, Posicion objetivo) {
        while (true) {
            Posicion p = new Posicion(random.nextInt(mapa.getFilas()), random.nextInt(mapa.getColumnas()));
            if (!mapa.esTransitable(p)) {
                continue;
            }
            if (p.equals(inicio) || p.equals(objetivo)) {
                continue;
            }
            if (!mapa.getCelda(p).getEnemigos().isEmpty() || !mapa.getCelda(p).getAliados().isEmpty()) {
                continue;
            }
            return p;
        }
    }
}
