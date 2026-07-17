package com.legendoftecla.gui;

import com.legendoftecla.console.Consola;
import com.legendoftecla.console.TipoMensaje;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Adaptador de salida del juego para una interfaz grafica. */
public final class ConsolaGrafica implements Consola {
    /**
     * Crea un adaptador de consola preparado para almacenar mensajes de la GUI.
     */
    public ConsolaGrafica() {
        // La coleccion de mensajes se inicializa junto con la instancia.
    }

    /**
     * Representa {@code Mensaje} dentro del dominio del juego.
      * @param texto valor de {@code texto}
      * @param tipo valor de {@code tipo}
     */
    public record Mensaje(String texto, TipoMensaje tipo) {
    }

    private final List<Mensaje> historial = new ArrayList<>();
    private Consumer<Mensaje> receptor;

    @Override
    public void imprimir(String mensaje) {
        imprimir(mensaje, TipoMensaje.INFO);
    }

    @Override
    public void imprimir(String mensaje, TipoMensaje tipo) {
        Mensaje entrada = new Mensaje(mensaje, tipo);
        historial.add(entrada);
        if (receptor != null) {
            receptor.accept(entrada);
        }
    }

    @Override
    public String leer(String descripcion) {
        throw new UnsupportedOperationException("La GUI no lee desde la entrada estandar.");
    }

    /**
     * Obtiene el valor de {@code Historial}.
      * @return resultado de la operacion
     */
    public List<Mensaje> getHistorial() {
        return List.copyOf(historial);
    }

    /**
     * Ejecuta la operacion publica {@code setReceptor}.
      * @param receptor valor de {@code receptor}
     */
    public void setReceptor(Consumer<Mensaje> receptor) {
        this.receptor = receptor;
    }
}
