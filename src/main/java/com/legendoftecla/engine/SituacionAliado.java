package com.legendoftecla.engine;

/** Estado operativo visible de un aliado durante la simulacion. */
public enum SituacionAliado {
    ACTIVO("ACTIVO"),
    ACOMPANANDO("ACOMPANANDO AL JUGADOR"),
    ACUDIENDO("ACUDIENDO A LA LLAMADA"),
    PROTEGIENDO("PROTEGIENDO AL JUGADOR"),
    ASISTIENDO_JUGADOR("ASISTIENDO AL JUGADOR"),
    ASISTIENDO_ALIADO("ASISTIENDO A OTRO ALIADO"),
    REABASTECIENDOSE("REPONIENDO SU VIDA O ENERGIA"),
    BUSCANDO_SUMINISTROS("BUSCANDO SUMINISTROS"),
    EN_COMBATE("EN COMBATE"),
    FUERA_DE_COMBATE("FUERA DE COMBATE"),
    EN_ESPERA_POR_RIESGO("EN ESPERA: VIDA EN PELIGRO"),
    EN_ESPERA_POR_RECURSOS("EN ESPERA: RECURSOS INSUFICIENTES"),
    SIN_RUTA("EN ESPERA: SIN RUTA AL JUGADOR"),
    EVACUADO("EVACUADO: LLEGO A LA SALIDA"),
    CAIDO("CAIDO: FUERA DE COMBATE");

    private final String etiqueta;

    SituacionAliado(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** @return descripcion estable para consola y GUI */
    public String getEtiqueta() {
        return etiqueta;
    }
}
