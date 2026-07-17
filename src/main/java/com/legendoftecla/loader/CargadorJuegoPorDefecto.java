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
 * Representa la entidad CargadorJuegoPorDefecto del juego.
 */
public class CargadorJuegoPorDefecto implements CargadorJuego {
    private final Consola consola;
    private final String nombreJugador;
    private final String clase;
    private final Dificultad dificultad;
    private final DimensionesMapa dimensiones;
    private final boolean conAliados;

    /**
     * Ejecuta CargadorJuegoPorDefecto.
      * @param clase valor de {@code clase}
      * @param consola valor de {@code consola}
      * @param dificultad valor de {@code dificultad}
      * @param dimensiones valor de {@code dimensiones}
      * @param nombreJugador valor de {@code nombreJugador}
      * @param conAliados indica si se deben generar aliados automaticamente
     */
    public CargadorJuegoPorDefecto(Consola consola, String nombreJugador, String clase,
            Dificultad dificultad, DimensionesMapa dimensiones, boolean conAliados) {
        this.consola = consola;
        this.nombreJugador = nombreJugador;
        this.clase = clase;
        this.dificultad = dificultad;
        this.dimensiones = dimensiones;
        this.conAliados = conAliados;
    }

    @Override
    /**
     * Ejecuta cargarJuego.
     */
    public Juego cargarJuego() {
        int filas = dimensiones != null ? dimensiones.filas() : 10;
        int columnas = dimensiones != null ? dimensiones.columnas() : 10;
        Mapa mapa = new Mapa("Base Lambda", "Instalacion abandonada de la resistencia", filas, columnas,
                new Posicion(0, 0), new Posicion(filas - 1, columnas - 1));
        for (int f = 0; f < mapa.getFilas(); f++) {
            for (int c = 0; c < mapa.getColumnas(); c++) {
                boolean transitable = !((f % 4 == 1 && c > 1 && c < mapa.getColumnas() - 2)
                        || (c % 5 == 2 && f > 2 && f < mapa.getFilas() - 2));
                mapa.setCelda(f, c, new Celda("Celda " + f + "," + c, transitable));
            }
        }
        mapa.setCelda(0, 0, new Celda("Punto de despliegue", true));
        mapa.setCelda(mapa.getFilas() - 1, mapa.getColumnas() - 1, new Celda("Zona objetivo", true));

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

        Juego juego = new Juego(consola, mapa, jugador, 160);

        Random random = new Random(11);
        poblarObjetos(mapa, random, Math.max(9, (filas * columnas) / 10));

        Enemigo.setMultiplicadorDanioGlobal(dificultad.getMultiplicadorDanioEnemigo());
        int baseEnemigos = Math.max(4, (filas * columnas) / 28);
        int cantidadEnemigos = dificultad.ajustarCantidadEnemigos(baseEnemigos);
        poblarEnemigos(juego, mapa, random, cantidadEnemigos);
        int cantidadAliados = conAliados
                ? GeneradorAliados.poblar(juego, mapa, dificultad, new Random(12), "AliadoBase")
                : 0;
        consola.imprimirInfo("Dificultad: " + dificultad.getEtiqueta()
                + " | enemigos=" + cantidadEnemigos
                + " | aliados=" + cantidadAliados
                + " | salud x" + dificultad.getMultiplicadorSaludEnemigo()
                + " | danio x" + dificultad.getMultiplicadorDanioEnemigo());

        return juego;
    }

    private void poblarObjetos(Mapa mapa, Random random, int cantidad) {
        List<Posicion> ocupadas = new ArrayList<>();
        ocupadas.add(mapa.getInicio());
        ocupadas.add(mapa.getObjetivo());
        for (int i = 0; i < cantidad; i++) {
            Posicion p = randomPosTransitable(mapa, random, ocupadas);
            int tipo = random.nextInt(6);
            switch (tipo) {
                case 0 -> mapa.getCelda(p).agregarObjeto(new Botiquin("botiquin_" + i, "Cura 20 de salud", 1.0, 20));
                case 1 -> mapa.getCelda(p).agregarObjeto(new ToritoRojo("torito_" + i, "Subida de energia", 0.5, 25));
                case 2 ->
                    mapa.getCelda(p).agregarObjeto(new Arma("escopeta_" + i, "Arma de corto alcance", 4.0, 18, false));
                case 3 ->
                    mapa.getCelda(p).agregarObjeto(new Armadura("chaleco_" + i, "Proteccion ligera", 6.0, 4, 10, 10));
                case 4 -> mapa.getCelda(p).agregarObjeto(new Binocular("binocular_" + i, "Amplia vision", 1.2, 2));
                default -> mapa.getCelda(p).agregarObjeto(new Explosivo("c4_" + i, "Explosivo plastico", 2.0));
            }
            ocupadas.add(p);
        }
    }

    private void poblarEnemigos(Juego juego, Mapa mapa, Random random, int cantidad) {
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
