package com.legendoftecla.loader;

import com.legendoftecla.console.Consola;
import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.constants.GameConstants;
import com.legendoftecla.exceptions.JuegoException;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Francotirador;
import com.legendoftecla.model.characters.HeavyFloater;
import com.legendoftecla.model.characters.Jugador;
import com.legendoftecla.model.characters.LightFloater;
import com.legendoftecla.model.characters.Marine;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.characters.Zapador;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Armadura;
import com.legendoftecla.model.items.Binocular;
import com.legendoftecla.model.items.Botiquin;
import com.legendoftecla.model.items.Explosivo;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.items.ToritoRojo;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.DimensionesMapa;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;

import java.nio.file.Path;
import java.util.Locale;

/** Carga el formato completo generado por el editor grafico. */
public final class CargadorJuegoJson implements CargadorJuego {
    private final Consola consola;
    private final String nombreJugador;
    private final String clase;
    private final Path directorio;
    private final Dificultad dificultad;
    private final DimensionesMapa dimensiones;

    /**
     * Crea una instancia de {@code CargadorJuegoJson}.
      * @param clase valor de {@code clase}
      * @param consola valor de {@code consola}
      * @param dificultad valor de {@code dificultad}
      * @param dimensiones valor de {@code dimensiones}
      * @param directorio valor de {@code directorio}
      * @param nombreJugador valor de {@code nombreJugador}
     */
    public CargadorJuegoJson(Consola consola, String nombreJugador, String clase, Path directorio,
            Dificultad dificultad, DimensionesMapa dimensiones) {
        this.consola = consola;
        this.nombreJugador = nombreJugador;
        this.clase = clase;
        this.directorio = directorio;
        this.dificultad = dificultad;
        this.dimensiones = dimensiones;
    }

