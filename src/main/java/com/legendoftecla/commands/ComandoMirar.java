package com.legendoftecla.commands;

import com.legendoftecla.console.ArteEnemigoLore;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;

import java.util.stream.Collectors;

/**
 * Representa la entidad ComandoMirar del juego.
 */
public class ComandoMirar implements Comando {
    private CommandContext context;
    private Direccion direccion;
    private int pasos;

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
        setContext(context);
        setDireccion(direccion);
        setPasos(pasos);
    }

    /** @return contexto de ejecucion */
    public CommandContext getContext() { return context; }
    /** @param context contexto no nulo */
    public void setContext(CommandContext context) { this.context = Validaciones.noNulo(context, "Contexto"); }
    /** @return direccion observada o {@code null} */
    public Direccion getDireccion() { return direccion; }
    /** @param direccion direccion opcional coherente con los pasos actuales */
    public void setDireccion(Direccion direccion) {
        if (direccion == null && pasos != 0) {
            throw new IllegalArgumentException("Mirar sin direccion no admite pasos.");
        }
        this.direccion = direccion;
    }
    /** @return distancia de observacion */
    public int getPasos() { return pasos; }
    /** @param pasos cero sin direccion o entre 1 y el limite del mapa */
    public void setPasos(int pasos) {
        if (direccion == null) {
            if (pasos != 0) {
                throw new IllegalArgumentException("Mirar sin direccion no admite pasos.");
            }
            this.pasos = 0;
            return;
        }
        this.pasos = Validaciones.enteroEntre(pasos, 1, Limites.MAPA_MAXIMO, "Pasos de vision");
    }

    @Override
    /**
     * Ejecuta ejecutar.
     */
    public void ejecutar() throws ComandoException {
        Celda celda = resolverCeldaAMirar();
        context.getJuego().getConsola().imprimir(celda.getDescripcion());
        if (direccion == null) {
            context.getJuego().inspeccionarCeldaActual();
            if (celda.getObjetos().isEmpty()) {
                context.getJuego().getConsola().imprimir("No hay objetos en esta celda.");
            } else {
                String lista = celda.getObjetos().stream()
                        .map(o -> o.getNombre()).collect(Collectors.joining(", "));
                context.getJuego().getConsola().imprimir("Objetos: " + lista);
            }
        } else {
            context.getJuego().getConsola().imprimir(
                    "Los objetos solo pueden inspeccionarse al llegar a la celda y mirar alli.");
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
