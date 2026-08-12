package com.legendoftecla.model.characters;

import com.legendoftecla.model.world.Posicion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.legendoftecla.validation.Validaciones;


/**
 * Representa la entidad Jugador del juego.
 */
public abstract class Jugador extends Personaje {
    private List<Posicion> recorrido;
    private com.legendoftecla.progression.ProgresionPersonaje progresion;

    /**
     * Ejecuta Jugador.
      * @param energia valor de {@code energia}
      * @param mochila valor de {@code mochila}
      * @param nombre valor de {@code nombre}
      * @param posicion valor de {@code posicion}
      * @param salud valor de {@code salud}
      * @param visionBase valor de {@code visionBase}
     */
    protected Jugador(String nombre, int salud, int energia, Posicion posicion, Mochila mochila, int visionBase) {
        super(nombre, salud, energia, posicion, mochila, visionBase);
        progresion = new com.legendoftecla.progression.ProgresionPersonaje();
        setRecorrido(List.of(posicion));
    }

    /** @return progresion persistente de campana */
    public com.legendoftecla.progression.ProgresionPersonaje getProgresion() { return progresion; }
    /** @param progresion progreso no nulo, util para cargar campanas */
    public void setProgresion(com.legendoftecla.progression.ProgresionPersonaje progresion) {
        this.progresion = Validaciones.noNulo(progresion, "Progresion");
    }

    /**
     * Ejecuta registrarPosicion.
     */
    public void registrarPosicion() {
        List<Posicion> nuevoRecorrido = new ArrayList<>(recorrido);
        nuevoRecorrido.add(getPosicion());
        setRecorrido(nuevoRecorrido);
    }

    /**
     * Ejecuta getRecorrido.
      * @return resultado de la operacion
     */
    public List<Posicion> getRecorrido() {
        return recorrido.stream()
                .map(posicion -> new Posicion(posicion.getFila(), posicion.getColumna()))
                .toList();
    }

    /**
     * Sustituye el recorrido por una copia sin posiciones nulas.
     *
     * @param recorrido posiciones registradas
     */
    public void setRecorrido(List<Posicion> recorrido) {
        Validaciones.noNulo(recorrido, "Recorrido");
        if (recorrido.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("El recorrido no admite posiciones nulas.");
        }
        this.recorrido = recorrido.stream()
                .map(posicion -> new Posicion(posicion.getFila(), posicion.getColumna()))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }
}

