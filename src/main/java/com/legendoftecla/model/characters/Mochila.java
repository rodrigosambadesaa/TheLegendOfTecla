package com.legendoftecla.model.characters;

import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Representa la entidad Mochila del juego.
 */
public class Mochila {
    private int capacidadMax;
    private double pesoMax;
    private List<Objeto> objetos;

    /**
     * Ejecuta Mochila.
      * @param capacidadMax valor de {@code capacidadMax}
      * @param pesoMax valor de {@code pesoMax}
     */
    public Mochila(int capacidadMax, double pesoMax) {
        setCapacidadMax(capacidadMax);
        setPesoMax(pesoMax);
        setObjetos(List.of());
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

    /** @param capacidadMax capacidad positiva y acotada */
    public void setCapacidadMax(int capacidadMax) {
        int validada = Validaciones.enteroEntre(
                capacidadMax, 1, Limites.CAPACIDAD_MOCHILA, "Capacidad de la mochila");
        if (objetos != null && objetos.size() > validada) {
            throw new IllegalArgumentException("La capacidad no puede ser inferior a los objetos guardados.");
        }
        this.capacidadMax = validada;
    }

    /**
     * Ejecuta getPesoMax.
      * @return resultado de la operacion
     */
    public double getPesoMax() {
        return pesoMax;
    }

    /** @param pesoMax peso maximo positivo y finito */
    public void setPesoMax(double pesoMax) {
        double validado = Validaciones.decimalEntre(
                pesoMax, 0.01, Limites.PESO_MAXIMO, "Peso maximo de la mochila");
        if (objetos != null && getPesoActual() > validado) {
            throw new IllegalArgumentException("El peso maximo no puede ser inferior al peso actual.");
        }
        this.pesoMax = validado;
    }

    /**
     * Sustituye el contenido aplicando capacidad, peso y nulidad.
     *
     * @param objetos nuevo contenido
     */
    public void setObjetos(List<Objeto> objetos) {
        Validaciones.noNulo(objetos, "Objetos de la mochila");
        if (objetos.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("La mochila no admite objetos nulos.");
        }
        if (objetos.size() > capacidadMax) {
            throw new IllegalArgumentException("Se supera la capacidad de la mochila.");
        }
        double peso = objetos.stream().mapToDouble(Objeto::getPeso).sum();
        if (peso > pesoMax) {
            throw new IllegalArgumentException("Se supera el peso maximo de la mochila.");
        }
        this.objetos = new ArrayList<>(objetos);
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
        Validaciones.noNulo(objeto, "Objeto");
        return objetos.size() < capacidadMax && getPesoActual() + objeto.getPeso() <= pesoMax;
    }

    /**
     * Ejecuta guardar.
      * @param objeto valor de {@code objeto}
      * @return resultado de la operacion
     */
    public boolean guardar(Objeto objeto) {
        Validaciones.noNulo(objeto, "Objeto");
        if (!puedeGuardar(objeto)) {
            return false;
        }
        List<Objeto> nuevos = new ArrayList<>(objetos);
        nuevos.add(objeto);
        setObjetos(nuevos);
        return true;
    }

    /**
     * Ejecuta quitarPorNombre.
      * @param nombre valor de {@code nombre}
      * @return resultado de la operacion
     */
    public Objeto quitarPorNombre(String nombre) {
        String nombreValidado = Validaciones.textoObligatorio(
                nombre, "Nombre del objeto", Limites.TEXTO_CORTO);
        for (int i = 0; i < objetos.size(); i++) {
            Objeto obj = objetos.get(i);
            if (obj.getNombre().equalsIgnoreCase(nombreValidado)) {
                List<Objeto> restantes = new ArrayList<>(objetos);
                Objeto retirado = restantes.remove(i);
                setObjetos(restantes);
                return retirado;
            }
        }
        return null;
    }
}

