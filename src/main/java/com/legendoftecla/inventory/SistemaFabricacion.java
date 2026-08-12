package com.legendoftecla.inventory;

import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.items.Objeto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Catalogo ordenado y ejecucion transaccional de recetas. */
public final class SistemaFabricacion {
    private final Map<String, Receta> recetas = new LinkedHashMap<>();
    public void registrar(Receta receta) {
        Receta validada = java.util.Objects.requireNonNull(receta, "Receta");
        String clave = validada.nombre().toLowerCase(Locale.ROOT);
        if (recetas.putIfAbsent(clave, validada) != null) {
            throw new IllegalArgumentException("Receta duplicada: " + validada.nombre());
        }
    }
    public List<Receta> recetas() { return List.copyOf(recetas.values()); }

    public ResultadoFabricacion fabricar(String nombre, Mochila mochila) {
        if (nombre == null || nombre.isBlank()) {
            return new ResultadoFabricacion(false, "Receta obligatoria.", null);
        }
        java.util.Objects.requireNonNull(mochila, "Mochila");
        Receta receta = recetas.get(nombre.toLowerCase(Locale.ROOT));
        if (receta == null) return new ResultadoFabricacion(false, "Receta desconocida.", null);
        Map<String, Integer> cantidades = cantidades(receta);
        for (Map.Entry<String, Integer> ingrediente : cantidades.entrySet()) {
            long disponibles = mochila.getObjetos().stream().filter(objeto ->
                    objeto.getNombre().equalsIgnoreCase(ingrediente.getKey())).count();
            if (disponibles < ingrediente.getValue()) {
                return new ResultadoFabricacion(false,
                        "Faltan " + ingrediente.getKey() + ".", null);
            }
        }
        Objeto resultado = java.util.Objects.requireNonNull(
                receta.fabrica().get(), "Resultado de receta");
        List<Objeto> originales = new ArrayList<>(mochila.getObjetos());
        for (Map.Entry<String, Integer> ingrediente : cantidades.entrySet()) {
            for (int i = 0; i < ingrediente.getValue(); i++) {
                mochila.quitarPorNombre(ingrediente.getKey());
            }
        }
        if (!mochila.guardar(resultado)) {
            mochila.setObjetos(originales);
            return new ResultadoFabricacion(false, "No hay capacidad para el resultado.", null);
        }
        return new ResultadoFabricacion(true, "Fabricado: " + resultado.getNombre(), resultado);
    }

    private Map<String, Integer> cantidades(Receta receta) {
        Map<String, Integer> resultado = new java.util.TreeMap<>(
                String.CASE_INSENSITIVE_ORDER);
        receta.ingredientes().forEach(ingrediente -> resultado.merge(
                ingrediente.nombre(), ingrediente.cantidad(), Integer::sum));
        return resultado;
    }
}
