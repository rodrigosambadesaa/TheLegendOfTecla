package com.legendoftecla.model.items;

import com.legendoftecla.exceptions.ObjetoNoUsableException;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;


/**
 * Representa la entidad Arma del juego.
 */
public final class Arma extends Objeto {
    private int danio;
    private boolean dosManos;
    private int capacidadCargador;
    private int municionActual;
    private TipoMunicion tipoMunicion;

    /**
     * Ejecuta Arma.
      * @param danio valor de {@code danio}
      * @param descripcion valor de {@code descripcion}
      * @param dosManos valor de {@code dosManos}
      * @param nombre valor de {@code nombre}
      * @param peso valor de {@code peso}
     */
    public Arma(String nombre, String descripcion, double peso, int danio, boolean dosManos) {
        this(nombre, descripcion, peso, danio, dosManos,
                TipoMunicion.INFINITA, 0, 0);
    }

    /** Crea un arma de municion finita con cargador inicial validado. */
    public Arma(String nombre, String descripcion, double peso, int danio,
            boolean dosManos, TipoMunicion tipoMunicion,
            int capacidadCargador, int municionActual) {
        super(nombre, descripcion, peso);
        setDanio(danio);
        setDosManos(dosManos);
        this.tipoMunicion = Validaciones.noNulo(tipoMunicion, "Tipo de municion");
        if (tipoMunicion == TipoMunicion.INFINITA) {
            this.capacidadCargador = 0;
            this.municionActual = 0;
        } else {
            if (capacidadCargador < 1 || municionActual < 0
                    || municionActual > capacidadCargador) {
                throw new IllegalArgumentException("Cargador invalido.");
            }
            this.capacidadCargador = capacidadCargador;
            this.municionActual = municionActual;
        }
    }

    /**
     * Ejecuta getDanio.
      * @return resultado de la operacion
     */
    public int getDanio() {
        return danio;
    }

    /** @param danio dano positivo y acotado */
    public void setDanio(int danio) {
        this.danio = Validaciones.enteroEntre(danio, 1, Limites.ESTADISTICA, "Dano del arma");
    }

    /**
     * Ejecuta isDosManos.
      * @return resultado de la operacion
     */
    public boolean isDosManos() {
        return dosManos;
    }

    /** @param dosManos estado solicitado */
    public void setDosManos(boolean dosManos) {
        this.dosManos = dosManos;
    }

    public int getCapacidadCargador() { return capacidadCargador; }
    public int getMunicionActual() { return municionActual; }
    public TipoMunicion getTipoMunicion() { return tipoMunicion; }
    /** @param capacidad nueva capacidad compatible con la carga actual */
    public void setCapacidadCargador(int capacidad) {
        if (tipoMunicion == TipoMunicion.INFINITA) {
            if (capacidad != 0) throw new IllegalArgumentException("Un arma infinita no tiene cargador.");
        } else if (capacidad < 1 || capacidad < municionActual) {
            throw new IllegalArgumentException("Capacidad de cargador invalida.");
        }
        capacidadCargador = capacidad;
    }
    /** @param actual carga entre cero y la capacidad */
    public void setMunicionActual(int actual) {
        if (tipoMunicion == TipoMunicion.INFINITA) {
            if (actual != 0) throw new IllegalArgumentException("Municion infinita invalida.");
        } else if (actual < 0 || actual > capacidadCargador) {
            throw new IllegalArgumentException("Municion actual invalida.");
        }
        municionActual = actual;
    }
    /** @param tipo tipo compatible; cambiarlo conserva las reglas del cargador */
    public void setTipoMunicion(TipoMunicion tipo) {
        tipoMunicion = Validaciones.noNulo(tipo, "Tipo de municion");
        if (tipo == TipoMunicion.INFINITA) { capacidadCargador = 0; municionActual = 0; }
    }
    public boolean usaMunicionInfinita() { return tipoMunicion == TipoMunicion.INFINITA; }
    public boolean puedeDisparar() { return usaMunicionInfinita() || municionActual > 0; }

    /** Consume un proyectil o falla sin modificar el estado. */
    public boolean consumirDisparo() {
        if (usaMunicionInfinita()) return true;
        if (municionActual <= 0) return false;
        municionActual--;
        return true;
    }

    /** Recarga parcialmente con un paquete compatible y devuelve proyectiles movidos. */
    public int recargar(Municion municion) {
        Validaciones.noNulo(municion, "Municion");
        if (usaMunicionInfinita() || municion.getTipo() != tipoMunicion) return 0;
        int cargados = municion.consumir(capacidadCargador - municionActual);
        municionActual += cargados;
        return cargados;
    }

    /** @return resumen apto para consola y GUI */
    public String estadoArma() {
        return usaMunicionInfinita() ? getNombre() + ": infinita"
                : getNombre() + ": " + municionActual + "/" + capacidadCargador
                        + " " + tipoMunicion.name().toLowerCase();
    }

    @Override
    /**
     * Ejecuta usar.
     */
    public void usar(Personaje personaje) throws ObjetoNoUsableException {
        throw new ObjetoNoUsableException("Las armas no se usan directamente; se equipan.");
    }
}

