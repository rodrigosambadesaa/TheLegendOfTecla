package com.legendoftecla.commands;
import com.legendoftecla.events.PuertaAbierta;
import com.legendoftecla.events.PuertaCerrada;
import com.legendoftecla.events.RuidoGenerado;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.elements.Puerta;
import com.legendoftecla.model.elements.EstadoPuerta;
import com.legendoftecla.model.items.Credencial;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Posicion;
/** Abre o cierra una puerta en la celda actual o adyacente. */
public final class ComandoPuerta implements Comando {
    private final CommandContext contexto;
    private final boolean abrir;
    public ComandoPuerta(CommandContext contexto, boolean abrir) { this.contexto = contexto; this.abrir = abrir; }
    public void ejecutar() throws ComandoException {
        PuertaLocalizada seleccion = buscar();
        Puerta puerta = seleccion.puerta();
        // Abrir una puerta ya abierta o destruida no cambia su estado:
        // no publicar falsas aperturas ni ruidos para la IA.
        if (abrir && (puerta.estaDestruido()
                || puerta.getEstado() == EstadoPuerta.ABIERTA)) {
            contexto.getJuego().getConsola().imprimirInfo(
                    "La puerta " + puerta.getId() + " ya permite el paso.");
            return;
        }
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
                seleccion.posicion(), abrir ? 3 : 4, abrir ? "abrir-puerta" : "cerrar-puerta"));
        contexto.getJuego().getConsola().imprimirExito(
                "Puerta " + puerta.getId() + (abrir ? " abierta." : " cerrada."));
    }
    /** Se conserva la posicion exacta encontrada; los IDs son locales a cada celda. */
    private record PuertaLocalizada(Puerta puerta, Posicion posicion) { }

    private PuertaLocalizada buscar() throws ComandoException {
        Posicion origen = contexto.getJuego().getJugador().getPosicion();
        PuertaLocalizada actual = en(origen);
        if (actual != null) return actual;
        for (Direccion direccion : Direccion.values()) {
            PuertaLocalizada cercana = en(origen.mover(direccion));
            if (cercana != null) return cercana;
        }
        throw new ComandoException("No hay una puerta cercana.");
    }

    private PuertaLocalizada en(Posicion posicion) {
        if (!contexto.getJuego().getMapa().estaDentro(posicion)) return null;
        return contexto.getJuego().getMapa().getCelda(posicion).getElementos().stream()
                .filter(Puerta.class::isInstance).map(Puerta.class::cast)
                .map(puerta -> new PuertaLocalizada(puerta, posicion))
                .findFirst().orElse(null);
    }
}
