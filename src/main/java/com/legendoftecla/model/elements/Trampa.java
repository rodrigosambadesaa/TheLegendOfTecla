package com.legendoftecla.model.elements;

import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.characters.Zapador;

import java.util.List;
import java.util.Objects;

/** Base de trampas detectables, desactivables y de recursos finitos. */
public abstract class Trampa extends ElementoBase {
    private final int detectabilidad;
    private final int dificultadDesactivacion;
    private final int dano;
    private final int radio;
    private final boolean remota;
    private boolean activa;
    private boolean detectada;

    protected Trampa(String id, int detectabilidad, int dificultadDesactivacion,
            int dano, int radio, boolean remota) {
        super(id, true, 1);
        if (detectabilidad < 0 || dificultadDesactivacion < 0 || dano < 0 || radio < 0) {
            throw new IllegalArgumentException("Parametros de trampa invalidos");
        }
        this.detectabilidad = detectabilidad;
        this.dificultadDesactivacion = dificultadDesactivacion;
        this.dano = dano;
        this.radio = radio;
        this.remota = remota;
        this.activa = true;
    }

    public boolean detectar(Personaje personaje, int percepcion) {
        Objects.requireNonNull(personaje, "Personaje");
        int ventaja = personaje instanceof Zapador ? 4 : 0;
        if (tieneDesactivacionAvanzada(personaje)) ventaja += 2;
        if (percepcion + ventaja >= detectabilidad) detectada = true;
        return detectada;
    }

    public boolean desactivar(Personaje personaje, int habilidad) {
        Objects.requireNonNull(personaje, "Personaje");
        if (!activa) return true;
        int ventaja = personaje instanceof Zapador ? 5 : 0;
        if (tieneDesactivacionAvanzada(personaje)) ventaja += 3;
        if (detectada && habilidad + ventaja >= dificultadDesactivacion) activa = false;
        return !activa;
    }

    /** Activa una vez el efecto sobre la victima. */
    public boolean activar(Personaje victima) {
        return activar(List.of(Objects.requireNonNull(victima, "Victima")));
    }

    /** Activa una vez y aplica su efecto a todas las victimas del radio. */
    public boolean activar(List<? extends Personaje> victimas) {
        if (!activa || remota) return false;
        return consumir(victimas);
    }

    /** Detonacion explicita para trampas remotas. */
    public boolean detonar(Personaje victima) {
        return detonar(List.of(Objects.requireNonNull(victima, "Victima")));
    }

    /** Detona explicitamente una trampa remota sobre todas las victimas del radio. */
    public boolean detonar(List<? extends Personaje> victimas) {
        if (!activa || !remota) return false;
        return consumir(victimas);
    }

    /** Fuerza la detonacion de cualquier trampa activa mediante un disparo. */
    public boolean disparar(List<? extends Personaje> victimas) {
        if (!activa) return false;
        return consumir(victimas);
    }

    protected void aplicarEstado(Personaje victima) { }
    public int getDetectabilidad() { return detectabilidad; }
    public int getDificultadDesactivacion() { return dificultadDesactivacion; }
    public int getDano() { return dano; }
    public int getRadio() { return radio; }
    public boolean isRemota() { return remota; }
    public boolean isActiva() { return activa; }
    public boolean isDetectada() { return detectada; }
    public boolean permitePaso() { return true; }
    public boolean bloqueaVision() { return false; }
    public char simbolo() { return detectada ? '^' : '.'; }

    private boolean consumir(List<? extends Personaje> victimas) {
        List<? extends Personaje> objetivos = List.copyOf(
                Objects.requireNonNull(victimas, "Victimas"));
        activa = false;
        for (Personaje victima : objetivos) {
            victima.recibirDanio(dano);
            aplicarEstado(victima);
        }
        return true;
    }

    private boolean tieneDesactivacionAvanzada(Personaje personaje) {
        return personaje instanceof com.legendoftecla.model.characters.Jugador jugador
                && jugador.getProgresion().tiene(
                        com.legendoftecla.progression.CatalogoHabilidades
                                .DESACTIVACION_AVANZADA);
    }
}
