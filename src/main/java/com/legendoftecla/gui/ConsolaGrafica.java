package com.legendoftecla.gui;

import com.legendoftecla.console.Consola;
import com.legendoftecla.console.TipoMensaje;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Adaptador de salida del juego para una interfaz grafica. */
public final class ConsolaGrafica implements Consola {
    /**
     * Crea un adaptador de consola preparado para almacenar mensajes de la GUI.
     */
    public ConsolaGrafica() {
        setHistorial(List.of());
        setReceptor(null);
    }

    /** Representa un mensaje validado emitido en la interfaz grafica. */
    public static final class Mensaje {
        private String texto;
        private TipoMensaje tipo;

        /**
         * Crea un mensaje validado.
         *
         * @param texto contenido
         * @param tipo categoria
         */
        public Mensaje(String texto, TipoMensaje tipo) {
            setTexto(texto);
            setTipo(tipo);
        }

        /** @return texto del mensaje */
        public String getTexto() { return texto; }
        /** @param texto texto no nulo y acotado */
        public void setTexto(String texto) {
            this.texto = Validaciones.texto(texto, "Mensaje", Limites.MENSAJE);
        }
        /** @return tipo del mensaje */
        public TipoMensaje getTipo() { return tipo; }
        /** @param tipo categoria no nula */
        public void setTipo(TipoMensaje tipo) {
            this.tipo = Validaciones.noNulo(tipo, "Tipo de mensaje");
        }
        /** @return texto conservando la API anterior */
        public String texto() { return getTexto(); }
        /** @return tipo conservando la API anterior */
        public TipoMensaje tipo() { return getTipo(); }
    }

    private List<Mensaje> historial;
    private Consumer<Mensaje> receptor;

    @Override
    public void imprimir(String mensaje) {
        imprimir(mensaje, TipoMensaje.INFO);
    }

    @Override
    public void imprimir(String mensaje, TipoMensaje tipo) {
        Mensaje entrada = new Mensaje(mensaje, tipo);
        List<Mensaje> actualizado = new ArrayList<>(historial);
        if (actualizado.size() == Limites.HISTORIAL_MENSAJES) {
            actualizado.remove(0);
        }
        actualizado.add(entrada);
        setHistorial(actualizado);
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

    /** @param historial historial no nulo, sin mensajes nulos y acotado */
    public void setHistorial(List<Mensaje> historial) {
        Validaciones.noNulo(historial, "Historial");
        if (historial.size() > Limites.HISTORIAL_MENSAJES
                || historial.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("El historial grafico no es valido.");
        }
        this.historial = new ArrayList<>(historial);
    }

    /** @return receptor actual o {@code null} */
    public Consumer<Mensaje> getReceptor() {
        return receptor;
    }

    /**
     * Ejecuta la operacion publica {@code setReceptor}.
      * @param receptor valor de {@code receptor}
     */
    public void setReceptor(Consumer<Mensaje> receptor) {
        this.receptor = receptor;
    }
}
