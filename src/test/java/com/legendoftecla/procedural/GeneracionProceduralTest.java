package com.legendoftecla.procedural;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.loader.CargadorJuegoProcedural;
import com.legendoftecla.model.items.Municion;
import com.legendoftecla.model.world.DimensionesMapa;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneracionProceduralTest {
    static Stream<GeneradorMapa> generadores() {
        return Stream.of(new GeneradorHabitaciones(), new GeneradorLaberinto(), new GeneradorCuevas());
    }

    @ParameterizedTest
    @MethodSource("generadores")
    void mismaSemillaMismoMapaYSalidaAlcanzable(GeneradorMapa generador) {
        ConfiguracionGeneracion config = ConfiguracionGeneracion.normal(15, 21);
        Mapa primero = generador.generar(12345, config);
        Mapa segundo = generador.generar(12345, config);
        assertEquals(firma(primero), firma(segundo));
        assertTrue(alcanzable(primero));
    }

    @Test
    void semillasDistintasCambianLaCueva() {
        GeneradorMapa generador = new GeneradorCuevas();
        ConfiguracionGeneracion config = ConfiguracionGeneracion.normal(15, 21);
        assertNotEquals(firma(generador.generar(1, config)), firma(generador.generar(2, config)));
    }

    @Test
    void cargadorProceduralPueblaEnemigosYRecursosDeFormaDeterminista() throws Exception {
        var dimensiones = new DimensionesMapa(15, 21);
        var primero = new CargadorJuegoProcedural(TestFixtures.consola(), "Tecla",
                "marine", Dificultad.NORMAL, dimensiones, false, 77).cargarJuego();
        var segundo = new CargadorJuegoProcedural(TestFixtures.consola(), "Tecla",
                "marine", Dificultad.NORMAL, dimensiones, false, 77).cargarJuego();

        assertTrue(!primero.getEnemigos().isEmpty());
        assertTrue(java.util.Arrays.stream(primero.getMapa().getCeldas())
                .flatMap(java.util.Arrays::stream).flatMap(celda -> celda.getObjetos().stream())
                .anyMatch(Municion.class::isInstance));
        assertEquals(primero.getEnemigos().stream().map(e -> e.getPosicion().toString()).toList(),
                segundo.getEnemigos().stream().map(e -> e.getPosicion().toString()).toList());
    }

    private String firma(Mapa mapa) {
        StringBuilder firma = new StringBuilder();
        for (int f = 0; f < mapa.getFilas(); f++) {
            for (int c = 0; c < mapa.getColumnas(); c++) {
                Posicion p = new Posicion(f, c);
                firma.append(mapa.esTransitable(p) ? '.' : '#')
                        .append(mapa.getCelda(p).simboloElemento());
            }
        }
        return firma.toString();
    }

    private boolean alcanzable(Mapa mapa) {
        ArrayDeque<Posicion> pendientes = new ArrayDeque<>(List.of(mapa.getInicio()));
        Set<Posicion> visitadas = new HashSet<>(Set.of(mapa.getInicio()));
        while (!pendientes.isEmpty()) {
            Posicion actual = pendientes.remove();
            if (actual.equals(mapa.getObjetivo())) return true;
            for (Direccion direccion : Direccion.values()) {
                Posicion candidata = actual.mover(direccion);
                if (mapa.esTransitable(candidata) && visitadas.add(candidata)) pendientes.add(candidata);
            }
        }
        return false;
    }
}
