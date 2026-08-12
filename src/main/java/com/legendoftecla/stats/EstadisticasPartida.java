package com.legendoftecla.stats;

import com.legendoftecla.events.*;

/** Proyeccion de estadisticas alimentada exclusivamente por eventos. */
public final class EstadisticasPartida implements AutoCloseable {
    private final Suscripcion suscripcion;
    private int danoCausado;
    private int danoRecibido;
    private int muertes;
    private int enemigosEliminados;
    private int aliadosEvacuados;
    private int incendiosApagados;
    private int objetosUsados;
    private int pasos;
    private int disparos;
    private int trampasDesactivadas;
    private int celdasInspeccionadas;
    private final com.legendoftecla.model.world.Juego juego;

    public EstadisticasPartida(BusEventos bus) {
        this(bus, null);
    }
    /** Construye una proyeccion capaz de distinguir bajas enemigas. */
    public EstadisticasPartida(com.legendoftecla.model.world.Juego juego) {
        this(juego.getBusEventos(), juego);
    }
    private EstadisticasPartida(BusEventos bus,
            com.legendoftecla.model.world.Juego juego) {
        this.juego = juego;
        suscripcion = bus.suscribir(EventoJuego.class, this::procesar);
    }

    private void procesar(EventoJuego evento) {
        if (evento instanceof PersonajeMovido) pasos++;
        if (evento instanceof PersonajeAtacado) disparos++;
        if (evento instanceof PersonajeMuerto muerte) {
            muertes++;
            if (juego != null && juego.getEnemigos().stream()
                    .anyMatch(enemigo -> enemigo.getNombre().equals(muerte.personaje()))) {
                enemigosEliminados++;
            }
        }
        if (evento instanceof AliadoEvacuado) aliadosEvacuados++;
        if (evento instanceof IncendioExtinguido) incendiosApagados++;
        if (evento instanceof ObjetoUsado) objetosUsados++;
        if (evento instanceof TrampaDesactivada) trampasDesactivadas++;
        if (evento instanceof CeldaInspeccionada) celdasInspeccionadas++;
        if (evento instanceof PersonajeDanado dano) danoCausado += dano.cantidad();
    }

    /** Registra dano recibido por el jugador cuando el adaptador conoce su identidad. */
    public void registrarDanoRecibido(int cantidad) { danoRecibido += Math.max(0, cantidad); }
    public int getDanoCausado() { return danoCausado; }
    public int getDanoRecibido() { return danoRecibido; }
    public int getMuertes() { return muertes; }
    public int getEnemigosEliminados() { return enemigosEliminados; }
    public int getAliadosEvacuados() { return aliadosEvacuados; }
    public int getIncendiosApagados() { return incendiosApagados; }
    public int getObjetosUsados() { return objetosUsados; }
    public int getPasos() { return pasos; }
    public int getDisparos() { return disparos; }
    public int getTrampasDesactivadas() { return trampasDesactivadas; }
    public int getCeldasInspeccionadas() { return celdasInspeccionadas; }
    public String resumen() {
        return "Pasos: " + pasos + ", disparos: " + disparos + ", dano: "
                + danoCausado + ", evacuados: " + aliadosEvacuados;
    }
    /** Snapshot persistible sin exponer mutabilidad interna. */
    public Snapshot snapshot() {
        return new Snapshot(danoCausado, danoRecibido, muertes, enemigosEliminados,
                aliadosEvacuados,
                incendiosApagados, objetosUsados, pasos, disparos,
                trampasDesactivadas, celdasInspeccionadas);
    }
    /** Restaura acumuladores de una partida guardada. */
    public void restaurar(Snapshot estado) {
        if (estado == null) return;
        danoCausado = noNegativo(estado.danoCausado());
        danoRecibido = noNegativo(estado.danoRecibido());
        muertes = noNegativo(estado.muertes());
        enemigosEliminados = noNegativo(estado.enemigosEliminados());
        aliadosEvacuados = noNegativo(estado.aliadosEvacuados());
        incendiosApagados = noNegativo(estado.incendiosApagados());
        objetosUsados = noNegativo(estado.objetosUsados());
        pasos = noNegativo(estado.pasos());
        disparos = noNegativo(estado.disparos());
        trampasDesactivadas = noNegativo(estado.trampasDesactivadas());
        celdasInspeccionadas = noNegativo(estado.celdasInspeccionadas());
    }
    private int noNegativo(int valor) {
        if (valor < 0) throw new IllegalArgumentException("Estadistica negativa");
        return valor;
    }
    public record Snapshot(int danoCausado, int danoRecibido, int muertes,
            int enemigosEliminados, int aliadosEvacuados, int incendiosApagados, int objetosUsados,
            int pasos, int disparos, int trampasDesactivadas,
            int celdasInspeccionadas) { }
    @Override public void close() { suscripcion.close(); }
}
