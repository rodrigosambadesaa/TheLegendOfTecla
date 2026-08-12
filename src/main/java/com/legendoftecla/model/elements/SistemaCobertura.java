package com.legendoftecla.model.elements;

import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;

import java.util.Objects;
import java.util.random.RandomGenerator;

/** Resolucion reproducible de precision, cobertura y flanqueo. */
public final class SistemaCobertura {
    private final RandomGenerator random;
    public SistemaCobertura(RandomGenerator random) {
        this.random = Objects.requireNonNull(random, "RNG");
    }
    /** @return probabilidad final limitada al intervalo unitario */
    public double probabilidadImpacto(double base, TipoCobertura cobertura,
            boolean flanqueado, double modificadorEstado) {
        double penalizacion = flanqueado ? 0 : switch (cobertura) {
            case NINGUNA -> 0;
            case MEDIA -> 0.25;
            case COMPLETA -> 0.55;
        };
        return Math.max(0.05, Math.min(0.95, (base - penalizacion) * modificadorEstado));
    }
    /** Resuelve un impacto consumiendo exactamente una decision RNG. */
    public boolean impacta(double probabilidad) {
        return random.nextDouble() < probabilidad;
    }

    /**
     * Localiza la cobertura situada inmediatamente entre atacante y objetivo.
     *
     * @return proteccion efectiva, incluyendo si la orientacion queda flanqueada
     */
    public Proteccion proteccion(Mapa mapa, Posicion atacante, Posicion objetivo) {
        int diferenciaFila = atacante.getFila() - objetivo.getFila();
        int diferenciaColumna = atacante.getColumna() - objetivo.getColumna();
        if (diferenciaFila != 0 && diferenciaColumna != 0 || atacante.equals(objetivo)) {
            return Proteccion.ninguna();
        }
        int pasoFila = Integer.signum(diferenciaFila);
        int pasoColumna = Integer.signum(diferenciaColumna);
        Posicion posicionCobertura = new Posicion(
                objetivo.getFila() + pasoFila, objetivo.getColumna() + pasoColumna);
        if (!mapa.estaDentro(posicionCobertura)) {
            return Proteccion.ninguna();
        }
        OrientacionCobertura ataqueDesde = orientacion(pasoFila, pasoColumna);
        return mapa.getCelda(posicionCobertura).getElementos().stream()
                .filter(Barricada.class::isInstance).map(Barricada.class::cast)
                .filter(barricada -> !barricada.estaDestruido())
                .map(barricada -> new Proteccion(barricada.getCobertura(),
                        barricada.getOrientacion() != OrientacionCobertura.TODAS
                                && barricada.getOrientacion() != ataqueDesde,
                        barricada))
                .findFirst().orElseGet(Proteccion::ninguna);
    }

    private OrientacionCobertura orientacion(int fila, int columna) {
        if (fila < 0) return OrientacionCobertura.NORTE;
        if (fila > 0) return OrientacionCobertura.SUR;
        if (columna < 0) return OrientacionCobertura.OESTE;
        return OrientacionCobertura.ESTE;
    }

    /** Resultado de resolver posicion y orientacion de una cobertura. */
    public record Proteccion(TipoCobertura tipo, boolean flanqueada, Barricada barricada) {
        /** @return ausencia de cobertura */
        public static Proteccion ninguna() {
            return new Proteccion(TipoCobertura.NINGUNA, false, null);
        }
    }
}
