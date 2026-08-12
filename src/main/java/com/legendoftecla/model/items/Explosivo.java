package com.legendoftecla.model.items;

import com.legendoftecla.exceptions.ObjetoNoUsableException;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;


/**
 * Representa la entidad Explosivo del juego.
 */
public class Explosivo extends Objeto {
    private final int danio;
    private final int alcanceMaximo;
    /**
     * Ejecuta Explosivo.
      * @param descripcion valor de {@code descripcion}
      * @param nombre valor de {@code nombre}
      * @param peso valor de {@code peso}
     */
    public Explosivo(String nombre, String descripcion, double peso) {
        this(nombre, descripcion, peso, 50, 5);
    }

    /** Constructor protegido para explosivos especializados como granadas. */
    protected Explosivo(String nombre, String descripcion, double peso,
            int danio, int alcanceMaximo) {
        super(nombre, descripcion, peso);
        this.danio = Validaciones.enteroEntre(
                danio, 0, Limites.ESTADISTICA, "Dano del explosivo");
        this.alcanceMaximo = Validaciones.enteroEntre(
                alcanceMaximo, 1, Limites.ESTADISTICA, "Alcance del explosivo");
    }

    /**
     * Obtiene el dano aplicado a cada enemigo de la celda de impacto.
     *
     * @return dano del explosivo
     */
    public int getDanio() {
        return danio;
    }

    /**
     * Obtiene la distancia maxima de lanzamiento en linea recta.
     *
     * @return alcance maximo en celdas
     */
    public int getAlcanceMaximo() {
        return alcanceMaximo;
    }

    @Override
    /**
     * Ejecuta usar.
     */
    public void usar(Personaje personaje) throws ObjetoNoUsableException {
        throw new ObjetoNoUsableException("El explosivo debe lanzarse como parte de una accion de combate.");
    }
}

