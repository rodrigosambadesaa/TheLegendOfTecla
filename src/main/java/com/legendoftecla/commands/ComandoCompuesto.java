package com.legendoftecla.commands;

import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.validation.Validaciones;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Representa la entidad ComandoCompuesto del juego.
 */
public class ComandoCompuesto implements Comando {
    private List<Comando> comandos;

    /**
     * Ejecuta ComandoCompuesto.
     */
    public ComandoCompuesto() {
        setComandos(List.of());
    }

    /** @return vista inmutable de los comandos */
    public List<Comando> getComandos() { return Collections.unmodifiableList(comandos); }
    /** @param comandos comandos no nulos, sin elementos nulos y acotados */
    public void setComandos(List<Comando> comandos) {
        Validaciones.noNulo(comandos, "Comandos");
        if (comandos.size() > 1_000 || comandos.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("La composicion admite hasta 1000 comandos no nulos.");
        }
        this.comandos = new ArrayList<>(comandos);
    }

    /**
     * Ejecuta agregar.
      * @param comando valor de {@code comando}
     */
    public void agregar(Comando comando) {
        List<Comando> nuevos = new ArrayList<>(comandos);
        nuevos.add(Validaciones.noNulo(comando, "Comando"));
        setComandos(nuevos);
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

