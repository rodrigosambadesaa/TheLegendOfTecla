package com.legendoftecla.constants;

/** Estrategia de movimiento y combate del grupo aliado. */
public enum FormacionAliada {
    SIN_FORMACION("sin formacion"),
    DEFENSIVA("defensiva"),
    OFENSIVA("ofensiva");

    private final String etiqueta;

    FormacionAliada(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** @return nombre legible de la estrategia */
    public String getEtiqueta() {
        return etiqueta;
    }
}
