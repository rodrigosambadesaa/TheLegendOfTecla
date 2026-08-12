package com.legendoftecla.ai;
/** Estrategia de decision para un estado de alerta. */
@FunctionalInterface
public interface EstadoIA {
    /** @return accion tactica sin efectos secundarios */
    AccionIA decidir(ContextoIA contexto);
}
