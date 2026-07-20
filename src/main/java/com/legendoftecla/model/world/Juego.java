package com.legendoftecla.model.world;

import com.legendoftecla.console.Consola;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Jugador;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Representa la entidad Juego del juego.
 */
public class Juego {
    private Consola consola;
    private Mapa mapa;
    private Jugador jugador;
    private List<Enemigo> enemigos;
    private List<Aliado> aliados;
    private List<Aliado> aliadosRegistrados;
    private List<Aliado> aliadosExtraidosDetalle;
    private int pasosMaximos;
    private int aliadosIniciales;
    private int aliadosExtraidos;
    private int pasos;
    private boolean solicitudAyudaAliados;
    private Set<Posicion> celdasInspeccionadas;

    /**
     * Ejecuta Juego.
      * @param consola valor de {@code consola}
      * @param jugador valor de {@code jugador}
      * @param mapa valor de {@code mapa}
      * @param pasosMaximos valor de {@code pasosMaximos}
     */
    public Juego(Consola consola, Mapa mapa, Jugador jugador, int pasosMaximos) {
        setConsola(consola);
        setMapa(mapa);
        setJugador(jugador);
        setPasosMaximos(pasosMaximos);
        if (!mapa.estaDentro(jugador.getPosicion())) {
            throw new IllegalArgumentException("El jugador debe comenzar dentro del mapa.");
        }
        setEnemigos(List.of());
        setAliados(List.of());
        setAliadosRegistrados(List.of());
        setAliadosExtraidosDetalle(List.of());
        setAliadosIniciales(0);
        setAliadosExtraidos(0);
        setPasos(0);
        setSolicitudAyudaAliados(false);
        setCeldasInspeccionadas(Set.of());
    }

    /**
     * Ejecuta getConsola.
      * @return resultado de la operacion
     */
    public Consola getConsola() {
        return consola;
    }

    /** @param consola adaptador de entrada/salida no nulo */
    public void setConsola(Consola consola) {
        this.consola = Validaciones.noNulo(consola, "Consola");
    }

    /**
     * Ejecuta getMapa.
      * @return resultado de la operacion
     */
    public Mapa getMapa() {
        return mapa;
    }

    /** @param mapa mapa no nulo */
    public void setMapa(Mapa mapa) {
        Mapa validado = Validaciones.noNulo(mapa, "Mapa");
        if (jugador != null && !validado.estaDentro(jugador.getPosicion())) {
            throw new IllegalArgumentException("El nuevo mapa dejaria al jugador fuera.");
        }
        if (celdasInspeccionadas != null
                && celdasInspeccionadas.stream().anyMatch(posicion -> !validado.estaDentro(posicion))) {
            throw new IllegalArgumentException("El nuevo mapa dejaria inspecciones fuera de sus limites.");
        }
        this.mapa = validado;
    }

    /**
     * Ejecuta getJugador.
      * @return resultado de la operacion
     */
    public Jugador getJugador() {
        return jugador;
    }

    /** @param jugador jugador no nulo y situado dentro del mapa */
    public void setJugador(Jugador jugador) {
        Jugador validado = Validaciones.noNulo(jugador, "Jugador");
        if (mapa != null && !mapa.estaDentro(validado.getPosicion())) {
            throw new IllegalArgumentException("El jugador debe estar dentro del mapa.");
        }
        this.jugador = validado;
    }

    /**
     * Ejecuta getPasos.
      * @return resultado de la operacion
     */
    public int getPasos() {
        return pasos;
    }

    /** @param pasos pasos no negativos y acotados */
    public void setPasos(int pasos) {
        this.pasos = Validaciones.enteroEntre(
                pasos, 0, Limites.PASOS_MAXIMOS + 1, "Pasos");
    }

    /**
     * Ejecuta getPasosMaximos.
      * @return resultado de la operacion
     */
    public int getPasosMaximos() {
        return pasosMaximos;
    }

    /** @param pasosMaximos limite positivo de pasos */
    public void setPasosMaximos(int pasosMaximos) {
        this.pasosMaximos = Validaciones.enteroEntre(
                pasosMaximos, 1, Limites.PASOS_MAXIMOS, "Pasos maximos");
        if (pasos > Limites.PASOS_MAXIMOS + 1) {
            setPasos(Limites.PASOS_MAXIMOS + 1);
        }
    }

    /**
     * Ejecuta registrarPaso.
     */
    public void registrarPaso() {
        setPasos(Math.min(Limites.PASOS_MAXIMOS + 1, pasos + 1));
    }

