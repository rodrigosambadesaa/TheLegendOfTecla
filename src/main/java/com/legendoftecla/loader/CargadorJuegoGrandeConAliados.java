package com.legendoftecla.loader;

import com.legendoftecla.console.Consola;
import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.constants.GameConstants;
import com.legendoftecla.model.characters.*;
import com.legendoftecla.model.items.*;
import com.legendoftecla.model.world.DimensionesMapa;
import com.legendoftecla.model.world.*;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Representa la entidad CargadorJuegoGrandeConAliados del juego.
 */
public class CargadorJuegoGrandeConAliados extends CargadorJuegoBase {
    private static final int PASOS_ENTRE_TORITOS_RUTA = 5;
    private static final int ENERGIA_TORITO_GRANDE = 35;

    private int varianteMapa;

    /**
     * Ejecuta CargadorJuegoGrandeConAliados.
      * @param clase valor de {@code clase}
      * @param consola valor de {@code consola}
      * @param dificultad valor de {@code dificultad}
      * @param dimensiones valor de {@code dimensiones}
      * @param nombreJugador valor de {@code nombreJugador}
      * @param conAliados indica si se deben generar aliados automaticamente
      * @param varianteMapa variante determinista del mapa, entre 1 y 50
     */
    public CargadorJuegoGrandeConAliados(Consola consola, String nombreJugador, String clase,
            Dificultad dificultad, DimensionesMapa dimensiones, boolean conAliados, int varianteMapa) {
        super(consola, nombreJugador, clase, dificultad, dimensiones, conAliados);
        setVarianteMapa(varianteMapa);
    }

    /** Crea el mapa grande con cantidad automatica ({@code -1}), nula o explicita. */
    public CargadorJuegoGrandeConAliados(Consola consola, String nombreJugador, String clase,
            Dificultad dificultad, DimensionesMapa dimensiones, int cantidadAliados, int varianteMapa) {
        super(consola, nombreJugador, clase, dificultad, dimensiones, cantidadAliados);
        setVarianteMapa(varianteMapa);
    }

    /** @return variante determinista seleccionada */
    public int getVarianteMapa() {
        return varianteMapa;
    }

    /** @param varianteMapa variante entre 1 y 50 */
    public void setVarianteMapa(int varianteMapa) {
        this.varianteMapa = com.legendoftecla.validation.Validaciones.enteroEntre(
                varianteMapa, 1, 50, "Variante del mapa");
    }

    @Override
    /**
     * Ejecuta cargarJuego.
     */
    public Juego cargarJuego() {
        int filas = dimensiones != null ? dimensiones.filas() : 50;
        int columnas = dimensiones != null ? dimensiones.columnas() : 50;
        Mapa mapa = new Mapa("Megabase Atlas - Variante " + varianteMapa,
                "Complejo militar de gran escala, distribucion " + varianteMapa,
                filas, columnas, new Posicion(0, 0),
                new Posicion(filas - 1, columnas - 1));

        int periodoFilas = 5 + varianteMapa % 5;
        int periodoColumnas = 7 + (varianteMapa * 3) % 6;
        int desfaseFilas = varianteMapa % periodoFilas;
        int desfaseColumnas = (varianteMapa * 2) % periodoColumnas;
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                boolean corredorSeguro = f == 0 || c == columnas - 1;
                boolean muroHorizontal = (f + desfaseFilas) % periodoFilas == 0
                        && c > 2 && c < columnas - 3;
                boolean muroVertical = (c + desfaseColumnas) % periodoColumnas == 0
                        && f > 1 && f < filas - 2;
                boolean abertura = (f + c + varianteMapa) % (6 + varianteMapa % 4) == 0;
                boolean transitable = corredorSeguro || (!(muroHorizontal || muroVertical) || abertura);
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

        GeneradorAmbiente.completar(mapa, new java.util.Random(223));
        Juego juego = new Juego(consola, mapa, jugador, 2200);

        Random random = new Random(4200L + varianteMapa);
        Enemigo.setMultiplicadorDanioGlobal(dificultad.getMultiplicadorDanioEnemigo());
        int cantidadObjetos = Math.max(180, (filas * columnas) / 12);
        poblarObjetos(mapa, random, cantidadObjetos);
        poblarToritosEnRuta(mapa);
        GeneradorSuministrosDificultad.poblar(
                mapa, dificultad, new Random(6200L + varianteMapa));
        poblarEnemigos(juego, mapa, random);
        int aliadosGenerados = conAliados
                ? GeneradorAliados.poblar(juego, mapa, dificultad,
                        new Random(5200L + varianteMapa), "AliadoAtlasV" + varianteMapa,
                        cantidadAliados, nivelAliados)
                : 0;
        GeneradorSuministrosPoblacion.poblar(
                juego, new Random(7200L + varianteMapa));
        consola.imprimirInfo("Dificultad: " + dificultad.getEtiqueta()
                + " | variante=" + varianteMapa
                + " | aliados=" + aliadosGenerados
                + " | salud x" + dificultad.getMultiplicadorSaludEnemigo()
                + " | danio x" + dificultad.getMultiplicadorDanioEnemigo());

        return juego;
    }

