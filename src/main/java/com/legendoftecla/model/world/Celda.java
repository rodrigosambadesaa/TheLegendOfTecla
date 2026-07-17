package com.legendoftecla.model.world;

import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.items.Objeto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Representa la entidad Celda del juego.
 */
public class Celda {
    private final String descripcion;
    private final boolean transitable;
    private final List<Objeto> objetos;
    private final List<Enemigo> enemigos;
    private final List<Aliado> aliados;

    /**
     * Ejecuta Celda.
     */
    public Celda(String descripcion, boolean transitable) {
        this.descripcion = descripcion;
        this.transitable = transitable;
        this.objetos = new ArrayList<>();
        this.enemigos = new ArrayList<>();
        this.aliados = new ArrayList<>();
    }

    /**
     * Ejecuta getDescripcion.
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Ejecuta isTransitable.
     */
    public boolean isTransitable() {
        return transitable;
    }

    /**
     * Ejecuta getObjetos.
     */
    public List<Objeto> getObjetos() {
        return Collections.unmodifiableList(objetos);
    }

    /**
     * Ejecuta getEnemigos.
     */
    public List<Enemigo> getEnemigos() {
        return Collections.unmodifiableList(enemigos);
    }

    /**
     * Ejecuta getAliados.
     */
    public List<Aliado> getAliados() {
        return Collections.unmodifiableList(aliados);
    }

    /**
     * Ejecuta agregarObjeto.
     */
    public void agregarObjeto(Objeto objeto) {
        objetos.add(objeto);
    }

    /**
     * Ejecuta quitarObjetoPorNombre.
     */
    public Objeto quitarObjetoPorNombre(String nombre) {
        for (int i = 0; i < objetos.size(); i++) {
            Objeto objeto = objetos.get(i);
            if (objeto.getNombre().equalsIgnoreCase(nombre)) {
                return objetos.remove(i);
            }
        }
        return null;
    }

    /**
     * Ejecuta agregarEnemigo.
     */
    public void agregarEnemigo(Enemigo enemigo) {
        enemigos.add(enemigo);
    }

    /**
     * Ejecuta quitarEnemigo.
     */
    public void quitarEnemigo(Enemigo enemigo) {
        enemigos.remove(enemigo);
    }

    /**
     * Ejecuta agregarAliado.
     */
    public void agregarAliado(Aliado aliado) {
        aliados.add(aliado);
    }

    /**
     * Ejecuta quitarAliado.
     */
    public void quitarAliado(Aliado aliado) {
        aliados.remove(aliado);
    }
}

