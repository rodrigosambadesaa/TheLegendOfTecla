package com.legendoftecla.stats;
/** Totales opcionales persistibles entre partidas. */
public final class EstadisticasGlobales {
    private int partidas;
    private int victorias;
    private int derrotas;
    private int turnos;
    private int muertes;
    private int enemigosEliminados;
    private int aliadosEvacuados;
    private int danoCausado;
    private int danoRecibido;
    private int incendiosApagados;
    private int objetosUsados;
    private int pasos;
    public void registrarPartida(boolean victoria, int turnos) {
        registrarPartida(victoria, turnos, null);
    }
    public void registrarPartida(boolean victoria, int turnos,
            EstadisticasPartida.Snapshot detalle) {
        partidas++;
        if (victoria) victorias++; else derrotas++;
        this.turnos += Math.max(0, turnos);
        if (detalle != null) {
            muertes += detalle.muertes();
            enemigosEliminados += detalle.enemigosEliminados();
            aliadosEvacuados += detalle.aliadosEvacuados();
            danoCausado += detalle.danoCausado();
            danoRecibido += detalle.danoRecibido();
            incendiosApagados += detalle.incendiosApagados();
            objetosUsados += detalle.objetosUsados();
            pasos += detalle.pasos();
        }
    }
    public int getPartidas() { return partidas; }
    public int getVictorias() { return victorias; }
    public int getDerrotas() { return derrotas; }
    public int getTurnos() { return turnos; }
    public int getMuertes() { return muertes; }
    public int getEnemigosEliminados() { return enemigosEliminados; }
    public int getAliadosEvacuados() { return aliadosEvacuados; }
    public int getDanoCausado() { return danoCausado; }
    public int getDanoRecibido() { return danoRecibido; }
    public int getIncendiosApagados() { return incendiosApagados; }
    public int getObjetosUsados() { return objetosUsados; }
    public int getPasos() { return pasos; }
    public Snapshot snapshot() {
        return new Snapshot(partidas, victorias, derrotas, turnos, muertes,
                enemigosEliminados, aliadosEvacuados, danoCausado, danoRecibido,
                incendiosApagados, objetosUsados, pasos);
    }
    public void restaurar(Snapshot e) {
        if (e == null || java.util.stream.IntStream.of(e.partidas(), e.victorias(),
                e.derrotas(), e.turnos(), e.muertes(), e.enemigosEliminados(),
                e.aliadosEvacuados(), e.danoCausado(), e.danoRecibido(),
                e.incendiosApagados(), e.objetosUsados(), e.pasos()).anyMatch(v -> v < 0)) {
            throw new IllegalArgumentException("Estadisticas globales invalidas");
        }
        partidas = e.partidas(); victorias = e.victorias(); derrotas = e.derrotas();
        turnos = e.turnos(); muertes = e.muertes();
        enemigosEliminados = e.enemigosEliminados();
        aliadosEvacuados = e.aliadosEvacuados(); danoCausado = e.danoCausado();
        danoRecibido = e.danoRecibido(); incendiosApagados = e.incendiosApagados();
        objetosUsados = e.objetosUsados(); pasos = e.pasos();
    }
    public record Snapshot(int partidas, int victorias, int derrotas, int turnos,
            int muertes, int enemigosEliminados, int aliadosEvacuados,
            int danoCausado, int danoRecibido, int incendiosApagados,
            int objetosUsados, int pasos) { }
}
