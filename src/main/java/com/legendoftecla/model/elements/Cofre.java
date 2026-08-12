package com.legendoftecla.model.elements;
import com.legendoftecla.model.items.Objeto;
import java.util.ArrayList;
import java.util.List;
/** Contenedor interactivo de loot finito. */
public final class Cofre extends ElementoBase {
    private final List<Objeto> contenido;
    private boolean abierto;
    public Cofre(String id, List<Objeto> contenido) {
        super(id, true, 20);
        this.contenido = new ArrayList<>(List.copyOf(contenido));
    }
    public List<Objeto> abrir() { abierto = true; return List.copyOf(contenido); }
    public boolean retirar(Objeto objeto) { return abierto && contenido.remove(objeto); }
    public boolean isAbierto() { return abierto; }
    public boolean permitePaso() { return estaDestruido(); }
    public boolean bloqueaVision() { return !estaDestruido(); }
    public char simbolo() { return abierto ? 'c' : 'C'; }
}