    private void poblarObjetos(Mapa mapa, Random random, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            Posicion p = randomPosTransitable(mapa, random, new ArrayList<>());
            int tipo = random.nextInt(6);
            switch (tipo) {
                case 0 -> mapa.getCelda(p).agregarObjeto(new Botiquin("botiquin_" + i, "Curacion media", 1.0, 20));
                case 1, 2 -> mapa.getCelda(p).agregarObjeto(new ToritoRojo(
                        "torito_" + i, "Energia instantanea", 0.5, ENERGIA_TORITO_GRANDE));
                case 3 -> mapa.getCelda(p).agregarObjeto(
                        com.legendoftecla.model.items.CatalogoArmas.crearPorIndice(
                                4200L + varianteMapa, i, "gran-mapa-" + i));
                case 4 ->
                    mapa.getCelda(p).agregarObjeto(new Armadura("armadura_" + i, "Blindaje compuesto", 5.5, 3, 8, 8));
                default -> mapa.getCelda(p).agregarObjeto(new Binocular("binocular_" + i, "Vision ampliada", 1.0, 2));
            }
        }
    }

    private void poblarToritosEnRuta(Mapa mapa) {
        List<Posicion> ruta = buscarRutaTransitable(mapa);
        int numero = 0;
        for (int i = PASOS_ENTRE_TORITOS_RUTA; i < ruta.size() - 1; i += PASOS_ENTRE_TORITOS_RUTA) {
            Posicion posicion = ruta.get(i);
            mapa.getCelda(posicion).agregarObjeto(new ToritoRojo(
                    "torito_ruta_" + numero++,
                    "Suministro de energia de la ruta principal",
                    0.5,
                    ENERGIA_TORITO_GRANDE));
        }
    }

    private List<Posicion> buscarRutaTransitable(Mapa mapa) {
        ArrayDeque<Posicion> pendientes = new ArrayDeque<>();
        Map<Posicion, Posicion> anterior = new HashMap<>();
        pendientes.add(mapa.getInicio());
        anterior.put(mapa.getInicio(), null);

        while (!pendientes.isEmpty()) {
            Posicion actual = pendientes.removeFirst();
            if (actual.equals(mapa.getObjetivo())) {
                break;
            }
            for (Direccion direccion : Direccion.values()) {
                Posicion siguiente = actual.mover(direccion);
                if (mapa.esTransitable(siguiente) && !anterior.containsKey(siguiente)) {
                    anterior.put(siguiente, actual);
                    pendientes.addLast(siguiente);
                }
            }
        }

        if (!anterior.containsKey(mapa.getObjetivo())) {
            return List.of();
        }
        List<Posicion> ruta = new ArrayList<>();
        for (Posicion posicion = mapa.getObjetivo(); posicion != null; posicion = anterior.get(posicion)) {
            ruta.add(posicion);
        }
        Collections.reverse(ruta);
        return ruta;
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
            int tipo = random.nextInt(9);
            enemigo = switch (tipo) {
                case 0 -> new Sectoid("Sectoid_" + i, p, new Mochila(3, 10), 2);
                case 1 -> new LightFloater("LightFloater_" + i, p, new Mochila(3, 10), 2);
                case 2 -> new HeavyFloater("HeavyFloater_" + i, p, new Mochila(3, 10), 2);
                case 3 -> new Berserker("Berserker_" + i, p, new Mochila(3, 10), 3);
                case 4 -> new Medic("Medic_" + i, p, new Mochila(3, 10), 3);
                case 5 -> new Sniper("Sniper_" + i, p, new Mochila(3, 10), 6);
                case 6 -> new Pyro("Pyro_" + i, p, new Mochila(3, 10), 4);
                case 7 -> new Scout("Scout_" + i, p, new Mochila(3, 10), 6);
                default -> new Commander("Commander_" + i, p, new Mochila(3, 10), 5);
            };
            enemigo.escalarSalud(dificultad.getMultiplicadorSaludEnemigo());
            com.legendoftecla.engine.ArsenalEnemigo.asignar(enemigo, dificultad);
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
