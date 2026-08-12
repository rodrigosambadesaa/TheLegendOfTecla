package com.legendoftecla.commands;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.exceptions.JuegoException;
import com.legendoftecla.persistence.PersistenciaPartida;
import java.nio.file.Path;
/** Reemplaza el juego del contexto por un savegame. */
public final class ComandoCargarPartida implements Comando {
    private final CommandContext contexto; private final String archivo;
    public ComandoCargarPartida(CommandContext contexto, String archivo) {
        this.contexto = contexto; this.archivo = archivo;
    }
    public void ejecutar() throws ComandoException {
        try { contexto.setJuego(PersistenciaPartida.cargar(
                Path.of(archivo), contexto.getJuego().getConsola())); }
        catch (JuegoException | RuntimeException e) { throw new ComandoException(e.getMessage()); }
    }
}
