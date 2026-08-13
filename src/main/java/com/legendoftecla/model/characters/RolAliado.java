package com.legendoftecla.model.characters;

/** Especializacion operativa de un miembro del escuadron aliado. */
public enum RolAliado {
    /** Protege al jugador, combate y explora. */
    COMBATIENTE("Combatiente"),
    /** Prioriza pacientes, botiquines y suministros energeticos. */
    MEDICO("Medico");

    private final String etiqueta;

    RolAliado(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** @return nombre apto para consola y GUI */
    public String getEtiqueta() {
        return etiqueta;
    }
}