    @Override
    public Juego cargarJuego() throws JuegoException {
        EscenarioDefinicion definicion = SerializadorEscenarioJson.cargar(directorio);
        int filas = dimensiones == null ? definicion.filas : dimensiones.filas();
        int columnas = dimensiones == null ? definicion.columnas : dimensiones.columnas();
        if (filas < definicion.filas || columnas < definicion.columnas) {
            throw new JuegoException("Las dimensiones configuradas no pueden recortar el escenario JSON.");
        }

        Posicion inicio = posicion(definicion.inicio);
        Posicion objetivo = posicion(definicion.objetivo);
        Mapa mapa = new Mapa(definicion.nombre, definicion.descripcion, filas, columnas, inicio, objetivo);
        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                mapa.setCelda(fila, columna, new Celda("Celda " + fila + "," + columna, true));
            }
        }
        for (EscenarioDefinicion.CeldaDef celda : definicion.celdas) {
            mapa.setCelda(celda.fila, celda.columna,
                    new Celda(celda.descripcion == null ? "Celda" : celda.descripcion, celda.transitable));
        }

        Jugador jugador = crearJugador(inicio);
        Juego juego = new Juego(consola, mapa, jugador, definicion.pasosMaximos);
        Enemigo.setMultiplicadorDanioGlobal(dificultad.getMultiplicadorDanioEnemigo());

        for (EscenarioDefinicion.ObjetoDef objetoDef : definicion.objetos) {
            Posicion posicion = posicion(objetoDef);
            exigirTransitable(mapa, posicion, "objeto " + objetoDef.nombre);
            mapa.getCelda(posicion).agregarObjeto(crearObjeto(objetoDef));
        }

        int cantidadEnemigos = dificultad.ajustarCantidadEnemigos(definicion.enemigos.size());
        for (int indice = 0; indice < cantidadEnemigos; indice++) {
            EscenarioDefinicion.PersonajeDef personajeDef =
                    definicion.enemigos.get(indice % definicion.enemigos.size());
            Posicion posicion = posicion(personajeDef);
            exigirTransitable(mapa, posicion, "enemigo " + personajeDef.nombre);
            String nombre = indice < definicion.enemigos.size()
                    ? personajeDef.nombre
                    : personajeDef.nombre + "_extra_" + indice;
            Enemigo enemigo = crearEnemigo(personajeDef, nombre, posicion);
            enemigo.escalarSalud(dificultad.getMultiplicadorSaludEnemigo());
            mapa.getCelda(posicion).agregarEnemigo(enemigo);
            juego.agregarEnemigo(enemigo);
        }

        for (EscenarioDefinicion.PersonajeDef personajeDef : definicion.aliados) {
            Posicion posicion = posicion(personajeDef);
            exigirTransitable(mapa, posicion, "aliado " + personajeDef.nombre);
            Aliado aliado = new Aliado(personajeDef.nombre, posicion, new Mochila(8, 30), personajeDef.vision);
            aliado.configurarEstadisticas(personajeDef.salud, personajeDef.energia, personajeDef.vision);
            mapa.getCelda(posicion).agregarAliado(aliado);
            juego.agregarAliado(aliado);
        }

        consola.imprimirInfo("Escenario JSON cargado: " + definicion.nombre
                + " | dificultad=" + dificultad.getEtiqueta()
                + " | enemigos=" + cantidadEnemigos
                + " | aliados=" + definicion.aliados.size());
        return juego;
    }

    private Jugador crearJugador(Posicion inicio) {
        Mochila mochila = new Mochila(GameConstants.MOCHILA_CAPACIDAD_MAX, GameConstants.MOCHILA_PESO_MAX);
        return switch (clase.toLowerCase(Locale.ROOT)) {
            case "marine" -> new Marine(nombreJugador, inicio, mochila, GameConstants.MAX_VISION_BASE);
            case "francotirador" -> new Francotirador(
                    nombreJugador, inicio, mochila, GameConstants.MAX_VISION_BASE);
            default -> new Zapador(nombreJugador, inicio, mochila, GameConstants.MAX_VISION_BASE);
        };
    }

    private Enemigo crearEnemigo(EscenarioDefinicion.PersonajeDef definicion,
            String nombre, Posicion posicion) {
        Mochila mochila = new Mochila(8, 30);
        Enemigo enemigo = switch (definicion.tipo.toLowerCase(Locale.ROOT)) {
            case "lightfloater", "light_floater" -> new LightFloater(nombre, posicion, mochila, definicion.vision);
            case "heavyfloater", "heavy_floater" -> new HeavyFloater(nombre, posicion, mochila, definicion.vision);
            default -> new Sectoid(nombre, posicion, mochila, definicion.vision);
        };
        enemigo.configurarEstadisticas(definicion.salud, definicion.energia, definicion.vision);
        return enemigo;
    }

    private Objeto crearObjeto(EscenarioDefinicion.ObjetoDef definicion) {
        String tipo = definicion.tipo == null ? "botiquin" : definicion.tipo.toLowerCase(Locale.ROOT);
        String descripcion = definicion.descripcion == null ? "" : definicion.descripcion;
        return switch (tipo) {
            case "arma" -> new Arma(definicion.nombre, descripcion, definicion.peso,
                    Math.max(1, definicion.valor), definicion.dosManos);
            case "armadura" -> new Armadura(definicion.nombre, descripcion, definicion.peso,
                    Math.max(0, definicion.valor), Math.max(0, definicion.valorSecundario),
                    Math.max(0, definicion.valorTerciario));
            case "binocular", "radar" -> new Binocular(definicion.nombre, descripcion, definicion.peso,
                    Math.max(1, definicion.valor));
            case "torito", "toritorojo", "energia" -> new ToritoRojo(
                    definicion.nombre, descripcion, definicion.peso, Math.max(1, definicion.valor));
            case "explosivo" -> new Explosivo(definicion.nombre, descripcion, definicion.peso);
            default -> new Botiquin(definicion.nombre, descripcion, definicion.peso,
                    Math.max(1, definicion.valor));
        };
    }

    private Posicion posicion(EscenarioDefinicion.Punto punto) {
        return new Posicion(punto.fila, punto.columna);
    }

    private void exigirTransitable(Mapa mapa, Posicion posicion, String elemento) throws JuegoException {
        if (!mapa.esTransitable(posicion)) {
            throw new JuegoException("La posicion de " + elemento + " no es transitable.");
        }
    }
}
