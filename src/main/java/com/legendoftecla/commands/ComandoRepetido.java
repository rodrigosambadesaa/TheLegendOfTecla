package com.legendoftecla.commands;

import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.validation.Validaciones;


/**
 * Representa la entidad ComandoRepetido del juego.
 */
public class ComandoRepetido implements Comando {
    private Comando comando;
    private int repeticiones;

    /**
     * Ejecuta ComandoRepetido.
      * @param comando valor de {@code comando}
      * @param repeticiones valor de {@code repeticiones}
     */
    public ComandoRepetido(Comando comando, int repeticiones) {
        setComando(comando);
        setRepeticiones(repeticiones);
    }

    /** @return comando repetido */
    public Comando getComando() { return comando; }
    /** @param comando comando no nulo */
    public void setComando(Comando comando) { this.comando = Validaciones.noNulo(comando, "Comando"); }
    /** @return numero de repeticiones */
    public int getRepeticiones() { return repeticiones; }
    /** @param repeticiones cantidad entre 1 y 1000 */
    public void setRepeticiones(int repeticiones) {
        this.repeticiones = Validaciones.enteroEntre(repeticiones, 1, 1_000, "Repeticiones");
    }

    @Override
    /**
     * Ejecuta ejecutar.
     */
    public void ejecutar() throws ComandoException {
        for (int i = 0; i < repeticiones; i++) {
            comando.ejecutar();
        }
    }
}

