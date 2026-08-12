package com.legendoftecla.achievements;
import com.legendoftecla.stats.EstadisticasPartida;
import java.util.function.Predicate;
/** Definicion declarativa de un logro. */
public record Logro(String id, String nombre, String descripcion,
        Predicate<EstadisticasPartida> condicion) { }
