package com.legendoftecla.ai;
/** Rol enemigo con comportamiento distinguible. */
public interface EnemigoTactico {
    /** Decide la accion preferida del rol sobre un contexto comun. */
    AccionIA decidirTactica(ContextoIA contexto);
}
