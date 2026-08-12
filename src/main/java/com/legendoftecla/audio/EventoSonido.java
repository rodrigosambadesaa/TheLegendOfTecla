package com.legendoftecla.audio;

/** Efectos sonoros disponibles en los dos modos de juego. */
public enum EventoSonido {
    MOVIMIENTO("move.wav"),
    ATAQUE("attack.wav"),
    DANIO("damage.wav"),
    EQUIPAR("equip.wav"),
    DESEQUIPAR("unequip.wav"),
    TIRAR("drop.wav"),
    MUERTE_ENEMIGO("enemy-death.wav"),
    MUERTE_ALIADO("ally-death.wav"),
    MUERTE_JUGADOR("player-death.wav"),
    INCENDIO("fire-start.wav"),
    APAGAR_FUEGO("fire-out.wav"),
    // Se reutilizan recursos compatibles ya auditados en el repositorio.
    PUERTA("move.wav"),
    RECARGA("equip.wav"),
    TRAMPA("attack.wav"),
    ALARMA("enemy-death.wav"),
    AGUA("fire-out.wav"),
    DESCUBRIMIENTO("equip.wav"),
    MISION("ally-death.wav");

    private final String archivo;

    EventoSonido(String archivo) { this.archivo = archivo; }
    public String getArchivo() { return archivo; }
}
