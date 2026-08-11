package com.legendoftecla.model.world;

import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Representa la entidad Celda del juego.
 */
public class Celda {
    private String descripcion;
    private boolean transitable;
    private List<Objeto> objetos;
    private List<Enemigo> enemigos;
    private List<Aliado> aliados;
    private TipoSuelo tipoSuelo;
    private boolean oscura;
    private boolean oscuridadPermanente;
    private boolean antorchaMural;
    private boolean fuenteAgua;
    private int nivelFuego;

    /**
     * Ejecuta Celda.
      * @param descripcion valor de {@code descripcion}
      * @param transitable valor de {@code transitable}
     */
    public Celda(String descripcion, boolean transitable) {
        setDescripcion(descripcion);
        setTransitable(transitable);
        setObjetos(List.of());
        setEnemigos(List.of());
        setAliados(List.of());
        setTipoSuelo(TipoSuelo.PIEDRA);
        setOscura(false);
        setOscuridadPermanente(false);
        setAntorchaMural(false);
        setFuenteAgua(false);
        setNivelFuego(0);
    }

    public TipoSuelo getTipoSuelo() { return tipoSuelo; }
    public void setTipoSuelo(TipoSuelo tipoSuelo) {
        this.tipoSuelo = Validaciones.noNulo(tipoSuelo, "Tipo de suelo");
    }
    public boolean isOscura() { return oscura; }
    public void setOscura(boolean oscura) { this.oscura = oscura; }
    public boolean isOscuridadPermanente() { return oscuridadPermanente; }
    public void setOscuridadPermanente(boolean oscuridadPermanente) {
        this.oscuridadPermanente = oscuridadPermanente;
        if (oscuridadPermanente) setOscura(true);
    }
    public boolean hasAntorchaMural() { return antorchaMural; }
    public boolean isAntorchaMural() { return antorchaMural; }
    public void setAntorchaMural(boolean antorchaMural) { this.antorchaMural = antorchaMural; }
    public boolean hasFuenteAgua() { return fuenteAgua; }
    public boolean isFuenteAgua() { return fuenteAgua; }
    public void setFuenteAgua(boolean fuenteAgua) { this.fuenteAgua = fuenteAgua; }
    public int getNivelFuego() { return nivelFuego; }
    public void setNivelFuego(int nivelFuego) {
        this.nivelFuego = Validaciones.enteroEntre(nivelFuego, 0, 3, "Nivel de fuego");
    }
    public boolean estaArdiendo() { return nivelFuego > 0; }

    /**
     * Ejecuta getDescripcion.
      * @return resultado de la operacion
     */
    public String getDescripcion() {
        return descripcion;
    }

    /** @param descripcion descripcion no nula y acotada */
    public void setDescripcion(String descripcion) {
        this.descripcion = Validaciones.texto(
                descripcion, "Descripcion de la celda", Limites.DESCRIPCION);
    }

    /**
     * Ejecuta isTransitable.
      * @return resultado de la operacion
     */
    public boolean isTransitable() {
        return transitable;
    }

    /** @param transitable estado solicitado */
    public void setTransitable(boolean transitable) {
        this.transitable = transitable;
    }

    /**
     * Ejecuta getObjetos.
      * @return resultado de la operacion
     */
    public List<Objeto> getObjetos() {
        return Collections.unmodifiableList(objetos);
    }

    /**
     * Ejecuta getEnemigos.
      * @return resultado de la operacion
     */
    public List<Enemigo> getEnemigos() {
        return Collections.unmodifiableList(enemigos);
    }

    /**
     * Ejecuta getAliados.
      * @return resultado de la operacion
     */
    public List<Aliado> getAliados() {
        return Collections.unmodifiableList(aliados);
    }

    /** @param objetos contenido de objetos no nulo */
    public void setObjetos(List<Objeto> objetos) {
        this.objetos = copiarValidada(objetos, "Objetos");
    }

    /** @param enemigos contenido de enemigos no nulo */
    public void setEnemigos(List<Enemigo> enemigos) {
        this.enemigos = copiarValidada(enemigos, "Enemigos");
    }

    /** @param aliados contenido de aliados no nulo */
    public void setAliados(List<Aliado> aliados) {
        this.aliados = copiarValidada(aliados, "Aliados");
    }

    /**
     * Ejecuta agregarObjeto.
      * @param objeto valor de {@code objeto}
     */
    public void agregarObjeto(Objeto objeto) {
        Objeto validado = Validaciones.noNulo(objeto, "Objeto");
        if (!objetos.contains(validado)) {
            List<Objeto> nuevos = new ArrayList<>(objetos);
            nuevos.add(validado);
            setObjetos(nuevos);
        }
    }

    /**
     * Ejecuta quitarObjetoPorNombre.
      * @param nombre valor de {@code nombre}
      * @return resultado de la operacion
     */
    public Objeto quitarObjetoPorNombre(String nombre) {
        String nombreValidado = Validaciones.textoObligatorio(
                nombre, "Nombre del objeto", Limites.TEXTO_CORTO);
        for (int i = 0; i < objetos.size(); i++) {
            Objeto objeto = objetos.get(i);
            if (objeto.getNombre().equalsIgnoreCase(nombreValidado)) {
                List<Objeto> restantes = new ArrayList<>(objetos);
                Objeto retirado = restantes.remove(i);
                setObjetos(restantes);
                return retirado;
            }
        }
        return null;
    }

    /**
     * Ejecuta agregarEnemigo.
      * @param enemigo valor de {@code enemigo}
     */
    public void agregarEnemigo(Enemigo enemigo) {
        Enemigo validado = Validaciones.noNulo(enemigo, "Enemigo");
        if (!enemigos.contains(validado)) {
            List<Enemigo> nuevos = new ArrayList<>(enemigos);
            nuevos.add(validado);
            setEnemigos(nuevos);
        }
    }

    /**
     * Ejecuta quitarEnemigo.
      * @param enemigo valor de {@code enemigo}
     */
    public void quitarEnemigo(Enemigo enemigo) {
        List<Enemigo> restantes = new ArrayList<>(enemigos);
        restantes.remove(Validaciones.noNulo(enemigo, "Enemigo"));
        setEnemigos(restantes);
    }

    /**
     * Ejecuta agregarAliado.
      * @param aliado valor de {@code aliado}
     */
    public void agregarAliado(Aliado aliado) {
        Aliado validado = Validaciones.noNulo(aliado, "Aliado");
        if (!aliados.contains(validado)) {
            List<Aliado> nuevos = new ArrayList<>(aliados);
            nuevos.add(validado);
            setAliados(nuevos);
        }
    }

    /**
     * Ejecuta quitarAliado.
      * @param aliado valor de {@code aliado}
     */
    public void quitarAliado(Aliado aliado) {
        List<Aliado> restantes = new ArrayList<>(aliados);
        restantes.remove(Validaciones.noNulo(aliado, "Aliado"));
        setAliados(restantes);
    }

    private <T> List<T> copiarValidada(List<T> valores, String campo) {
        Validaciones.noNulo(valores, campo);
        if (valores.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException(campo + " no puede contener valores nulos.");
        }
        return new ArrayList<>(valores);
    }
}

