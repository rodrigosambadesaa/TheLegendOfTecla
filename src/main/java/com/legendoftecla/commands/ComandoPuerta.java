package com.legendoftecla.commands;
import com.legendoftecla.events.PuertaAbierta;
import com.legendoftecla.events.PuertaCerrada;
import com.legendoftecla.events.RuidoGenerado;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.elements.Puerta;
import com.legendoftecla.model.items.Credencial;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Posicion;
/** Abre o cierra una puerta en la celda actual o adyacente. */
public final class ComandoPuerta implements Comando {
    private final CommandContext contexto;
    private final boolean abrir;
    public ComandoPuerta(CommandContext contexto, boolean abrir) { this.contexto = contexto; this.abrir = abrir; }
    public void ejecutar() throws ComandoException {
        Puerta puerta = buscar();
        boolean exito;
        if (abrir) {
            String codigo = contexto.getJuego().getJugador().getMochila().getObjetos().stream()
                    .filter(Credencial.class::isInstance).map(Credencial.class::cast)
                    .map(Credencial::getCodigo).filter(c -> c.equals(puerta.getCredencial()))
                    .findFirst().orElse(null);
            exito = puerta.abrir(codigo);
        } else exito = puerta.cerrar();
        if (!exito) throw new ComandoException("La puerta no admite esa accion o falta la credencial.");
        var ahora = contexto.getJuego().getBusEventos().ahora();
        if (abrir) contexto.getJuego().publicarEvento(new PuertaAbierta(ahora, puerta.getId()));
        else contexto.getJuego().publicarEvento(new PuertaCerrada(ahora, puerta.getId()));
        contexto.getJuego().publicarEvento(new RuidoGenerado(ahora,
                posicion(puerta), abrir ? 3 : 4, abrir ? "abrir-puerta" : "cerrar-puerta"));
        contexto.getJuego().getConsola().imprimirExito(
                "Puerta " + puerta.getId() + (abrir ? " abierta." : " cerrada."));
    }
    private Puerta buscar() throws ComandoException {
        Posicion origen = contexto.getJuego().getJugador().getPosicion();
        Puerta actual = en(origen); if (actual != null) return actual;
        for (Direccion d : Direccion.values()) { Puerta p = en(origen.mover(d)); if (p != null) return p; }
        throw new ComandoException("No hay una puerta cercana.");
    }
    private Puerta en(Posicion posicion) {
        if (!contexto.getJuego().getMapa().estaDentro(posicion)) return null;
        return contexto.getJuego().getMapa().getCelda(posicion).getElementos().stream()
                .filter(Puerta.class::isInstance).map(Puerta.class::cast).findFirst().orElse(null);
    }
    private Posicion posicion(Puerta puerta) {
        for (int fila = 0; fila < contexto.getJuego().getMapa().getFilas(); fila++) {
            for (int columna = 0; columna < contexto.getJuego().getMapa().getColumnas(); columna++) {
                Posicion posicion = new Posicion(fila, columna);
                if (contexto.getJuego().getMapa().getCelda(posicion).getElementos().contains(puerta)) {
                    return posicion;
                }
            }
        }
        return contexto.getJuego().getJugador().getPosicion();
    }
}
