package com.legendoftecla.commands;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.exceptions.JuegoException;
import com.legendoftecla.persistence.PersistenciaPartida;
import java.nio.file.Path;
/** Guarda una partida completa versionada. */
public final class ComandoGuardarPartida implements Comando {
    private final CommandContext contexto; private final String archivo;
    public ComandoGuardarPartida(CommandContext contexto, String archivo) {
        this.contexto = contexto; this.archivo = archivo;
    }
    public void ejecutar() throws ComandoException {
        try { PersistenciaPartida.guardar(contexto.getJuego(), Path.of(archivo), 1L); }
        catch (JuegoException | RuntimeException e) { throw new ComandoException(e.getMessage()); }
        contexto.getJuego().getConsola().imprimirExito("Partida guardada en " + archivo + ".");
    }
}
