package com.legendoftecla.engine;

import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.constants.CondicionVictoria;
import com.legendoftecla.model.world.DimensionesMapa;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;

import java.nio.file.Path;
import java.util.Locale;

/** Configuracion encapsulada compartida por consola y GUI. */
public final class ConfiguracionPartida {
    private String nombreJugador;
    private String clase;
    private String modo;
    private Dificultad dificultad;
    private DimensionesMapa dimensiones;
    private Path directorioDatos;
    private boolean conAliados;
    /** Cero desactiva, menos uno calcula y un valor positivo fija la cantidad. */
    private int cantidadAliados = 0;
    /** Cero mantiene el nivel automatico; un valor positivo lo personaliza. */
    private int nivelAliados = 0;
    private CondicionVictoria condicionVictoria;
    private int varianteMapa;
    private long seed = 1L;

    /**
     * Crea una configuracion completa utilizando exclusivamente setters.
     *
     * @param nombreJugador nombre
     * @param clase clase
     * @param modo modo
     * @param dificultad dificultad
     * @param dimensiones dimensiones opcionales
     * @param directorioDatos directorio opcional
     * @param conAliados aliados
     * @param varianteMapa variante
     */
    public ConfiguracionPartida(String nombreJugador, String clase, String modo, Dificultad dificultad,
            DimensionesMapa dimensiones, Path directorioDatos, boolean conAliados, int varianteMapa) {
        this(nombreJugador, clase, modo, dificultad, dimensiones, directorioDatos, conAliados,
                CondicionVictoria.JUGADOR_Y_ALIADOS, varianteMapa);
    }

    /**
     * Crea una configuracion completa con una condicion de victoria explicita.
     *
     * @param nombreJugador nombre
     * @param clase clase
     * @param modo modo
     * @param dificultad dificultad
     * @param dimensiones dimensiones opcionales
     * @param directorioDatos directorio opcional
     * @param conAliados aliados
     * @param condicionVictoria condicion de llegada
     * @param varianteMapa variante
     */
    public ConfiguracionPartida(String nombreJugador, String clase, String modo, Dificultad dificultad,
            DimensionesMapa dimensiones, Path directorioDatos, boolean conAliados,
            CondicionVictoria condicionVictoria, int varianteMapa) {
        this(nombreJugador, clase, modo, dificultad, dimensiones, directorioDatos,
                conAliados ? -1 : 0, condicionVictoria, varianteMapa);
    }

    /** Crea una configuracion con cantidad automatica ({@code -1}), nula o explicita. */
    public ConfiguracionPartida(String nombreJugador, String clase, String modo, Dificultad dificultad,
            DimensionesMapa dimensiones, Path directorioDatos, int cantidadAliados,
            CondicionVictoria condicionVictoria, int varianteMapa) {
        setNombreJugador(nombreJugador);
        setClase(clase);
        setDificultad(dificultad);
        setDimensiones(dimensiones);
        setDirectorioDatos(directorioDatos);
        setModo(modo);
        setCantidadAliados(cantidadAliados);
        setCondicionVictoria(condicionVictoria);
        setVarianteMapa(varianteMapa);
        validarCoherencia();
    }

    /** @return nombre del jugador */
    public String getNombreJugador() {
        return nombreJugador;
    }

    /** @param nombreJugador nombre obligatorio y acotado */
    public void setNombreJugador(String nombreJugador) {
        this.nombreJugador = Validaciones.textoObligatorio(
                nombreJugador, "Nombre del jugador", Limites.TEXTO_CORTO);
    }

    /** @return clase */
    public String getClase() {
        return clase;
    }

    /** @param clase marine, francotirador o zapador */
    public void setClase(String clase) {
        String valor = Validaciones.textoObligatorio(clase, "Clase", Limites.TEXTO_CORTO)
                .toLowerCase(Locale.ROOT);
        if (!valor.equals("marine") && !valor.equals("francotirador") && !valor.equals("zapador")) {
            throw new IllegalArgumentException("Clase de jugador invalida: " + clase);
        }
        this.clase = valor;
    }

    /** @return modo */
    public String getModo() {
        return modo;
    }

    /** @param modo default, grande o ficheros */
    public void setModo(String modo) {
        String valor = Validaciones.textoObligatorio(modo, "Modo", Limites.TEXTO_CORTO)
                .toLowerCase(Locale.ROOT);
        if (!valor.equals("default") && !valor.equals("grande")
                && !valor.equals("ficheros") && !valor.equals("procedural")) {
            throw new IllegalArgumentException("Modo de juego invalido: " + modo);
        }
        if (valor.equals("ficheros") && directorioDatos == null) {
            throw new IllegalArgumentException("El modo ficheros requiere un directorio de datos.");
        }
        this.modo = valor;
    }

    /** @return dificultad */
    public Dificultad getDificultad() {
        return dificultad;
    }

