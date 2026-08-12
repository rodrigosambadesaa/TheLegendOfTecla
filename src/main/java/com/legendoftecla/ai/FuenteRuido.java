package com.legendoftecla.ai;
/** Intensidad base reproducible de acciones ruidosas. */
public enum FuenteRuido {
    CAMINAR(2), CORRER(5), ABRIR_PUERTA(3), CERRAR_PUERTA(4),
    DISPARO(9), EXPLOSION(14), ANTORCHA(5), TERMINAL(2), OBJETO_PESADO(6);
    private final int intensidad;
    FuenteRuido(int intensidad) { this.intensidad = intensidad; }
    public int intensidad() { return intensidad; }
}
