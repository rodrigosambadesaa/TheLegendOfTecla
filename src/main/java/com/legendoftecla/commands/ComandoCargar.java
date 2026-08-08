package com.legendoftecla.commands;

import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.exceptions.JuegoException;
import com.legendoftecla.loader.CargadorJuegoDeFicheros;
import com.legendoftecla.model.characters.Francotirador;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.characters.Zapador;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;

import java.nio.file.Path;

/** Carga una partida desde escenario JSON o desde los tres ficheros de la P2. */
public final class ComandoCargar implements Comando {
    private CommandContext context;
    private String directorio;

    /**
     * @param context contexto que recibirá la partida cargada
     * @param directorio ruta del directorio de datos
     */
    public ComandoCargar(CommandContext context, String directorio) {
        setContext(context);
        setDirectorio(directorio);
    }

    /** @return contexto */
    public CommandContext getContext() { return context; }
    /** @param context contexto no nulo */
    public void setContext(CommandContext context) {
        this.context = Validaciones.noNulo(context, "Contexto");
    }
    /** @return ruta textual */
    public String getDirectorio() { return directorio; }
    /** @param directorio ruta obligatoria */
    public void setDirectorio(String directorio) {
        this.directorio = Validaciones.textoObligatorio(
                directorio, "Directorio de carga", Limites.DESCRIPCION);
    }

    @Override
    public void ejecutar() throws ComandoException {
        Juego anterior = context.getJuego();
        Personaje jugador = anterior.getJugador();
        String clase = jugador instanceof Francotirador ? "francotirador"
                : jugador instanceof Zapador ? "zapador" : "marine";
        try {
            Juego cargado = new CargadorJuegoDeFicheros(
                    anterior.getConsola(), jugador.getNombre(), clase, Path.of(directorio),
                    Dificultad.NORMAL, null, !anterior.getAliadosRegistrados().isEmpty()).cargarJuego();
            cargado.setCondicionVictoria(anterior.getCondicionVictoria());
            context.setJuego(cargado);
            cargado.getConsola().imprimirExito(
                    "Partida cargada desde " + Path.of(directorio).normalize() + ".");
        } catch (JuegoException | RuntimeException e) {
            throw new ComandoException("No se pudo cargar la partida: " + e.getMessage());
        }
    }
}
