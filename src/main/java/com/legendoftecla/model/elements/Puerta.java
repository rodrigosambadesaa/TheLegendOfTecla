package com.legendoftecla.model.elements;

/** Puerta dinamica con credencial opcional y resistencia estructural. */
public final class Puerta extends ElementoBase {
    private EstadoPuerta estado;
    private final String credencial;

    public Puerta(String id, EstadoPuerta estado, String credencial,
            boolean destructible, int resistencia) {
        super(id, destructible, resistencia);
        this.estado = java.util.Objects.requireNonNull(estado, "Estado");
        this.credencial = credencial == null || credencial.isBlank() ? null : credencial;
    }

    /** @return estado actual */
    public EstadoPuerta getEstado() { return estado; }
    /** @return ID de llave o tarjeta exigida, o {@code null} */
    public String getCredencial() { return credencial; }

    /** Abre una puerta accesible o desbloqueada con la credencial correcta. */
    public boolean abrir(String credencialPresentada) {
        if (estaDestruido() || estado == EstadoPuerta.ABIERTA) return true;
        if (estado == EstadoPuerta.BLINDADA) return false;
        if (estado == EstadoPuerta.BLOQUEADA
                && !java.util.Objects.equals(credencial, credencialPresentada)) return false;
        estado = EstadoPuerta.ABIERTA;
        return true;
    }

    /** Cierra una puerta abierta y no destruida. */
    public boolean cerrar() {
        if (estaDestruido() || estado != EstadoPuerta.ABIERTA) return false;
        estado = EstadoPuerta.CERRADA;
        return true;
    }

    /** Bloquea una puerta cerrada si dispone de credencial configurada. */
    public boolean bloquear() {
        if (estado != EstadoPuerta.CERRADA || credencial == null) return false;
        estado = EstadoPuerta.BLOQUEADA;
        return true;
    }

    /** Desbloquea incluso una puerta blindada mediante un terminal autorizado. */
    public boolean desbloquearPorTerminal() {
        if (estaDestruido() || estado == EstadoPuerta.ABIERTA) return false;
        estado = EstadoPuerta.CERRADA;
        return true;
    }

    public boolean permitePaso() { return estaDestruido() || estado == EstadoPuerta.ABIERTA; }
    public boolean bloqueaVision() { return !permitePaso(); }
    public char simbolo() { return permitePaso() ? '/' : '+'; }
}
