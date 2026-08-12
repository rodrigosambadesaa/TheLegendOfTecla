package com.legendoftecla.effects;

import com.legendoftecla.model.characters.Personaje;

/** Estrategia reutilizable de un efecto temporal. */
public interface EfectoEstado {
    /** @return identidad estable */
    TipoEstado tipo();
    /** @return duracion inicial en turnos */
    int duracionInicial();
    /** @return si una nueva aplicacion incrementa potencia */
    default boolean acumulable() { return false; }
    /** @return si una nueva aplicacion renueva la duracion */
    default boolean renuevaDuracion() { return true; }
    /** Hook al aplicar por primera vez. */
    default void alAplicar(Personaje personaje) { }
    /** Hook al comienzo del turno. */
    default void alInicioTurno(Personaje personaje, int acumulaciones) { }
    /** Hook al final del turno. */
    default void alFinTurno(Personaje personaje, int acumulaciones) { }
    /** Hook tras moverse. */
    default void alMover(Personaje personaje, int acumulaciones) { }
    /** Hook de limpieza al caducar o eliminarse explicitamente. */
    default void alEliminar(Personaje personaje) { }
    /** @return si una condicion de dominio exige retirar el efecto */
    default boolean debeEliminarse(Personaje personaje) { return personaje.getSalud() <= 0; }
    /** @return multiplicador de precision aportado */
    default double multiplicadorPrecision() { return 1.0; }
    /** @return multiplicador de vision aportado */
    default double multiplicadorVision() { return 1.0; }
    /** @return si impide ejecutar la siguiente accion */
    default boolean bloqueaAccion() { return false; }
}
