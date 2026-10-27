package com.legendoftecla.model.elements;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SistemaCoberturaSeleccionTest {
    private static final Posicion ATACANTE = new Posicion(0, 0);
    private static final Posicion OBJETIVO = new Posicion(0, 2);
    private static final Posicion POSICION_COBERTURA = new Posicion(0, 1);

    @Test
    void laBarricadaOrientadaMasFuerteGanaEnCienPermutaciones() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Barricada flanqueada = barricada("flanqueada", TipoCobertura.COMPLETA,
                OrientacionCobertura.NORTE);
        Barricada media = barricada("media", TipoCobertura.MEDIA,
                OrientacionCobertura.OESTE);
        Barricada fuerte = barricada("fuerte", TipoCobertura.COMPLETA,
                OrientacionCobertura.OESTE);
        Barricada destruida = barricada("destruida", TipoCobertura.COMPLETA,
                OrientacionCobertura.OESTE);
        destruida.recibirDanio(10);
        List<Barricada> barreras = new ArrayList<>(List.of(
                flanqueada, media, fuerte, destruida));
        SistemaCobertura sistema = new SistemaCobertura(new Random(1234));
        Random random = new Random(20261027);

        for (int iteracion = 0; iteracion < 100; iteracion++) {
            Collections.shuffle(barreras, random);
            juego.getMapa().getCelda(POSICION_COBERTURA)
                    .setElementos(new ArrayList<>(barreras));
            SistemaCobertura.Proteccion seleccion =
                    sistema.proteccion(juego.getMapa(), ATACANTE, OBJETIVO);
            assertFalse(seleccion.flanqueada(), "iteracion " + iteracion);
            assertEquals(TipoCobertura.COMPLETA, seleccion.tipo());
            assertSame(fuerte, seleccion.barricada(), "iteracion " + iteracion);
        }
    }

    @Test
    void destruccionDeCoberturaFuertePermiteUsarLaSiguiente() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Barricada fuerte = barricada("fuerte", TipoCobertura.COMPLETA,
                OrientacionCobertura.OESTE);
        Barricada media = barricada("media", TipoCobertura.MEDIA,
                OrientacionCobertura.OESTE);
        juego.getMapa().getCelda(POSICION_COBERTURA)
                .setElementos(List.of(media, fuerte));
        SistemaCobertura sistema = new SistemaCobertura(new Random(7));

        assertSame(fuerte, sistema.proteccion(
                juego.getMapa(), ATACANTE, OBJETIVO).barricada());

        fuerte.recibirDanio(10);

        SistemaCobertura.Proteccion restante = sistema.proteccion(
                juego.getMapa(), ATACANTE, OBJETIVO);
        assertSame(media, restante.barricada());
        assertEquals(TipoCobertura.MEDIA, restante.tipo());
        assertFalse(restante.flanqueada());
    }

    @Test
    void coberturasTodasFlanqueadasMantienenPenalizacionNula() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Barricada norte = barricada("norte", TipoCobertura.COMPLETA,
                OrientacionCobertura.NORTE);
        Barricada sur = barricada("sur", TipoCobertura.MEDIA,
                OrientacionCobertura.SUR);
        juego.getMapa().getCelda(POSICION_COBERTURA)
                .setElementos(List.of(sur, norte));
        SistemaCobertura sistema = new SistemaCobertura(new Random(7));

        SistemaCobertura.Proteccion resultado = sistema.proteccion(
                juego.getMapa(), ATACANTE, OBJETIVO);

        assertTrue(resultado.flanqueada());
        assertEquals(0.8, sistema.probabilidadImpacto(
                0.8, resultado.tipo(), resultado.flanqueada(), 1.0), 0.0001);
    }

    @Test
    void empatesDeCoberturaSeResuelvenPorIdEstable() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Barricada zeta = barricada("zeta", TipoCobertura.COMPLETA,
                OrientacionCobertura.OESTE);
        Barricada alfa = barricada("alfa", TipoCobertura.COMPLETA,
                OrientacionCobertura.OESTE);
        SistemaCobertura sistema = new SistemaCobertura(new Random(7));
        for (List<Barricada> orden : List.of(List.of(zeta, alfa), List.of(alfa, zeta))) {
            juego.getMapa().getCelda(POSICION_COBERTURA).setElementos(orden);
            assertSame(alfa, sistema.proteccion(
                    juego.getMapa(), ATACANTE, OBJETIVO).barricada());
        }
    }

    private static Barricada barricada(String id, TipoCobertura tipo,
            OrientacionCobertura orientacion) {
        return new Barricada(id, 10, tipo, orientacion);
    }
}
