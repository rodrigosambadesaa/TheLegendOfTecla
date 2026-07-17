package com.legendoftecla.gui;

import com.legendoftecla.console.Consola;
import com.legendoftecla.console.TipoMensaje;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Adaptador de salida del juego para una interfaz grafica. */
public final class ConsolaGrafica implements Consola {
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

    public List<Mensaje> getHistorial() {
        return List.copyOf(historial);
    }

    public void setReceptor(Consumer<Mensaje> receptor) {
        this.receptor = receptor;
    }
}
