package com.legendoftecla.model.items;

import com.legendoftecla.exceptions.ObjetoNoUsableException;
import com.legendoftecla.model.characters.Personaje;


/**
 * Representa la entidad Explosivo del juego.
 */
public final class Explosivo extends Objeto {
    private static final int DANIO = 50;
    private static final int ALCANCE_MAXIMO = 5;
    /**
     * Ejecuta Explosivo.
      * @param descripcion valor de {@code descripcion}
      * @param nombre valor de {@code nombre}
      * @param peso valor de {@code peso}
     */
    public Explosivo(String nombre, String descripcion, double peso) {
        super(nombre, descripcion, peso);
    }

    /**
     * Obtiene el dano aplicado a cada enemigo de la celda de impacto.
     *
     * @return dano del explosivo
     */
    public int getDanio() {
        return DANIO;
    }

    /**
     * Obtiene la distancia maxima de lanzamiento en linea recta.
     *
     * @return alcance maximo en celdas
     */
    public int getAlcanceMaximo() {
        return ALCANCE_MAXIMO;
    }

    @Override
    /**
     * Ejecuta usar.
     */
    public void usar(Personaje personaje) throws ObjetoNoUsableException {
        throw new ObjetoNoUsableException("El explosivo debe lanzarse como parte de una accion de combate.");
    }
}

