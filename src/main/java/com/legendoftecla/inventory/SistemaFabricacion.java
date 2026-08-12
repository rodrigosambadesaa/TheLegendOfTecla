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
        recetas.put(receta.nombre().toLowerCase(Locale.ROOT), receta);
    }
    public List<Receta> recetas() { return List.copyOf(recetas.values()); }

    public ResultadoFabricacion fabricar(String nombre, Mochila mochila) {
        Receta receta = recetas.get(nombre.toLowerCase(Locale.ROOT));
        if (receta == null) return new ResultadoFabricacion(false, "Receta desconocida.", null);
        for (Ingrediente ingrediente : receta.ingredientes()) {
            long disponibles = mochila.getObjetos().stream().filter(objeto ->
                    objeto.getNombre().equalsIgnoreCase(ingrediente.nombre())).count();
            if (disponibles < ingrediente.cantidad()) {
                return new ResultadoFabricacion(false, "Faltan " + ingrediente.nombre() + ".", null);
            }
        }
        Objeto resultado = receta.fabrica().get();
        List<Objeto> originales = new ArrayList<>(mochila.getObjetos());
        for (Ingrediente ingrediente : receta.ingredientes()) {
            for (int i = 0; i < ingrediente.cantidad(); i++) {
                mochila.quitarPorNombre(ingrediente.nombre());
            }
        }
        if (!mochila.guardar(resultado)) {
            mochila.setObjetos(originales);
            return new ResultadoFabricacion(false, "No hay capacidad para el resultado.", null);
        }
        return new ResultadoFabricacion(true, "Fabricado: " + resultado.getNombre(), resultado);
    }
}
