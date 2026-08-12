package com.legendoftecla.model.items;

import com.legendoftecla.validation.Validaciones;

/** Explosivo arrojadizo utilizable por cualquier clase. */
public final class Granada extends Explosivo {
    private final TipoGranada tipo;

    /** Crea una granada con dano y alcance propios de su variante. */
    public Granada(String nombre, String descripcion, double peso, TipoGranada tipo) {
        super(nombre, descripcion, peso, danio(tipo), 4);
        this.tipo = Validaciones.noNulo(tipo, "Tipo de granada");
    }

    /** @return variante tactica */
    public TipoGranada getTipo() { return tipo; }

    private static int danio(TipoGranada tipo) {
        return switch (Validaciones.noNulo(tipo, "Tipo de granada")) {
            case FRAGMENTACION -> 35;
            case INCENDIARIA -> 20;
            case ATURDIDORA -> 5;
        };
    }
}
