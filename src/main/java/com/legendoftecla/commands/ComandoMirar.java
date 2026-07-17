package com.legendoftecla.commands;

import com.legendoftecla.console.ArteEnemigoLore;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;

import java.util.stream.Collectors;

/**
 * Representa la entidad ComandoMirar del juego.
 */
public class ComandoMirar implements Comando {
    private final CommandContext context;
    private final Direccion direccion;
    private final int pasos;

    /**
     * Ejecuta ComandoMirar.
      * @param context valor de {@code context}
     */
    public ComandoMirar(CommandContext context) {
        this(context, null, 0);
    }

    /**
     * Ejecuta ComandoMirar.
      * @param context valor de {@code context}
      * @param direccion valor de {@code direccion}
      * @param pasos valor de {@code pasos}
     */
    public ComandoMirar(CommandContext context, Direccion direccion, int pasos) {
        this.context = context;
        this.direccion = direccion;
        this.pasos = pasos;
    }

    @Override
    /**
     * Ejecuta ejecutar.
     */
    public void ejecutar() throws ComandoException {
        Celda celda = resolverCeldaAMirar();
        context.getJuego().getConsola().imprimir(celda.getDescripcion());
        if (celda.getObjetos().isEmpty()) {
            context.getJuego().getConsola().imprimir("No hay objetos en esta celda.");
        } else {
            String lista = celda.getObjetos().stream().map(o -> o.getNombre()).collect(Collectors.joining(", "));
            context.getJuego().getConsola().imprimir("Objetos: " + lista);
        }
        if (!celda.getEnemigos().isEmpty()) {
            String enemigos = celda.getEnemigos().stream().map(e -> e.getNombre()).collect(Collectors.joining(", "));
            context.getJuego().getConsola().imprimir("Enemigos aqui: " + enemigos);
            for (var enemigo : celda.getEnemigos()) {
                context.getJuego().getConsola().imprimirInfo(ArteEnemigoLore.renderizarFicha(enemigo));
            }
        }
    }

    private Celda resolverCeldaAMirar() throws ComandoException {
        Mapa mapa = context.getJuego().getMapa();
        Posicion origen = context.getJuego().getJugador().getPosicion();
        if (direccion == null) {
            return mapa.getCelda(origen);
        }

        Posicion destino = origen;
        for (int i = 0; i < pasos; i++) {
            destino = destino.mover(direccion);
            if (!mapa.estaDentro(destino)) {
                throw new ComandoException("No puedes mirar fuera del mapa.");
            }
        }

        if (!mapa.esTransitable(destino)) {
            throw new ComandoException("No puedes mirar esa celda: destino no transitable.");
        }

        return mapa.getCelda(destino);
    }
}