    /** @param dificultad dificultad; {@code null} se normaliza a normal */
    public void setDificultad(Dificultad dificultad) {
        this.dificultad = dificultad == null ? Dificultad.NORMAL : dificultad;
    }

    /** @return dimensiones opcionales */
    public DimensionesMapa getDimensiones() {
        return dimensiones == null ? null
                : new DimensionesMapa(dimensiones.getFilas(), dimensiones.getColumnas());
    }

    /** @param dimensiones dimensiones opcionales ya delimitadas */
    public void setDimensiones(DimensionesMapa dimensiones) {
        this.dimensiones = dimensiones == null ? null
                : new DimensionesMapa(dimensiones.getFilas(), dimensiones.getColumnas());
    }

    /** @return directorio de datos */
    public Path getDirectorioDatos() {
        return directorioDatos;
    }

    /** @param directorioDatos directorio opcional */
    public void setDirectorioDatos(Path directorioDatos) {
        if (directorioDatos == null && "ficheros".equals(modo)) {
            throw new IllegalArgumentException("El modo ficheros requiere un directorio de datos.");
        }
        this.directorioDatos = directorioDatos == null ? null : directorioDatos.normalize();
    }

    /** @return {@code true} si se generan aliados */
    public boolean isConAliados() {
        return conAliados;
    }

    /** @param conAliados estado solicitado */
    public void setConAliados(boolean conAliados) {
        setCantidadAliados(conAliados ? -1 : 0);
    }

    /** @return menos uno para calculo automatico, cero sin aliados o cantidad exacta */
    public int getCantidadAliados() { return cantidadAliados; }

    /** @param cantidadAliados menos uno, cero o cantidad entre uno y el limite defensivo */
    public void setCantidadAliados(int cantidadAliados) {
        if (cantidadAliados < -1 || cantidadAliados > Limites.ALIADOS_MAXIMOS) {
            throw new IllegalArgumentException("Cantidad de aliados fuera de limites: usa auto o un valor entre 0 y "
                    + Limites.ALIADOS_MAXIMOS + ".");
        }
        this.cantidadAliados = cantidadAliados;
        this.conAliados = cantidadAliados != 0;
    }

    /** @return condicion de llegada necesaria para ganar */
    public CondicionVictoria getCondicionVictoria() {
        return condicionVictoria;
    }

    /** @param condicionVictoria condicion; {@code null} conserva la regla historica */
    public void setCondicionVictoria(CondicionVictoria condicionVictoria) {
        this.condicionVictoria = condicionVictoria == null
                ? CondicionVictoria.JUGADOR_Y_ALIADOS
                : condicionVictoria;
    }

    /** @return variante */
    public int getVarianteMapa() {
        return varianteMapa;
    }

    /** @param varianteMapa variante entre 1 y 50 */
    public void setVarianteMapa(int varianteMapa) {
        this.varianteMapa = Validaciones.enteroEntre(varianteMapa, 1, 50, "Variante del mapa");
    }

    /** @return nombre, conservando la API anterior */
    public String nombreJugador() { return getNombreJugador(); }
    /** @return clase, conservando la API anterior */
    public String clase() { return getClase(); }
    /** @return modo, conservando la API anterior */
    public String modo() { return getModo(); }
    /** @return dificultad, conservando la API anterior */
    public Dificultad dificultad() { return getDificultad(); }
    /** @return dimensiones, conservando la API anterior */
    public DimensionesMapa dimensiones() { return getDimensiones(); }
    /** @return directorio, conservando la API anterior */
    public Path directorioDatos() { return getDirectorioDatos(); }
    /** @return aliados, conservando la API anterior */
    public boolean conAliados() { return isConAliados(); }
    /** @return politica de cantidad de aliados */
    public int cantidadAliados() { return getCantidadAliados(); }
    /** @return cero para automatico o nivel exacto solicitado */
    public int getNivelAliados() { return nivelAliados; }
    /** @param nivelAliados cero o nivel entre uno y cien */
    public void setNivelAliados(int nivelAliados) {
        this.nivelAliados = Validaciones.enteroEntre(nivelAliados, 0,
                Limites.NIVEL_ALIADO_MAXIMO, "Nivel de aliados");
    }
    /** @return nivel solicitado conservando acceso compacto */
    public int nivelAliados() { return getNivelAliados(); }
    /** @return condicion de victoria, conservando el estilo de acceso compacto */
    public CondicionVictoria condicionVictoria() { return getCondicionVictoria(); }
    /** @return variante, conservando la API anterior */
    public int varianteMapa() { return getVarianteMapa(); }
    /** @return semilla procedural */
    public long getSeed() { return seed; }
    /** @param seed semilla reproducible sin restricciones */
    public void setSeed(long seed) { this.seed = seed; }
    /** @return semilla conservando el acceso compacto */
    public long seed() { return seed; }
    private void validarCoherencia() {
        if ("ficheros".equals(modo) && directorioDatos == null) {
            throw new IllegalArgumentException("Selecciona el directorio del escenario.");
        }
    }
}
