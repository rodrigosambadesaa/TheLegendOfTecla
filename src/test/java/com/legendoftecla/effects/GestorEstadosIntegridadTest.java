package com.legendoftecla.effects;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.characters.Personaje;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GestorEstadosIntegridadTest {
    @Test
    void efectoQueFallaNoQuedaRegistradoEnElPersonaje() {
        var personaje = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        GestorEstados gestor = personaje.getEstados();
        EfectoEstado defectuoso = new EfectoEstado() {
            @Override public TipoEstado tipo() { return TipoEstado.ASUSTADO; }
            @Override public int duracionInicial() { return 3; }
            @Override public void alAplicar(Personaje destino) {
                throw new IllegalStateException("Aplicacion rechazada");
            }
        };

        assertThrows(IllegalStateException.class, () -> gestor.aplicar(defectuoso));

        assertFalse(gestor.contiene(TipoEstado.ASUSTADO));
        assertTrue(gestor.getActivos().isEmpty());
        assertTrue(gestor.aplicar(new Asustado()));
        assertTrue(gestor.contiene(TipoEstado.ASUSTADO));
    }

    @Test
    void snapshotConTiposDuplicadosNoDestruyeEstadosPrevios() {
        var personaje = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        GestorEstados gestor = personaje.getEstados();
        gestor.aplicar(new Inspirado());
        List<EstadoActivo> anteriores = gestor.getActivos();

        List<EstadoActivo> duplicados = List.of(
                new EstadoActivo(TipoEstado.QUEMADO, 3, 1),
                new EstadoActivo(TipoEstado.ASUSTADO, 2, 1),
                new EstadoActivo(TipoEstado.QUEMADO, 1, 4));

        assertThrows(IllegalArgumentException.class,
                () -> gestor.restaurar(duplicados));
        assertEquals(anteriores, gestor.getActivos());
        assertTrue(gestor.contiene(TipoEstado.INSPIRADO));
        assertFalse(gestor.contiene(TipoEstado.QUEMADO));
    }

    @Test
    void restauracionCorrectaConservaTurnosYAcumulaciones() {
        var personaje = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        GestorEstados gestor = personaje.getEstados();
        gestor.aplicar(new Quemado());
        List<EstadoActivo> nuevos = List.of(
                new EstadoActivo(TipoEstado.ASUSTADO, 6, 2),
                new EstadoActivo(TipoEstado.MOJADO, 2, 1),
                new EstadoActivo(TipoEstado.INSPIRADO, 1, 1));

        gestor.restaurar(nuevos);

        assertEquals(3, gestor.getActivos().size());
        assertTrue(gestor.getActivos().containsAll(nuevos));
        assertFalse(gestor.contiene(TipoEstado.QUEMADO));
        assertTrue(gestor.contiene(TipoEstado.ASUSTADO));
    }

    @Test
    void snapshotNuloNoEliminaLosEstadosExistentes() {
        var personaje = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        GestorEstados gestor = personaje.getEstados();
        gestor.aplicar(new Inspirado());

        assertThrows(NullPointerException.class,
                () -> gestor.restaurar((List<EstadoActivo>) null));

        assertEquals(1, gestor.getActivos().size());
        assertTrue(gestor.contiene(TipoEstado.INSPIRADO));
    }
}
