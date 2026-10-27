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
    private final java.util.Map<String, String> ultimoAtacante = new java.util.HashMap<>();

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
        if (evento instanceof PersonajeMovido) pasos = sumarSaturado(pasos, 1);
        if (evento instanceof PersonajeAtacado ataque) {
            ultimoAtacante.put(ataque.objetivo(), ataque.atacante());
            if (esAtaqueDelJugador(ataque)) disparos = sumarSaturado(disparos, 1);
        }
        if (evento instanceof PersonajeMuerto muerte) {
            muertes = sumarSaturado(muertes, 1);
            if (juego != null && juego.getEnemigos().stream()
                    .anyMatch(enemigo -> enemigo.getNombre().equals(muerte.personaje()))) {
                enemigosEliminados = sumarSaturado(enemigosEliminados, 1);
            }
        }
        if (evento instanceof AliadoEvacuado) aliadosEvacuados = sumarSaturado(aliadosEvacuados, 1);
        if (evento instanceof IncendioExtinguido) incendiosApagados = sumarSaturado(incendiosApagados, 1);
        if (evento instanceof ObjetoUsado) objetosUsados = sumarSaturado(objetosUsados, 1);
        if (evento instanceof TrampaDesactivada) trampasDesactivadas = sumarSaturado(trampasDesactivadas, 1);
        if (evento instanceof CeldaInspeccionada) celdasInspeccionadas = sumarSaturado(celdasInspeccionadas, 1);
        if (evento instanceof PersonajeDanado dano) {
            if (juego == null) {
                danoCausado = sumarSaturado(danoCausado, dano.cantidad());
            } else if (dano.personaje().equals(juego.getJugador().getNombre())) {
                danoRecibido = sumarSaturado(danoRecibido, dano.cantidad());
            } else if (juego.getJugador().getNombre().equals(
                    ultimoAtacante.get(dano.personaje()))) {
                danoCausado += dano.cantidad();
            }
        }
    }

    private boolean esAtaqueDelJugador(PersonajeAtacado ataque) {
        return juego == null || ataque.atacante().equals(juego.getJugador().getNombre());
    }

    /** Registra dano recibido por el jugador cuando el adaptador conoce su identidad. */
    public void registrarDanoRecibido(int cantidad) {
        danoRecibido = sumarSaturado(danoRecibido, cantidad);
    }
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
    /**
     * Valida el snapshot completo antes de sustituir cualquier contador.
     * No deja estadisticas parcialmente restauradas ante un dato corrupto.
     */
    public void restaurar(Snapshot estado) {
        if (estado == null) return;
        int causado = noNegativo(estado.danoCausado());
        int recibido = noNegativo(estado.danoRecibido());
        int totalMuertes = noNegativo(estado.muertes());
        int eliminados = noNegativo(estado.enemigosEliminados());
        int evacuados = noNegativo(estado.aliadosEvacuados());
        int incendios = noNegativo(estado.incendiosApagados());
        int usados = noNegativo(estado.objetosUsados());
        int totalPasos = noNegativo(estado.pasos());
        int totalDisparos = noNegativo(estado.disparos());
        int trampas = noNegativo(estado.trampasDesactivadas());
        int inspeccionadas = noNegativo(estado.celdasInspeccionadas());

        danoCausado = causado;
        danoRecibido = recibido;
        muertes = totalMuertes;
        enemigosEliminados = eliminados;
        aliadosEvacuados = evacuados;
        incendiosApagados = incendios;
        objetosUsados = usados;
        pasos = totalPasos;
        disparos = totalDisparos;
        trampasDesactivadas = trampas;
        celdasInspeccionadas = inspeccionadas;
    }

    /** La aritmetica saturada evita que un contador vuelva a ser negativo. */
    private static int sumarSaturado(int actual, int cantidad) {
        if (cantidad <= 0) return actual;
        return (int) Math.min(Integer.MAX_VALUE, (long) actual + cantidad);
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
