package com.legendoftecla.commands;

import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.inventory.ServicioIntercambio;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.events.ObjetoRecogido;
import com.legendoftecla.events.ObjetoTirado;

/** Ejecuta dar, pedir o intercambio atomico con un aliado cercano. */
public final class ComandoTransferir implements Comando {
    public enum Operacion { DAR, PEDIR, INTERCAMBIAR }
    private final CommandContext contexto;
    private final Operacion operacion;
    private final String objeto;
    private final String segundoObjeto;
    private final String aliado;
    public ComandoTransferir(CommandContext contexto, Operacion operacion,
            String objeto, String segundoObjeto, String aliado) {
        this.contexto = contexto; this.operacion = operacion; this.objeto = objeto;
        this.segundoObjeto = segundoObjeto; this.aliado = aliado;
    }
    @Override public void ejecutar() throws ComandoException {
        Aliado receptor = contexto.getJuego().getAliados().stream()
                .filter(a -> a.getNombre().equalsIgnoreCase(aliado)).findFirst()
                .orElseThrow(() -> new ComandoException("Aliado activo no encontrado: " + aliado));
        ServicioIntercambio servicio = new ServicioIntercambio(1);
        boolean enCombate = enCombate(receptor);
        try {
            switch (operacion) {
                case DAR -> {
                    servicio.dar(contexto.getJuego().getJugador(), receptor, objeto, enCombate);
                    publicarTransferencia(contexto.getJuego().getJugador(), receptor, objeto);
                }
                case PEDIR -> {
                    servicio.pedir(contexto.getJuego().getJugador(), receptor, objeto, enCombate);
                    publicarTransferencia(receptor, contexto.getJuego().getJugador(), objeto);
                }
                case INTERCAMBIAR -> {
                    servicio.intercambiar(contexto.getJuego().getJugador(), objeto,
                            receptor, segundoObjeto, enCombate);
                    publicarTransferencia(contexto.getJuego().getJugador(), receptor, objeto);
                    publicarTransferencia(receptor, contexto.getJuego().getJugador(), segundoObjeto);
                }
            }
            contexto.getJuego().getConsola().imprimirExito("Intercambio completado con " + aliado + ".");
        } catch (AccionInvalidaException error) {
            throw new ComandoException(error.getMessage());
        }
    }

    private boolean enCombate(Aliado aliadoObjetivo) {
        Personaje jugador = contexto.getJuego().getJugador();
        return contexto.getJuego().getEnemigos().stream()
                .filter(enemigo -> enemigo.getSalud() > 0)
                .anyMatch(enemigo -> amenaza(enemigo, jugador)
                        || amenaza(enemigo, aliadoObjetivo));
    }

    private boolean amenaza(Personaje enemigo, Personaje objetivo) {
        return enemigo.getPosicion().distanciaManhattan(objetivo.getPosicion())
                <= enemigo.getRangoVision()
                && contexto.getJuego().getMapa().hayLineaAtaque(
                        enemigo.getPosicion(), objetivo.getPosicion());
    }

    private void publicarTransferencia(Personaje origen, Personaje destino,
            String nombreObjeto) {
        var ahora = contexto.getJuego().getBusEventos().ahora();
        contexto.getJuego().publicarEvento(new ObjetoTirado(
                ahora, origen.getNombre(), nombreObjeto, origen.getPosicion()));
        contexto.getJuego().publicarEvento(new ObjetoRecogido(
                ahora, destino.getNombre(), nombreObjeto, destino.getPosicion()));
    }
}
