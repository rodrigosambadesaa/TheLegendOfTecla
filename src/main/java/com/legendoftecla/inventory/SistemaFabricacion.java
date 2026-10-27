package com.legendoftecla.inventory;

import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.items.Objeto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

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

    /**
     * Prepara el inventario final sin retirar objetos uno a uno. La mochila
     * recibe una unica asignacion validada cuando la fabricacion tiene exito;
     * ante cualquier fallo se conservan las referencias y su orden originales.
     */
    public ResultadoFabricacion fabricar(String nombre, Mochila mochila) {
        if (nombre == null || nombre.isBlank()) {
            return new ResultadoFabricacion(false, "Receta obligatoria.", null);
        }
        java.util.Objects.requireNonNull(mochila, "Mochila");
        Receta receta = recetas.get(nombre.toLowerCase(Locale.ROOT));
        if (receta == null) return new ResultadoFabricacion(false, "Receta desconocida.", null);

        Map<String, Integer> pendientes;
        try {
            pendientes = cantidades(receta);
        } catch (ArithmeticException error) {
            return new ResultadoFabricacion(false, "Cantidad de ingredientes excesiva.", null);
        }
        List<Objeto> restantes = new ArrayList<>(mochila.getObjetos().size());
        for (Objeto objeto : mochila.getObjetos()) {
            Integer necesarios = pendientes.get(objeto.getNombre());
            if (necesarios != null && necesarios > 0) {
                pendientes.put(objeto.getNombre(), necesarios - 1);
            } else {
                restantes.add(objeto);
            }
        }
        for (Map.Entry<String, Integer> ingrediente : pendientes.entrySet()) {
            if (ingrediente.getValue() > 0) {
                return new ResultadoFabricacion(false,
                        "Faltan " + ingrediente.getKey() + ".", null);
            }
        }

        Objeto resultado = java.util.Objects.requireNonNull(
                receta.fabrica().get(), "Resultado de receta");
        restantes.add(resultado);
        try {
            mochila.setObjetos(restantes);
        } catch (IllegalArgumentException error) {
            return new ResultadoFabricacion(false, "No hay capacidad para el resultado.", null);
        }
        return new ResultadoFabricacion(true, "Fabricado: " + resultado.getNombre(), resultado);
    }

    private Map<String, Integer> cantidades(Receta receta) {
        Map<String, Integer> resultado = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        receta.ingredientes().forEach(ingrediente -> resultado.merge(
                ingrediente.nombre(), ingrediente.cantidad(), Math::addExact));
        return resultado;
    }
}
