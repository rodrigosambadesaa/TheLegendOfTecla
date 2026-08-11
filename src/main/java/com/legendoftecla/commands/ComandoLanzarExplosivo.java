package com.legendoftecla.commands;

import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Zapador;
import com.legendoftecla.model.items.Explosivo;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;
import com.legendoftecla.audio.EventoSonido;
import com.legendoftecla.audio.GestorSonido;
import com.legendoftecla.engine.SistemaIncendios;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.ThreadLocalRandom;

/** Lanza y consume un explosivo contra todos los enemigos de una celda. */
public final class ComandoLanzarExplosivo implements Comando {
    private static final Pattern PATRON_ALCANCE = Pattern.compile("^(\\d+)([nseoNSEO])$");

    private CommandContext context;
    private String alcance;
    private String nombreExplosivo;

    /**
     * Crea el comando de lanzamiento.
     *
     * @param context contexto de la partida
     * @param alcance distancia y direccion, por ejemplo {@code 3e}
     * @param nombreExplosivo nombre del explosivo de la mochila
     */
    public ComandoLanzarExplosivo(CommandContext context, String alcance, String nombreExplosivo) {
        setContext(context);
        setAlcance(alcance);
        setNombreExplosivo(nombreExplosivo);
    }

    /** @return contexto de ejecucion */
    public CommandContext getContext() { return context; }
    /** @param context contexto no nulo */
    public void setContext(CommandContext context) { this.context = Validaciones.noNulo(context, "Contexto"); }
    /** @return alcance de lanzamiento */
    public String getAlcance() { return alcance; }
    /** @param alcance distancia y direccion con formato como {@code 3e} */
    public void setAlcance(String alcance) {
        String alcanceValidado = Validaciones.textoObligatorio(
                alcance, "Alcance", Limites.TEXTO_CORTO);
        if (!PATRON_ALCANCE.matcher(alcanceValidado).matches()) {
            throw new IllegalArgumentException("El alcance debe usar un formato como 3e.");
        }
        this.alcance = alcanceValidado;
    }
    /** @return nombre del explosivo */
    public String getNombreExplosivo() { return nombreExplosivo; }
    /** @param nombreExplosivo nombre obligatorio y acotado */
    public void setNombreExplosivo(String nombreExplosivo) {
        this.nombreExplosivo = Validaciones.textoObligatorio(
                nombreExplosivo, "Nombre del explosivo", Limites.TEXTO_CORTO);
    }

    @Override
    public void ejecutar() throws ComandoException {
        if (!(context.getJuego().getJugador() instanceof Zapador)) {
            throw new ComandoException("Solo el zapador puede lanzar explosivos.");
        }

        Explosivo explosivo = buscarExplosivo();
        Posicion origen = context.getJuego().getJugador().getPosicion();
        Posicion destino = resolverDestino(origen, explosivo.getAlcanceMaximo());
        Mapa mapa = context.getJuego().getMapa();
        if (!mapa.hayLineaAtaque(origen, destino)) {
            throw new ComandoException("Lanzamiento bloqueado: hay un muro en la trayectoria.");
        }

        Celda celda = mapa.getCelda(destino);
        List<Enemigo> objetivos = List.copyOf(celda.getEnemigos());
        if (objetivos.isEmpty()) {
            throw new ComandoException("No hay enemigos en la celda objetivo.");
        }

        context.getJuego().getJugador().getMochila().quitarPorNombre(explosivo.getNombre());
        List<Integer> vidasAntes = objetivos.stream().map(Enemigo::getSalud).toList();
        GestorSonido.reproducir(EventoSonido.ATAQUE, origen, origen);
        objetivos.forEach(enemigo -> enemigo.recibirDanio(explosivo.getDanio()));
        context.getJuego().getConsola().imprimir("Lanzas " + explosivo.getNombre() + " a " + destino
                + " y causas " + explosivo.getDanio() + " de dano a " + objetivos.size() + " enemigo(s).");
        for (int i = 0; i < objetivos.size(); i++) {
            Enemigo enemigo = objetivos.get(i);
            int quitada = vidasAntes.get(i) - enemigo.getSalud();
            context.getJuego().getConsola().imprimir(context.getJuego().getJugador().getNombre()
                    + " ataca a " + enemigo.getNombre() + " con " + explosivo.getNombre()
                    + ": quita " + quitada + " de vida; quedan " + enemigo.getSalud()
                    + "/" + enemigo.getSaludMaxima() + ".");
            GestorSonido.reproducir(enemigo.getSalud() <= 0
                    ? EventoSonido.MUERTE_ENEMIGO : EventoSonido.DANIO, destino, origen);
        }
        SistemaIncendios.intentarDerribarAntorcha(
                context.getJuego(), destino, ThreadLocalRandom.current());

        objetivos.stream().filter(enemigo -> enemigo.getSalud() <= 0).forEach(enemigo -> {
            celda.quitarEnemigo(enemigo);
            enemigo.getMochila().getObjetos().forEach(celda::agregarObjeto);
        });
    }

    private Explosivo buscarExplosivo() throws ComandoException {
        Objeto objeto = context.getJuego().getJugador().getMochila().getObjetos().stream()
                .filter(candidato -> candidato.getNombre().equalsIgnoreCase(nombreExplosivo))
                .findFirst()
                .orElse(null);
        if (!(objeto instanceof Explosivo explosivo)) {
            throw new ComandoException("No tienes ese explosivo en la mochila.");
        }
        return explosivo;
    }

    private Posicion resolverDestino(Posicion origen, int alcanceMaximo) throws ComandoException {
        Matcher matcher = PATRON_ALCANCE.matcher(alcance == null ? "" : alcance.trim());
        if (!matcher.matches()) {
            throw new ComandoException("Alcance invalido. Usa un valor como 3e, 2n, 4s o 1o.");
        }
        int pasos = Integer.parseInt(matcher.group(1));
        if (pasos < 1 || pasos > alcanceMaximo) {
            throw new ComandoException("El explosivo permite lanzar entre 1 y " + alcanceMaximo + " celdas.");
        }
        Direccion direccion = Direccion.desdeTexto(matcher.group(2));
        Posicion destino = origen;
        for (int paso = 0; paso < pasos; paso++) {
            destino = destino.mover(direccion);
            if (!context.getJuego().getMapa().estaDentro(destino)) {
                throw new ComandoException("El lanzamiento queda fuera del mapa.");
            }
        }
        return destino;
    }
}
