package com.legendoftecla.progression;

import com.legendoftecla.model.characters.Francotirador;
import com.legendoftecla.model.characters.Jugador;
import com.legendoftecla.model.characters.Marine;
import com.legendoftecla.model.characters.Zapador;

/** Árboles compactos por clase; las reglas de uso consultan los IDs estables. */
public final class CatalogoHabilidades {
    public static final String RESISTENCIA = "resistencia";
    public static final String FUEGO_SUPRESION = "fuego-supresion";
    public static final String DOBLE_PESADA = "doble-arma-pesada";
    public static final String DISPARO_PRECISO = "disparo-preciso";
    public static final String SILENCIADOR = "silenciador";
    public static final String VISION_AVANZADA = "vision-avanzada";
    public static final String DESACTIVACION_AVANZADA = "desactivacion-avanzada";
    public static final String DEMOLICION = "demolicion";
    public static final String REUTILIZAR_EXPLOSIVOS = "reutilizar-explosivos";

    private CatalogoHabilidades() { }

    /** @return árbol correspondiente a la clase concreta */
    public static ArbolHabilidades para(Jugador jugador) {
        java.util.Objects.requireNonNull(jugador, "Jugador");
        if (jugador instanceof Marine) return marine();
        if (jugador instanceof Francotirador) return francotirador();
        if (jugador instanceof Zapador) return zapador();
        throw new IllegalArgumentException("Clase sin arbol de habilidades");
    }

    private static ArbolHabilidades marine() {
        ArbolHabilidades arbol = new ArbolHabilidades();
        arbol.agregar(habilidad(RESISTENCIA, "Resistencia", 2, null,
                jugador -> ampliarSalud(jugador, 15)));
        arbol.agregar(habilidad(FUEGO_SUPRESION, "Fuego de supresion", 3,
                RESISTENCIA, jugador -> ampliarEnergia(jugador, 10)));
        arbol.agregar(habilidad(DOBLE_PESADA, "Doble arma pesada", 4,
                FUEGO_SUPRESION, jugador -> ampliarEnergia(jugador, 15)));
        return arbol;
    }

    private static ArbolHabilidades francotirador() {
        ArbolHabilidades arbol = new ArbolHabilidades();
        arbol.agregar(habilidad(DISPARO_PRECISO, "Disparo preciso", 2, null,
                jugador -> jugador.setVisionBase(jugador.getVisionBase() + 1)));
        arbol.agregar(habilidad(SILENCIADOR, "Silenciador", 3,
                DISPARO_PRECISO, jugador -> ampliarEnergia(jugador, 5)));
        arbol.agregar(habilidad(VISION_AVANZADA, "Vision avanzada", 4,
                SILENCIADOR, jugador -> jugador.setVisionBase(
                        jugador.getVisionBase() + 2)));
        return arbol;
    }

    private static ArbolHabilidades zapador() {
        ArbolHabilidades arbol = new ArbolHabilidades();
        arbol.agregar(habilidad(DESACTIVACION_AVANZADA,
                "Desactivacion avanzada", 2, null,
                jugador -> ampliarEnergia(jugador, 10)));
        arbol.agregar(habilidad(DEMOLICION, "Demolicion", 3,
                DESACTIVACION_AVANZADA, jugador -> ampliarSalud(jugador, 10)));
        arbol.agregar(habilidad(REUTILIZAR_EXPLOSIVOS,
                "Reutilizacion de explosivos", 4, DEMOLICION,
                jugador -> ampliarEnergia(jugador, 15)));
        return arbol;
    }

    private static Habilidad habilidad(String id, String nombre, int nivel,
            String previa, EfectoHabilidad efecto) {
        return new Habilidad(id, nombre, new RequisitoHabilidad(nivel, previa), efecto);
    }

    private static void ampliarSalud(Jugador jugador, int cantidad) {
        int actual = jugador.getSalud();
        jugador.setSaludMaxima(jugador.getSaludMaxima() + cantidad);
        jugador.setSalud(actual + cantidad);
    }

    private static void ampliarEnergia(Jugador jugador, int cantidad) {
        int actual = jugador.getEnergia();
        jugador.setEnergiaMaxima(jugador.getEnergiaMaxima() + cantidad);
        jugador.setEnergia(actual + cantidad);
    }
}
