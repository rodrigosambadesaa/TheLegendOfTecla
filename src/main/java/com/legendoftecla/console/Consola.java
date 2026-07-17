package com.legendoftecla.console;


/**
 * Representa la entidad Consola del juego.
 */
public interface Consola {
    /**
     * Ejecuta la operacion publica {@code imprimir}.
      * @param mensaje valor de {@code mensaje}
     */
    void imprimir(String mensaje);

    /**
     * Ejecuta la operacion publica {@code imprimir}.
      * @param mensaje valor de {@code mensaje}
      * @param tipo valor de {@code tipo}
     */
    default void imprimir(String mensaje, TipoMensaje tipo) {
        imprimir(mensaje);
    }

    /**
     * Ejecuta la operacion publica {@code imprimirInfo}.
      * @param mensaje valor de {@code mensaje}
     */
    default void imprimirInfo(String mensaje) {
        imprimir(mensaje, TipoMensaje.INFO);
    }

    /**
     * Ejecuta la operacion publica {@code imprimirExito}.
      * @param mensaje valor de {@code mensaje}
     */
    default void imprimirExito(String mensaje) {
        imprimir(mensaje, TipoMensaje.EXITO);
    }

    /**
     * Ejecuta la operacion publica {@code imprimirError}.
      * @param mensaje valor de {@code mensaje}
     */
    default void imprimirError(String mensaje) {
        imprimir(mensaje, TipoMensaje.ERROR);
    }

    /**
     * Ejecuta la operacion publica {@code imprimirAdvertencia}.
      * @param mensaje valor de {@code mensaje}
     */
    default void imprimirAdvertencia(String mensaje) {
        imprimir(mensaje, TipoMensaje.ADVERTENCIA);
    }

    /**
     * Ejecuta la operacion publica {@code imprimirEstado}.
      * @param mensaje valor de {@code mensaje}
     */
    default void imprimirEstado(String mensaje) {
        imprimir(mensaje, TipoMensaje.ESTADO);
    }

    /**
     * Ejecuta la operacion publica {@code leer}.
      * @param descripcion valor de {@code descripcion}
      * @return resultado de la operacion
     */
    String leer(String descripcion);
}

