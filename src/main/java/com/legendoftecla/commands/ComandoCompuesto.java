package com.legendoftecla.commands;

import com.legendoftecla.exceptions.ComandoException;

import java.util.ArrayList;
import java.util.List;


/**
 * Representa la entidad ComandoCompuesto del juego.
 */
public class ComandoCompuesto implements Comando {
    private final List<Comando> comandos;

    /**
     * Ejecuta ComandoCompuesto.
     */
    public ComandoCompuesto() {
        this.comandos = new ArrayList<>();
    }

    /**
     * Ejecuta agregar.
      * @param comando valor de {@code comando}
     */
    public void agregar(Comando comando) {
        comandos.add(comando);
    }

    @Override
    /**
     * Ejecuta ejecutar.
     */
    public void ejecutar() throws ComandoException {
        for (Comando comando : comandos) {
            comando.ejecutar();
        }
    }
}

