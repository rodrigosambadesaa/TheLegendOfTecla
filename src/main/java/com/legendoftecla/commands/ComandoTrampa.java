package com.legendoftecla.commands;
import com.legendoftecla.events.TrampaDesactivada;
import com.legendoftecla.events.TrampaDetectada;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.engine.SistemaTrampas;
import com.legendoftecla.model.elements.Trampa;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Posicion;
/** Detecta o desactiva una trampa en la celda actual. */
public final class ComandoTrampa implements Comando {
    /** Interacciones admitidas por el comando. */
    public enum Operacion { DETECTAR, DESACTIVAR, DETONAR, DISPARAR }
    private final CommandContext contexto;
    private final Operacion operacion;
    public ComandoTrampa(CommandContext contexto, Operacion operacion) {
        this.contexto = java.util.Objects.requireNonNull(contexto, "Contexto");
        this.operacion = java.util.Objects.requireNonNull(operacion, "Operacion");
    }
    public void ejecutar() throws ComandoException {
        var jugador = contexto.getJuego().getJugador();
        TrampaLocalizada localizada = buscar(jugador.getPosicion());
        Trampa trampa = localizada.trampa();
        boolean exito = switch (operacion) {
            case DETECTAR -> trampa.detectar(jugador, jugador.getVisionBase());
            case DESACTIVAR -> trampa.desactivar(jugador, jugador.getVisionBase());
            case DETONAR -> SistemaTrampas.detonar(
                    contexto.getJuego(), localizada.posicion(), trampa);
            case DISPARAR -> SistemaTrampas.disparar(
                    contexto.getJuego(), localizada.posicion(), trampa);
        };
        if (!exito) {
            throw new ComandoException(mensajeError());
        }
        var ahora = contexto.getJuego().getBusEventos().ahora();
        if (operacion == Operacion.DESACTIVAR) {
            contexto.getJuego().publicarEvento(new TrampaDesactivada(
                    ahora, trampa.getId(), jugador.getNombre()));
        } else if (operacion == Operacion.DETECTAR) {
            contexto.getJuego().publicarEvento(new TrampaDetectada(
                    ahora, trampa.getId(), jugador.getNombre()));
        }
        contexto.getJuego().getConsola().imprimirExito(
                "Trampa " + trampa.getId() + ": " + operacion.name().toLowerCase() + ".");
    }

    private TrampaLocalizada buscar(Posicion origen) throws ComandoException {
        java.util.List<Posicion> posiciones = new java.util.ArrayList<>();
        posiciones.add(origen);
        for (Direccion direccion : Direccion.values()) {
            posiciones.add(origen.mover(direccion));
        }
        for (Posicion posicion : posiciones) {
            java.util.List<Trampa> trampas = SistemaTrampas.trampasEn(
                    contexto.getJuego(), posicion);
            if (!trampas.isEmpty()) {
                return new TrampaLocalizada(posicion, trampas.get(0));
            }
        }
        throw new ComandoException("No hay una trampa cercana.");
    }

    private String mensajeError() {
        return switch (operacion) {
            case DETECTAR -> "No detectas ninguna trampa.";
            case DESACTIVAR -> "No se pudo desactivar la trampa.";
            case DETONAR -> "La trampa no admite detonacion remota.";
            case DISPARAR -> "La trampa ya no esta activa.";
        };
    }

    private record TrampaLocalizada(Posicion posicion, Trampa trampa) { }
}
