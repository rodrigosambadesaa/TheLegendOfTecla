package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

class ServiciosMotorRefactorTest {
    @Test
    void navegacionCalculaRutaMinimaYRodeaObstaculos() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        juego.getMapa().setCelda(0, 1, new Celda("Muro", false));

        assertEquals(4, NavegacionTactica.distancia(juego.getMapa(),
                new Posicion(0, 0), new Posicion(0, 2)));
        assertEquals(Direccion.SUR, NavegacionTactica.primerPaso(juego.getMapa(),
                new Posicion(0, 0), new Posicion(0, 2)));
        assertEquals(0, NavegacionTactica.distancia(juego.getMapa(),
                new Posicion(1, 1), new Posicion(1, 1)));
    }

    @Test
    void navegacionInformaDestinoInaccesible() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        juego.getMapa().setCelda(1, 2, new Celda("Muro", false));
        juego.getMapa().setCelda(2, 1, new Celda("Muro", false));

        assertEquals(-1, NavegacionTactica.distancia(juego.getMapa(),
                new Posicion(0, 0), new Posicion(2, 2)));
        assertNull(NavegacionTactica.primerPaso(juego.getMapa(),
                new Posicion(0, 0), new Posicion(2, 2)));
    }

    @Test
    void registroActualizaEnTiempoConstanteYGeneraResumen() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Aliado aliado = new Aliado("Ada", new Posicion(1, 1), new Mochila(3, 10), 2);
        juego.agregarAliado(aliado);
        RegistroEstadoAliados registro = new RegistroEstadoAliados();
        registro.inicializar(List.of(aliado));

        registro.cambiar(aliado, SituacionAliado.PROTEGIENDO);
        registro.marcarCombate(aliado, true);

        assertEquals(SituacionAliado.PROTEGIENDO, registro.situacion(juego, aliado));
        assertTrue(registro.estaEnCombate(juego, aliado));
        assertTrue(registro.resumen(juego).contains("PROTEGIENDO AL JUGADOR"));
        assertThrows(UnsupportedOperationException.class,
                () -> registro.getCombates().put(aliado, false));

        aliado.setSalud(0);
        assertEquals(SituacionAliado.CAIDO, registro.situacion(juego, aliado));
        assertFalse(registro.estaEnCombate(juego, aliado));
    }

    @Test
    void registroRechazaEntradasCorruptas() {
        RegistroEstadoAliados registro = new RegistroEstadoAliados();
        assertThrows(NullPointerException.class, () -> registro.inicializar(null));
        assertThrows(IllegalArgumentException.class,
                () -> registro.setSituaciones(
                        java.util.Collections.singletonMap(null, SituacionAliado.ACTIVO)));
    }

    @Test
    void indiceEspacialBuscaPorDistanciaYRespetaRadio() {
        Aliado cercano = new Aliado("Cercano", new Posicion(2, 3), new Mochila(3, 10), 2);
        Aliado lejano = new Aliado("Lejano", new Posicion(8, 8), new Mochila(3, 10), 2);
        IndiceEspacialPersonajes<Aliado> indice =
                new IndiceEspacialPersonajes<>(List.of(lejano, cercano));

        assertEquals(cercano, indice.masCercano(new Posicion(2, 2), aliado -> true));
        assertEquals(List.of(cercano), indice.cercanos(
                new Posicion(2, 2), 2, aliado -> aliado.getSalud() > 0));
        assertNull(indice.masCercano(new Posicion(2, 2), 0, aliado -> true));
        assertThrows(IllegalArgumentException.class,
                () -> indice.cercanos(new Posicion(2, 2), -1, aliado -> true));
    }
}
