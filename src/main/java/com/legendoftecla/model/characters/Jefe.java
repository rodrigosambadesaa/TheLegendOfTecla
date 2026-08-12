package com.legendoftecla.model.characters;
import com.legendoftecla.model.world.Posicion;
/** Enemigo con transiciones de fase basadas en salud. */
public abstract class Jefe extends Enemigo {
    private FaseJefe fase = FaseJefe.UNO;
    private final java.util.Set<FaseJefe> habilidadesEjecutadas =
            java.util.EnumSet.noneOf(FaseJefe.class);
    protected Jefe(String nombre, int salud, Posicion posicion, Mochila mochila, int vision) {
        super(nombre, salud, 200, posicion, mochila, vision);
    }
    public FaseJefe actualizarFase() {
        double proporcion = (double) getSalud() / getSaludMaxima();
        fase = proporcion > 0.70 ? FaseJefe.UNO : proporcion > 0.40 ? FaseJefe.DOS
                : proporcion > 0.15 ? FaseJefe.TRES : FaseJefe.FINAL;
        return fase;
    }
    public FaseJefe getFase() { return actualizarFase(); }
    /** @return {@code true} una sola vez por fase para evitar invocaciones infinitas */
    public boolean consumirHabilidadDeFase() {
        return habilidadesEjecutadas.add(getFase());
    }
    /** @return fases cuya habilidad especial ya se ejecuto */
    public java.util.Set<FaseJefe> getHabilidadesEjecutadas() {
        return java.util.Set.copyOf(habilidadesEjecutadas);
    }
    /** @return habilidad especial correspondiente a la fase */
    public abstract String habilidadActual();
}
