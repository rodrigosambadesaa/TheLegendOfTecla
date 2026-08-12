package com.legendoftecla.progression;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/** Catalogo pequeno y ordenado de habilidades. */
public final class ArbolHabilidades {
    private final Map<String, Habilidad> habilidades = new LinkedHashMap<>();
    public void agregar(Habilidad habilidad) { habilidades.put(habilidad.id(), habilidad); }
    public Habilidad buscar(String id) { return habilidades.get(id); }
    public List<Habilidad> listar() { return List.copyOf(habilidades.values()); }
}
