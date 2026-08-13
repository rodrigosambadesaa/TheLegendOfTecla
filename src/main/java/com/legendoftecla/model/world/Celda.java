package com.legendoftecla.model.world;

import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.elements.ElementoMapa;
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
    private List<ElementoMapa> elementos;

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
        setElementos(List.of());
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
        return transitable && elementos.stream().allMatch(ElementoMapa::permitePaso);
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
    /** @return transitabilidad del terreno sin elementos dinamicos */
    public boolean isTerrenoTransitable() { return transitable; }

    /** @return elementos interactivos presentes */
    public List<ElementoMapa> getElementos() { return Collections.unmodifiableList(elementos); }
    /** @param elementos elementos no nulos y con IDs unicos */
    public void setElementos(List<ElementoMapa> elementos) {
        List<ElementoMapa> copia = copiarValidada(elementos, "Elementos");
        if (copia.stream().map(ElementoMapa::getId).distinct().count() != copia.size()) {
            throw new IllegalArgumentException("Los IDs de elementos deben ser unicos por celda.");
        }
        this.elementos = copia;
    }
    /** Agrega un elemento si no existe su identidad. */
    public void agregarElemento(ElementoMapa elemento) {
        List<ElementoMapa> nuevos = new ArrayList<>(elementos);
        if (nuevos.stream().noneMatch(actual -> actual.getId().equals(elemento.getId()))) {
            nuevos.add(Validaciones.noNulo(elemento, "Elemento"));
            setElementos(nuevos);
        }
    }
    /** @return si la celda interrumpe vision o linea de tiro */
    public boolean bloqueaVision() {
        return !transitable || elementos.stream().anyMatch(ElementoMapa::bloqueaVision);
    }
    /** @return simbolo de elemento prioritario, o cero si no existe */
    public char simboloElemento() {
        return elementos.stream().filter(elemento -> !elemento.estaDestruido())
                .map(ElementoMapa::simbolo).findFirst().orElse((char) 0);
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
            enemigos.add(validado);
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
            aliados.add(validado);
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