    /**
     * Ejecuta agregarEnemigo.
      * @param enemigo valor de {@code enemigo}
     */
    public void agregarEnemigo(Enemigo enemigo) {
        Enemigo validado = Validaciones.noNulo(enemigo, "Enemigo");
        validarPosicionPersonaje(validado.getPosicion(), "enemigo");
        if (!enemigos.contains(validado)) {
            List<Enemigo> nuevos = new ArrayList<>(enemigos);
            nuevos.add(validado);
            setEnemigos(nuevos);
        }
    }

    /**
     * Ejecuta getEnemigos.
      * @return resultado de la operacion
     */
    public List<Enemigo> getEnemigos() {
        return Collections.unmodifiableList(enemigos);
    }

    /** @param enemigos enemigos no nulos situados dentro del mapa */
    public void setEnemigos(List<Enemigo> enemigos) {
        this.enemigos = copiarPersonajes(enemigos, "Enemigos");
    }

    /**
     * Ejecuta agregarAliado.
      * @param aliado valor de {@code aliado}
     */
    public void agregarAliado(Aliado aliado) {
        Aliado validado = Validaciones.noNulo(aliado, "Aliado");
        validarPosicionPersonaje(validado.getPosicion(), "aliado");
        if (!aliadosRegistrados.contains(validado)) {
            List<Aliado> activos = new ArrayList<>(aliados);
            activos.add(validado);
            setAliados(activos);
            List<Aliado> registrados = new ArrayList<>(aliadosRegistrados);
            registrados.add(validado);
            setAliadosRegistrados(registrados);
            setAliadosIniciales(aliadosIniciales + 1);
        }
    }

    /**
     * Ejecuta getAliados.
      * @return resultado de la operacion
     */
    public List<Aliado> getAliados() {
        return Collections.unmodifiableList(aliados);
    }

    /** @param aliados aliados activos no nulos */
    public void setAliados(List<Aliado> aliados) {
        this.aliados = copiarPersonajes(aliados, "Aliados");
    }

    /**
     * Obtiene todos los aliados que participaron en la partida, incluidos los evacuados.
     *
     * @return vista inmutable del historial completo de aliados
     */
    public List<Aliado> getAliadosRegistrados() {
        return Collections.unmodifiableList(aliadosRegistrados);
    }

    /** @param aliadosRegistrados historial completo no nulo */
    public void setAliadosRegistrados(List<Aliado> aliadosRegistrados) {
        this.aliadosRegistrados = copiarPersonajes(aliadosRegistrados, "Aliados registrados");
    }

    /** @return vista inmutable de aliados evacuados */
    public List<Aliado> getAliadosExtraidosDetalle() {
        return Collections.unmodifiableList(aliadosExtraidosDetalle);
    }

    /** @param extraidos aliados evacuados no nulos */
    public void setAliadosExtraidosDetalle(List<Aliado> extraidos) {
        this.aliadosExtraidosDetalle = copiarPersonajes(extraidos, "Aliados extraidos");
    }

    /**
     * Indica si un aliado llego con vida a la salida del mapa.
     *
     * @param aliado aliado cuyo estado se consulta
     * @return {@code true} cuando el aliado ya fue evacuado
     */
    public boolean estaAliadoExtraido(Aliado aliado) {
        return aliadosExtraidosDetalle.contains(aliado);
    }

    /**
     * Ejecuta getAliadosIniciales.
      * @return resultado de la operacion
     */
    public int getAliadosIniciales() {
        return aliadosIniciales;
    }

    /** @param aliadosIniciales cantidad inicial no negativa */
    public void setAliadosIniciales(int aliadosIniciales) {
        this.aliadosIniciales = Validaciones.enteroEntre(
                aliadosIniciales, 0, Limites.ESTADISTICA, "Aliados iniciales");
    }

    /**
     * Ejecuta getAliadosExtraidos.
      * @return resultado de la operacion
     */
    public int getAliadosExtraidos() {
        return aliadosExtraidos;
    }

    /** @param aliadosExtraidos cantidad evacuada entre cero y la inicial */
    public void setAliadosExtraidos(int aliadosExtraidos) {
        this.aliadosExtraidos = Validaciones.enteroEntre(
                aliadosExtraidos, 0, Math.max(0, aliadosIniciales), "Aliados extraidos");
    }

    /**
     * Ejecuta extraerAliado.
      * @param aliado valor de {@code aliado}
      * @return resultado de la operacion
     */
    public boolean extraerAliado(Aliado aliado) {
        if (aliado == null || aliado.getSalud() <= 0) {
            return false;
        }
        if (!aliados.contains(aliado)) {
            return false;
        }
        List<Aliado> activos = new ArrayList<>(aliados);
        activos.remove(aliado);
        setAliados(activos);
        List<Aliado> extraidos = new ArrayList<>(aliadosExtraidosDetalle);
        extraidos.add(aliado);
        setAliadosExtraidosDetalle(extraidos);
        setAliadosExtraidos(aliadosExtraidos + 1);
        return true;
    }

