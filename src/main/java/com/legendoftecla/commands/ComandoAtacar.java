package com.legendoftecla.commands;

import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.engine.SistemaCombate;
import com.legendoftecla.engine.ServicioBotinEnemigo;
import com.legendoftecla.engine.SistemaIluminacion;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Representa la entidad ComandoAtacar del juego.
 */
public class ComandoAtacar implements Comando {
    private static final Pattern PATRON_ALCANCE = Pattern.compile("^(\\d+)([nseoNSEO])$");

    private CommandContext context;
    private String alcance;
    private String nombreObjetivo;

    /**
     * Ejecuta ComandoAtacar.
      * @param alcance valor de {@code alcance}
      * @param context valor de {@code context}
      * @param nombreObjetivo valor de {@code nombreObjetivo}
     */
    public ComandoAtacar(CommandContext context, String alcance, String nombreObjetivo) {
        setContext(context);
        setAlcance(alcance);
        setNombreObjetivo(nombreObjetivo);
    }

    /** @return contexto de ejecucion */
    public CommandContext getContext() { return context; }
    /** @param context contexto no nulo */
    public void setContext(CommandContext context) { this.context = Validaciones.noNulo(context, "Contexto"); }
    /** @return alcance opcional */
    public String getAlcance() { return alcance; }
    /** @param alcance alcance opcional con formato como {@code 3e} */
    public void setAlcance(String alcance) {
        if (alcance != null && !alcance.isBlank() && !PATRON_ALCANCE.matcher(alcance.trim()).matches()) {
            throw new IllegalArgumentException("El alcance debe usar un formato como 3e.");
        }
        this.alcance = alcance == null ? null
                : Validaciones.texto(alcance.trim(), "Alcance", Limites.TEXTO_CORTO);
    }
    /** @return nombre opcional del objetivo */
    public String getNombreObjetivo() { return nombreObjetivo; }
    /** @param nombreObjetivo nombre opcional y acotado */
    public void setNombreObjetivo(String nombreObjetivo) {
        this.nombreObjetivo = nombreObjetivo == null ? null
                : Validaciones.texto(nombreObjetivo.trim(), "Nombre del objetivo", Limites.TEXTO_CORTO);
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
        if (!SistemaIluminacion.hayLuz(context.getJuego(), destino)) {
            throw new ComandoException("La celda objetivo esta oscura; necesitas iluminarla antes de atacar.");
        }
        List<Enemigo> enemigos = celda.getEnemigos().stream()
                .filter(enemigo -> enemigo.getSalud() > 0)
                .toList();
        if (enemigos.isEmpty()) {
            throw new ComandoException("No hay enemigos en la celda objetivo.");
        }
        if (!debeAtacarATodos() && enemigos.stream()
                .noneMatch(enemigo -> enemigo.getNombre().equalsIgnoreCase(nombreObjetivo))) {
            throw new ComandoException("No existe ese enemigo en la celda.");
        }
        SistemaCombate.atacarTodos(context.getJuego(), context.getJuego().getJugador(),
                enemigos, ThreadLocalRandom.current());
        context.getJuego().getConsola().imprimir("Atacas a todos los enemigos de la celda objetivo "
                + destino + " (" + enemigos.size() + " objetivo(s)).");
        celda.getEnemigos().stream().filter(e -> e.getSalud() <= 0).toList().forEach(e -> {
            celda.quitarEnemigo(e);
            ServicioBotinEnemigo.soltar(celda, e);
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
