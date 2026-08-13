package com.legendoftecla.loader;

import com.legendoftecla.console.Consola;
import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.exceptions.JuegoException;
import com.legendoftecla.model.characters.Francotirador;
import com.legendoftecla.model.characters.Jugador;
import com.legendoftecla.model.characters.Marine;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Zapador;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.items.Botiquin;
import com.legendoftecla.model.items.CatalogoArmas;
import com.legendoftecla.model.items.ClaseArma;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Municion;
import com.legendoftecla.model.items.TipoMunicion;
import com.legendoftecla.model.world.DimensionesMapa;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.procedural.ConfiguracionGeneracion;
import com.legendoftecla.procedural.GeneradorHabitaciones;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Adaptador que expone la generacion procedural a consola y GUI. */
public final class CargadorJuegoProcedural extends CargadorJuegoBase {
    private final long seed;
    public CargadorJuegoProcedural(Consola consola, String nombre, String clase,
            Dificultad dificultad, DimensionesMapa dimensiones, boolean aliados, long seed) {
        super(consola, nombre, clase, dificultad, dimensiones, aliados);
        this.seed = seed;
    }

    /** Crea el cargador procedural con cantidad automatica ({@code -1}), nula o explicita. */
    public CargadorJuegoProcedural(Consola consola, String nombre, String clase,
            Dificultad dificultad, DimensionesMapa dimensiones, int cantidadAliados, long seed) {
        super(consola, nombre, clase, dificultad, dimensiones, cantidadAliados);
        this.seed = seed;
    }
    @Override public Juego cargarJuego() throws JuegoException {
        int filas = dimensiones == null ? 15 : dimensiones.getFilas();
        int columnas = dimensiones == null ? 25 : dimensiones.getColumnas();
        Mapa mapa = new GeneradorHabitaciones().generar(seed,
                ConfiguracionGeneracion.normal(filas, columnas));
        Mochila mochila = new Mochila(10, 50);
        Jugador jugador = switch (clase) {
            case "francotirador" -> new Francotirador(nombreJugador, mapa.getInicio(), mochila, 6);
            case "zapador" -> new Zapador(nombreJugador, mapa.getInicio(), mochila, 4);
            default -> new Marine(nombreJugador, mapa.getInicio(), mochila, 4);
        };
        ClaseArma claseInicial = switch (clase) {
                case "francotirador" -> ClaseArma.RIFLE_PRECISION;
                case "zapador" -> ClaseArma.ESCOPETA;
                default -> ClaseArma.RIFLE_ASALTO;
        };
        TipoMunicion municionInicial;
        try {
            Arma inicial = CatalogoArmas.crearDeClase(
                    claseInicial, new Random(seed ^ 0xA24A5L), "inicial");
            municionInicial = inicial.getTipoMunicion();
            jugador.equipar(inicial);
        } catch (com.legendoftecla.exceptions.AccionInvalidaException error) {
            throw new JuegoException("No se pudo equipar el arma procedural: "
                    + error.getMessage());
        }
        Juego juego = new Juego(consola, mapa, jugador, Math.max(50, filas * columnas));
        poblar(juego, new Random(seed ^ 0x5EED5EEDL), municionInicial);
        int aliadosGenerados = conAliados
                ? GeneradorAliados.poblar(juego, mapa, dificultad,
                        new Random(seed ^ 0xA11AD05L), "AliadoProcedural", cantidadAliados,
                        nivelAliados)
                : 0;
        GeneradorSuministrosPoblacion.poblar(juego, new Random(seed ^ 0x5A1D05L));
        consola.imprimirInfo("Mapa procedural: seed=" + seed
                + " | enemigos=" + juego.getEnemigos().size()
                + " | aliados=" + aliadosGenerados);
        return juego;
    }

    private void poblar(Juego juego, Random random, TipoMunicion municionInicial) {
        List<com.legendoftecla.model.world.Posicion> libres = new ArrayList<>();
        for (int fila = 1; fila < juego.getMapa().getFilas() - 1; fila++) {
            for (int columna = 1; columna < juego.getMapa().getColumnas() - 1; columna++) {
                var posicion = new com.legendoftecla.model.world.Posicion(fila, columna);
                if (juego.getMapa().esTransitable(posicion)
                        && !posicion.equals(juego.getMapa().getInicio())
                        && !posicion.equals(juego.getMapa().getObjetivo())) {
                    libres.add(posicion);
                }
            }
        }
        mezclar(libres, random);
        int indice = 0;
        int base = Math.max(2, juego.getMapa().getFilas() * juego.getMapa().getColumnas() / 120);
        int enemigos = Math.min(libres.size(), dificultad.ajustarCantidadEnemigos(base));
        for (int numero = 0; numero < enemigos; numero++, indice++) {
            var posicion = libres.get(indice);
            Sectoid enemigo = new Sectoid("Sectoid-procedural-" + numero, posicion,
                    new Mochila(3, 12), 4);
            enemigo.escalarSalud(dificultad.getMultiplicadorSaludEnemigo());
            com.legendoftecla.engine.ArsenalEnemigo.asignar(enemigo, dificultad);
            juego.agregarEnemigo(enemigo);
            juego.getMapa().getCelda(posicion).agregarEnemigo(enemigo);
        }
        int botiquines = Math.min(libres.size() - indice,
                2 + dificultad.calcularSuministrosExtra(libres.size()));
        for (int numero = 0; numero < botiquines; numero++, indice++) {
            juego.getMapa().getCelda(libres.get(indice)).agregarObjeto(new Botiquin(
                    "Botiquin procedural " + numero, "Suministro generado", 1, 25));
        }
        int paquetes = Math.min(libres.size() - indice,
                dificultad.calcularMunicionExtra(libres.size()));
        for (int numero = 0; numero < paquetes; numero++, indice++) {
            juego.getMapa().getCelda(libres.get(indice)).agregarObjeto(new Municion(
                    "Municion inicial " + numero, 0.5, municionInicial, 12));
        }
    }

    private void mezclar(List<com.legendoftecla.model.world.Posicion> posiciones, Random random) {
        for (int indice = posiciones.size() - 1; indice > 0; indice--) {
            int elegido = random.nextInt(indice + 1);
            var temporal = posiciones.get(indice);
            posiciones.set(indice, posiciones.get(elegido));
            posiciones.set(elegido, temporal);
        }
    }
}