    /** Registra una orden para que los aliados acudan a ayudar al jugador. */
    public void solicitarAyudaAliados() {
        setSolicitudAyudaAliados(true);
    }

    /**
     * Consume la orden de ayuda pendiente para que el motor la active una sola vez.
     *
     * @return {@code true} si habia una solicitud pendiente
     */
    public boolean consumirSolicitudAyudaAliados() {
        boolean pendiente = solicitudAyudaAliados;
        setSolicitudAyudaAliados(false);
        return pendiente;
    }

    /** @return {@code true} si hay una solicitud pendiente */
    public boolean isSolicitudAyudaAliados() {
        return solicitudAyudaAliados;
    }

    /** @param solicitudAyudaAliados estado solicitado */
    public void setSolicitudAyudaAliados(boolean solicitudAyudaAliados) {
        this.solicitudAyudaAliados = solicitudAyudaAliados;
    }

    /**
     * Devuelve copias de las celdas cuyos objetos ya fueron inspeccionados presencialmente.
     *
     * @return conjunto defensivo de posiciones inspeccionadas
     */
    public Set<Posicion> getCeldasInspeccionadas() {
        Set<Posicion> copia = new HashSet<>();
        celdasInspeccionadas.forEach(posicion -> copia.add(
                new Posicion(posicion.getFila(), posicion.getColumna())));
        return Collections.unmodifiableSet(copia);
    }

    /**
     * Sustituye el registro por posiciones validas pertenecientes al mapa.
     *
     * @param celdasInspeccionadas posiciones no nulas y situadas dentro del mapa
     */
    public void setCeldasInspeccionadas(Set<Posicion> celdasInspeccionadas) {
        Validaciones.noNulo(celdasInspeccionadas, "Celdas inspeccionadas");
        if (celdasInspeccionadas.size() > mapa.getFilas() * mapa.getColumnas()
                || celdasInspeccionadas.stream().anyMatch(
                        posicion -> posicion == null || !mapa.estaDentro(posicion))) {
            throw new IllegalArgumentException("Las celdas inspeccionadas deben pertenecer al mapa.");
        }
        Set<Posicion> copia = new HashSet<>();
        celdasInspeccionadas.forEach(posicion -> copia.add(
                new Posicion(posicion.getFila(), posicion.getColumna())));
        this.celdasInspeccionadas = copia;
    }

    /** Registra que el jugador ha mirado la celda en la que se encuentra. */
    public void inspeccionarCeldaActual() {
        Set<Posicion> inspeccionadas = new HashSet<>(celdasInspeccionadas);
        inspeccionadas.add(jugador.getPosicion());
        setCeldasInspeccionadas(inspeccionadas);
    }

    /**
     * Indica si los objetos de una posicion pueden mostrarse al jugador.
     *
     * @param posicion posicion consultada
     * @return {@code true} si el jugador ya estuvo alli y ejecuto {@code mirar}
     */
    public boolean isCeldaInspeccionada(Posicion posicion) {
        return posicion != null && celdasInspeccionadas.contains(posicion);
    }

    /**
     * Ejecuta jugadorGano.
      * @return resultado de la operacion
     */
    public boolean jugadorGano() {
        if (!jugador.getPosicion().equals(mapa.getObjetivo())) {
            return false;
        }
        if (aliadosIniciales <= 0) {
            return true;
        }
        return aliadosExtraidos == aliadosIniciales;
    }

    /**
     * Ejecuta jugadorMuerto.
      * @return resultado de la operacion
     */
    public boolean jugadorMuerto() {
        return jugador.getSalud() <= 0 || jugador.getEnergia() <= 0;
    }

    /**
     * Ejecuta excedioPasos.
      * @return resultado de la operacion
     */
    public boolean excedioPasos() {
        return pasos > pasosMaximos;
    }

    private void validarPosicionPersonaje(Posicion posicion, String tipo) {
        if (!mapa.estaDentro(posicion)) {
            throw new IllegalArgumentException("La posicion del " + tipo + " queda fuera del mapa.");
        }
    }

    private <T extends com.legendoftecla.model.characters.Personaje> List<T> copiarPersonajes(
            List<T> personajes, String campo) {
        Validaciones.noNulo(personajes, campo);
        if (personajes.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException(campo + " no puede contener valores nulos.");
        }
        if (mapa != null) {
            personajes.forEach(personaje -> validarPosicionPersonaje(personaje.getPosicion(), campo));
        }
        return new ArrayList<>(personajes);
    }
}
