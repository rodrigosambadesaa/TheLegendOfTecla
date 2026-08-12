package com.legendoftecla.missions;
/** Especializacion semantica para neutralizar un jefe. */
public final class EliminarJefe extends EliminarEnemigo {
    public EliminarJefe(String nombre) { super(nombre); }
    @Override public String descripcion() { return "Eliminar al jefe"; }
}
