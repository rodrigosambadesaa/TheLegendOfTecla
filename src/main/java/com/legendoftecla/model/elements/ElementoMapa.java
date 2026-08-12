package com.legendoftecla.model.elements;

/** Contrato de un elemento interactivo alojado en una celda. */
public interface ElementoMapa {
    /** @return identificador unico dentro del escenario */
    String getId();
    /** @return si permite atravesar su celda */
    boolean permitePaso();
    /** @return si interrumpe vision y linea de tiro */
    boolean bloqueaVision();
    /** @return simbolo ASCII del estado actual */
    char simbolo();
    /** Aplica dano estructural si el elemento es destructible. */
    void recibirDanio(int cantidad);
    /** @return si dejo de afectar al mapa */
    boolean estaDestruido();
}
