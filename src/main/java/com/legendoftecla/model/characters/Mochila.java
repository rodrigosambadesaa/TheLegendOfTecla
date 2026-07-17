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
     */
    public Mochila(int capacidadMax, double pesoMax) {
        this.capacidadMax = capacidadMax;
        this.pesoMax = pesoMax;
        this.objetos = new ArrayList<>();
    }

    /**
     * Ejecuta getObjetos.
     */
    public List<Objeto> getObjetos() {
        return Collections.unmodifiableList(objetos);
    }

    /**
     * Ejecuta getCapacidadMax.
     */
    public int getCapacidadMax() {
        return capacidadMax;
    }

    /**
     * Ejecuta getPesoMax.
     */
    public double getPesoMax() {
        return pesoMax;
    }

    /**
     * Ejecuta getPesoActual.
     */
    public double getPesoActual() {
        return objetos.stream().mapToDouble(Objeto::getPeso).sum();
    }

    /**
     * Ejecuta getEspacioRestante.
     */
    public int getEspacioRestante() {
        return capacidadMax - objetos.size();
    }

    /**
     * Ejecuta puedeGuardar.
     */
    public boolean puedeGuardar(Objeto objeto) {
        return objetos.size() < capacidadMax && getPesoActual() + objeto.getPeso() <= pesoMax;
    }

    /**
     * Ejecuta guardar.
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

