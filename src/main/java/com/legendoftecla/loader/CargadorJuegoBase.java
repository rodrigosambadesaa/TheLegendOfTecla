package com.legendoftecla.loader;

import com.legendoftecla.console.Consola;
import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.model.world.DimensionesMapa;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;

import java.util.Locale;

/** Estado comun encapsulado de todos los cargadores de partidas. */
public abstract class CargadorJuegoBase implements CargadorJuego {
    protected Consola consola;
    protected String nombreJugador;
    protected String clase;
    protected Dificultad dificultad;
    protected DimensionesMapa dimensiones;
    protected boolean conAliados;
    protected int cantidadAliados;
    protected int nivelAliados;

    /**
     * Inicializa los atributos comunes exclusivamente mediante setters.
     *
     * @param consola salida del juego
     * @param nombreJugador nombre
     * @param clase clase del jugador
     * @param dificultad dificultad
     * @param dimensiones dimensiones opcionales
     * @param conAliados si se generan aliados
     */
    protected CargadorJuegoBase(Consola consola, String nombreJugador, String clase,
            Dificultad dificultad, DimensionesMapa dimensiones, boolean conAliados) {
        this(consola, nombreJugador, clase, dificultad, dimensiones, conAliados ? -1 : 0);
    }

    /** Inicializa el cargador con cantidad automatica ({@code -1}), nula o explicita. */
    protected CargadorJuegoBase(Consola consola, String nombreJugador, String clase,
            Dificultad dificultad, DimensionesMapa dimensiones, int cantidadAliados) {
        setConsola(consola);
        setNombreJugador(nombreJugador);
        setClase(clase);
        setDificultad(dificultad);
        setDimensiones(dimensiones);
        setCantidadAliados(cantidadAliados);
        setNivelAliados(0);
    }

    /** @return consola asociada */
    public Consola getConsola() { return consola; }
    /** @param consola consola no nula */
    public void setConsola(Consola consola) {
        this.consola = Validaciones.noNulo(consola, "Consola");
    }
    /** @return nombre del jugador */
    public String getNombreJugador() { return nombreJugador; }
    /** @param nombreJugador nombre obligatorio y acotado */
    public void setNombreJugador(String nombreJugador) {
        this.nombreJugador = Validaciones.textoObligatorio(
                nombreJugador, "Nombre del jugador", Limites.TEXTO_CORTO);
    }
    /** @return clase normalizada */
    public String getClase() { return clase; }
    /** @param clase marine, francotirador o zapador */
    public void setClase(String clase) {
        String valor = Validaciones.textoObligatorio(clase, "Clase", Limites.TEXTO_CORTO)
                .toLowerCase(Locale.ROOT);
        if (!valor.equals("marine") && !valor.equals("francotirador") && !valor.equals("zapador")) {
            throw new IllegalArgumentException("Clase de jugador invalida: " + clase);
        }
        this.clase = valor;
    }
    /** @return dificultad */
    public Dificultad getDificultad() { return dificultad; }
    /** @param dificultad dificultad no nula */
    public void setDificultad(Dificultad dificultad) {
        this.dificultad = Validaciones.noNulo(dificultad, "Dificultad");
    }
    /** @return dimensiones opcionales */
    public DimensionesMapa getDimensiones() {
        return dimensiones == null ? null
                : new DimensionesMapa(dimensiones.getFilas(), dimensiones.getColumnas());
    }
    /** @param dimensiones dimensiones opcionales delimitadas por su propia clase */
    public void setDimensiones(DimensionesMapa dimensiones) {
        this.dimensiones = dimensiones == null ? null
                : new DimensionesMapa(dimensiones.getFilas(), dimensiones.getColumnas());
    }
    /** @return si se generan aliados */
    public boolean isConAliados() { return conAliados; }
    /** @param conAliados estado solicitado */
    public void setConAliados(boolean conAliados) { setCantidadAliados(conAliados ? -1 : 0); }
    /** @return menos uno para automatico, cero para ninguno o cantidad explicita */
    public int getCantidadAliados() { return cantidadAliados; }
    /** @param cantidadAliados politica validada de generacion */
    public void setCantidadAliados(int cantidadAliados) {
        if (cantidadAliados < -1 || cantidadAliados > Limites.ALIADOS_MAXIMOS) {
            throw new IllegalArgumentException("Cantidad de aliados fuera de limites.");
        }
        this.cantidadAliados = cantidadAliados;
        this.conAliados = cantidadAliados != 0;
    }
    /** @return cero para automatico o nivel exacto */
    public int getNivelAliados() { return nivelAliados; }
    /** @param nivelAliados cero o nivel configurable */
    public void setNivelAliados(int nivelAliados) {
        this.nivelAliados = Validaciones.enteroEntre(nivelAliados, 0,
                Limites.NIVEL_ALIADO_MAXIMO, "Nivel de aliados");
    }
}
