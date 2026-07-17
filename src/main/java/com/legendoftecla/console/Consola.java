package com.legendoftecla.console;


/**
 * Representa la entidad Consola del juego.
 */
public interface Consola {
    void imprimir(String mensaje);

    default void imprimir(String mensaje, TipoMensaje tipo) {
        imprimir(mensaje);
    }

    default void imprimirInfo(String mensaje) {
        imprimir(mensaje, TipoMensaje.INFO);
    }

    default void imprimirExito(String mensaje) {
        imprimir(mensaje, TipoMensaje.EXITO);
    }

    default void imprimirError(String mensaje) {
        imprimir(mensaje, TipoMensaje.ERROR);
    }

    default void imprimirAdvertencia(String mensaje) {
        imprimir(mensaje, TipoMensaje.ADVERTENCIA);
    }

    default void imprimirEstado(String mensaje) {
        imprimir(mensaje, TipoMensaje.ESTADO);
    }

    String leer(String descripcion);
}

