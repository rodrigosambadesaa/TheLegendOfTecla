package com.legendoftecla.loader;

import com.legendoftecla.model.items.CuboAgua;
import com.legendoftecla.model.items.Linterna;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.model.world.TipoSuelo;

import java.util.Random;

/** Añade zonas oscuras, madera, luz y agua a mapas que no las declaran expresamente. */
public final class GeneradorAmbiente {
    private GeneradorAmbiente() { }

    public static void completar(Mapa mapa, Random random) {
        int transitables = 0;
        boolean tieneLinterna = false;
        boolean tieneCubo = false;
        boolean tieneFuente = false;
        boolean tieneAntorcha = false;
        for (int f = 0; f < mapa.getFilas(); f++) {
            for (int c = 0; c < mapa.getColumnas(); c++) {
                Celda celda = mapa.getCelda(new Posicion(f, c));
                if (!celda.isTransitable()) continue;
                transitables++;
                tieneLinterna |= celda.getObjetos().stream().anyMatch(Linterna.class::isInstance);
                tieneCubo |= celda.getObjetos().stream().anyMatch(CuboAgua.class::isInstance);
                tieneFuente |= celda.hasFuenteAgua();
                tieneAntorcha |= celda.hasAntorchaMural();
                if (!celda.isOscuridadPermanente() && random.nextDouble() < 0.14) celda.setOscura(true);
                if (random.nextDouble() < 0.18) celda.setTipoSuelo(TipoSuelo.MADERA);
            }
        }
        Celda inicio = mapa.getCelda(mapa.getInicio());
        if (!inicio.isOscuridadPermanente()) inicio.setOscura(false);
        if (!tieneLinterna) inicio.agregarObjeto(new Linterna(
                "Linterna", "Linterna reutilizable para zonas oscuras", 0.8, 4));
        if (!tieneCubo) inicio.agregarObjeto(new CuboAgua(
                "Cubo de agua", "Cubo reutilizable para combatir incendios", 2.0, true));
        if (!tieneFuente) buscarCeldaLibre(mapa, random).setFuenteAgua(true);
        if (!tieneAntorcha) buscarCeldaLibre(mapa, random).setAntorchaMural(true);
        if (transitables > 8) buscarCeldaLibre(mapa, random).setTipoSuelo(TipoSuelo.MADERA);
    }

    private static Celda buscarCeldaLibre(Mapa mapa, Random random) {
        for (int intento = 0; intento < mapa.getFilas() * mapa.getColumnas() * 2; intento++) {
            Posicion p = new Posicion(random.nextInt(mapa.getFilas()), random.nextInt(mapa.getColumnas()));
            if (mapa.esTransitable(p) && !p.equals(mapa.getInicio()) && !p.equals(mapa.getObjetivo())) {
                return mapa.getCelda(p);
            }
        }
        return mapa.getCelda(mapa.getInicio());
    }
}
