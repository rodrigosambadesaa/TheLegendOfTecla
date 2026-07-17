package com.legendoftecla.loader;

import com.legendoftecla.console.Consola;
import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.constants.GameConstants;
import com.legendoftecla.model.characters.*;
import com.legendoftecla.model.items.*;
import com.legendoftecla.model.world.DimensionesMapa;
import com.legendoftecla.model.world.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Representa la entidad CargadorJuegoGrandeConAliados del juego.
 */
public class CargadorJuegoGrandeConAliados implements CargadorJuego {
    private final Consola consola;
    private final String nombreJugador;
    private final String clase;
    private final Dificultad dificultad;
    private final DimensionesMapa dimensiones;

    /**
     * Ejecuta CargadorJuegoGrandeConAliados.
      * @param clase valor de {@code clase}
      * @param consola valor de {@code consola}
      * @param dificultad valor de {@code dificultad}
      * @param dimensiones valor de {@code dimensiones}
      * @param nombreJugador valor de {@code nombreJugador}
     */
    public CargadorJuegoGrandeConAliados(Consola consola, String nombreJugador, String clase,
            Dificultad dificultad, DimensionesMapa dimensiones) {
        this.consola = consola;
        this.nombreJugador = nombreJugador;
        this.clase = clase;
        this.dificultad = dificultad;
        this.dimensiones = dimensiones;
    }

    @Override
    /**
     * Ejecuta cargarJuego.
     */
    public Juego cargarJuego() {
        int filas = dimensiones != null ? dimensiones.filas() : 50;
        int columnas = dimensiones != null ? dimensiones.columnas() : 50;
        Mapa mapa = new Mapa("Megabase Atlas", "Complejo militar de gran escala", filas, columnas, new Posicion(0, 0),
                new Posicion(filas - 1, columnas - 1));

        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                boolean transitable = true;
                if ((f % 7 == 0 && c > 2 && c < columnas - 3) || (c % 9 == 0 && f > 1 && f < filas - 2)) {
                    transitable = (f % 14 == 0) || (c % 18 == 0);
                }
                mapa.setCelda(f, c, new Celda("Sector " + f + "," + c, transitable));
            }
        }
        mapa.setCelda(0, 0, new Celda("Punto de despliegue", true));
        mapa.setCelda(filas - 1, columnas - 1, new Celda("Zona objetivo", true));

        Jugador jugador = switch (clase.toLowerCase()) {
            case "marine" -> new Marine(nombreJugador, mapa.getInicio(),
                    new Mochila(GameConstants.MOCHILA_CAPACIDAD_MAX, GameConstants.MOCHILA_PESO_MAX),
                    GameConstants.MAX_VISION_BASE);
            case "francotirador" -> new Francotirador(nombreJugador, mapa.getInicio(),
                    new Mochila(GameConstants.MOCHILA_CAPACIDAD_MAX, GameConstants.MOCHILA_PESO_MAX),
                    GameConstants.MAX_VISION_BASE);
            default -> new Zapador(nombreJugador, mapa.getInicio(),
                    new Mochila(GameConstants.MOCHILA_CAPACIDAD_MAX, GameConstants.MOCHILA_PESO_MAX),
                    GameConstants.MAX_VISION_BASE);
        };

        Juego juego = new Juego(consola, mapa, jugador, 2200);

        Random random = new Random(42);
        Enemigo.setMultiplicadorDanioGlobal(dificultad.getMultiplicadorDanioEnemigo());
        poblarObjetos(mapa, random, 180);
        poblarEnemigos(juego, mapa, random);
        poblarAliadosSiMapaGrande(juego, mapa, random);
        consola.imprimirInfo("Dificultad: " + dificultad.getEtiqueta()
                + " | salud x" + dificultad.getMultiplicadorSaludEnemigo()
                + " | danio x" + dificultad.getMultiplicadorDanioEnemigo());

        return juego;
    }

    private void poblarObjetos(Mapa mapa, Random random, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            Posicion p = randomPosTransitable(mapa, random, new ArrayList<>());
            int tipo = random.nextInt(5);
            switch (tipo) {
                case 0 -> mapa.getCelda(p).agregarObjeto(new Botiquin("botiquin_" + i, "Curacion media", 1.0, 20));
                case 1 -> mapa.getCelda(p).agregarObjeto(new ToritoRojo("torito_" + i, "Energia instantanea", 0.5, 20));
                case 2 -> mapa.getCelda(p).agregarObjeto(new Arma("rifle_" + i, "Arma tactica", 3.5, 14, false));
                case 3 ->
                    mapa.getCelda(p).agregarObjeto(new Armadura("armadura_" + i, "Blindaje compuesto", 5.5, 3, 8, 8));
                default -> mapa.getCelda(p).agregarObjeto(new Binocular("binocular_" + i, "Vision ampliada", 1.0, 2));
            }
        }
    }

    private void poblarEnemigos(Juego juego, Mapa mapa, Random random) {
        int baseCantidad = Math.max(18, (mapa.getFilas() * mapa.getColumnas()) / 120);
        int cantidad = dificultad.ajustarCantidadEnemigos(baseCantidad);
        List<Posicion> ocupadas = new ArrayList<>();
        ocupadas.add(mapa.getInicio());
        ocupadas.add(mapa.getObjetivo());
        for (int i = 0; i < cantidad; i++) {
            Posicion p = randomPosTransitable(mapa, random, ocupadas);
            Enemigo enemigo;
            int tipo = random.nextInt(3);
            if (tipo == 0) {
                enemigo = new Sectoid("Sectoid_" + i, p, new Mochila(3, 10), 2);
            } else if (tipo == 1) {
                enemigo = new LightFloater("LightFloater_" + i, p, new Mochila(3, 10), 2);
            } else {
                enemigo = new HeavyFloater("HeavyFloater_" + i, p, new Mochila(3, 10), 2);
            }
            enemigo.escalarSalud(dificultad.getMultiplicadorSaludEnemigo());
            mapa.getCelda(p).agregarEnemigo(enemigo);
            juego.agregarEnemigo(enemigo);
            ocupadas.add(p);
        }
    }

    private void poblarAliadosSiMapaGrande(Juego juego, Mapa mapa, Random random) {
        if (mapa.getFilas() <= 20 || mapa.getColumnas() <= 20) {
            return;
        }
        int cantidadAliados = Math.max(3, (mapa.getFilas() * mapa.getColumnas()) / 250);
        List<Posicion> ocupadas = new ArrayList<>();
        ocupadas.add(mapa.getInicio());
        ocupadas.add(mapa.getObjetivo());
        for (int i = 0; i < cantidadAliados; i++) {
            Posicion p = randomPosTransitable(mapa, random, ocupadas);
            Aliado aliado = new Aliado("Aliado_" + i, p, new Mochila(4, 12), 3);
            if (random.nextDouble() < 0.5) {
                aliado.getMochila().guardar(new Binocular("radar_tactico_" + i,
                        "Radar tactico que mejora la evaluacion de amenazas", 1.0, 2));
            }
            mapa.getCelda(p).agregarAliado(aliado);
            juego.agregarAliado(aliado);
            ocupadas.add(p);
        }
    }

    private Posicion randomPosTransitable(Mapa mapa, Random random, List<Posicion> ocupadas) {
        while (true) {
            Posicion p = new Posicion(random.nextInt(mapa.getFilas()), random.nextInt(mapa.getColumnas()));
            if (!mapa.esTransitable(p)) {
                continue;
            }
            if (ocupadas.stream().anyMatch(o -> o.equals(p))) {
                continue;
            }
            return p;
        }
    }
}
