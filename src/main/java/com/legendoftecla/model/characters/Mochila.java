package com.legendoftecla.model.characters;

import com.legendoftecla.model.items.Objeto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Representa la entidad Mochila del juego.
 */
public class Mochila {
    private final int capacidadMax;
    private final double pesoMax;
    private final List<Objeto> objetos;

    /**
     * Ejecuta Mochila.
      * @param capacidadMax valor de {@code capacidadMax}
      * @param pesoMax valor de {@code pesoMax}
     */
    public Mochila(int capacidadMax, double pesoMax) {
        this.capacidadMax = capacidadMax;
        this.pesoMax = pesoMax;
        this.objetos = new ArrayList<>();
    }

    /**
     * Ejecuta getObjetos.
      * @return resultado de la operacion
     */
    public List<Objeto> getObjetos() {
        return Collections.unmodifiableList(objetos);
    }

    /**
     * Ejecuta getCapacidadMax.
      * @return resultado de la operacion
     */
    public int getCapacidadMax() {
        return capacidadMax;
    }

    /**
     * Ejecuta getPesoMax.
      * @return resultado de la operacion
     */
    public double getPesoMax() {
        return pesoMax;
    }

    /**
     * Ejecuta getPesoActual.
      * @return resultado de la operacion
     */
    public double getPesoActual() {
        return objetos.stream().mapToDouble(Objeto::getPeso).sum();
    }

    /**
     * Ejecuta getEspacioRestante.
      * @return resultado de la operacion
     */
    public int getEspacioRestante() {
        return capacidadMax - objetos.size();
    }

    /**
     * Ejecuta puedeGuardar.
      * @param objeto valor de {@code objeto}
      * @return resultado de la operacion
     */
    public boolean puedeGuardar(Objeto objeto) {
        return objetos.size() < capacidadMax && getPesoActual() + objeto.getPeso() <= pesoMax;
    }

    /**
     * Ejecuta guardar.
      * @param objeto valor de {@code objeto}
      * @return resultado de la operacion
     */
    public boolean guardar(Objeto objeto) {
        if (!puedeGuardar(objeto)) {
            return false;
        }
        objetos.add(objeto);
        return true;
    }

    /**
     * Ejecuta quitarPorNombre.
      * @param nombre valor de {@code nombre}
      * @return resultado de la operacion
     */
    public Objeto quitarPorNombre(String nombre) {
        for (int i = 0; i < objetos.size(); i++) {
            Objeto obj = objetos.get(i);
            if (obj.getNombre().equalsIgnoreCase(nombre)) {
                return objetos.remove(i);
            }
        }
        return null;
    }
}

