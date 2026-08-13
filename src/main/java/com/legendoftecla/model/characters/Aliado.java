package com.legendoftecla.model.characters;

import com.legendoftecla.model.world.Posicion;


/**
 * Representa la entidad Aliado del juego.
 */
public final class Aliado extends Personaje {
    private int nivel = 1;
    private RolAliado rol = RolAliado.COMBATIENTE;
    /**
     * Ejecuta Aliado.
      * @param mochila valor de {@code mochila}
      * @param nombre valor de {@code nombre}
      * @param posicion valor de {@code posicion}
      * @param visionBase valor de {@code visionBase}
     */
    public Aliado(String nombre, Posicion posicion, Mochila mochila, int visionBase) {
        super(nombre, 90, 140, posicion, mochila, visionBase);
    }

    /** @return nivel tactico del aliado */
    public int getNivel() { return nivel; }

    /** @param nivel nivel entre uno y el limite defensivo */
    public void setNivel(int nivel) {
        this.nivel = com.legendoftecla.validation.Validaciones.enteroEntre(nivel, 1,
                com.legendoftecla.validation.Limites.NIVEL_ALIADO_MAXIMO, "Nivel del aliado");
    }

    /** @return especializacion operativa del aliado */
    public RolAliado getRol() { return rol; }

    /** @param rol especializacion no nula */
    public void setRol(RolAliado rol) {
        this.rol = java.util.Objects.requireNonNull(rol, "Rol del aliado");
    }

    /** @return si el aliado prioriza asistencia y suministros */
    public boolean esMedico() { return rol == RolAliado.MEDICO; }

    @Override
    /**
     * Ejecuta aplicarModificadorDanio.
     */
    protected int aplicarModificadorDanio(int base, Personaje objetivo) {
        int distancia = Math.max(1, getPosicion().distanciaManhattan(objetivo.getPosicion()));
        if (distancia <= 1) {
            return (int) Math.ceil(base * 1.4);
        }
        return base;
    }
}

