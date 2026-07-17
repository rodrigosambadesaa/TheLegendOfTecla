package com.legendoftecla.commands;

import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Representa la entidad ComandoAtacar del juego.
 */
public class ComandoAtacar implements Comando {
    private static final Pattern PATRON_ALCANCE = Pattern.compile("^(\\d+)([nseoNSEO])$");

    private final CommandContext context;
    private final String alcance;
    private final String nombreObjetivo;

    /**
     * Ejecuta ComandoAtacar.
     */
    public ComandoAtacar(CommandContext context, String alcance, String nombreObjetivo) {
        this.context = context;
        this.alcance = alcance;
        this.nombreObjetivo = nombreObjetivo;
    }

    @Override
    /**
     * Ejecuta ejecutar.
     */
    public void ejecutar() throws ComandoException {
        Posicion origen = context.getJuego().getJugador().getPosicion();
        Posicion destino = resolverDestino(origen);
        Mapa mapa = context.getJuego().getMapa();
        if (!mapa.hayLineaAtaque(origen, destino)) {
            throw new ComandoException("Ataque bloqueado: hay celdas no transitables en la trayectoria.");
        }
        Celda celda = mapa.getCelda(destino);
        List<Enemigo> enemigos = celda.getEnemigos();
        if (enemigos.isEmpty()) {
            throw new ComandoException("No hay enemigos en la celda objetivo.");
        }
        if (debeAtacarATodos()) {
            context.getJuego().getJugador().atacar(enemigos);
            context.getJuego().getConsola()
                    .imprimir("Atacas a todos los enemigos de la celda objetivo " + destino + ".");
        } else {
            Enemigo objetivo = enemigos.stream().filter(e -> e.getNombre().equalsIgnoreCase(nombreObjetivo)).findFirst()
                    .orElse(null);
            if (objetivo == null) {
                throw new ComandoException("No existe ese enemigo en la celda.");
            }
            context.getJuego().getJugador().atacar(objetivo);
            context.getJuego().getConsola().imprimir("Atacas a " + objetivo.getNombre() + " en " + destino + ".");
        }
        celda.getEnemigos().stream().filter(e -> e.getSalud() <= 0).toList().forEach(e -> {
            celda.quitarEnemigo(e);
            e.getMochila().getObjetos().forEach(celda::agregarObjeto);
        });
    }

    private boolean debeAtacarATodos() {
        if (nombreObjetivo == null || nombreObjetivo.isBlank()) {
            return true;
        }
        String objetivo = nombreObjetivo.trim();
        return objetivo.equalsIgnoreCase("todos") || objetivo.equalsIgnoreCase("todas");
    }

    private Posicion resolverDestino(Posicion origen) throws ComandoException {
        if (alcance == null || alcance.isBlank()) {
            return origen;
        }
        Matcher m = PATRON_ALCANCE.matcher(alcance.trim());
        if (!m.matches()) {
            throw new ComandoException("Alcance invalido. Usa formato como 3e, 2n, 4s, 1o.");
        }
        int pasos = Integer.parseInt(m.group(1));
        Direccion direccion = Direccion.desdeTexto(m.group(2));
        Posicion actual = origen;
        for (int i = 0; i < pasos; i++) {
            actual = actual.mover(direccion);
            if (!context.getJuego().getMapa().estaDentro(actual)) {
                throw new ComandoException("El objetivo queda fuera del mapa.");
            }
        }
        return actual;
    }
}
