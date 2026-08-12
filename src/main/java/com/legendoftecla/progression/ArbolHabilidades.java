package com.legendoftecla.progression;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/** Catalogo pequeno y ordenado de habilidades. */
public final class ArbolHabilidades {
    private final Map<String, Habilidad> habilidades = new LinkedHashMap<>();
    public void agregar(Habilidad habilidad) {
        Habilidad validada = java.util.Objects.requireNonNull(habilidad, "Habilidad");
        if (habilidades.putIfAbsent(validada.id(), validada) != null) {
            throw new IllegalArgumentException("Habilidad duplicada: " + validada.id());
        }
    }
    public Habilidad buscar(String id) { return habilidades.get(id); }
    public List<Habilidad> listar() { return List.copyOf(habilidades.values()); }
}
