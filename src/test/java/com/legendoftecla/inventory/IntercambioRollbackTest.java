package com.legendoftecla.inventory;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.items.Componente;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IntercambioRollbackTest {
    @Test
    void falloPorObjetoInexistenteRestauraContenidoYOrden() {
        Personaje primero = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        Aliado segundo = new Aliado("Comerciante", new Posicion(0, 1), new Mochila(4, 20), 2);
        Objeto uno = pieza("Uno", 1);
        Objeto dos = pieza("Dos", 1);
        Objeto tres = pieza("Tres", 1);
        Objeto otro = pieza("Otro", 1);
        primero.getMochila().setObjetos(List.of(uno, dos, tres));
        segundo.getMochila().setObjetos(List.of(otro));

        assertThrows(AccionInvalidaException.class, () ->
                new ServicioIntercambio(1).intercambiar(
                        primero, "Dos", segundo, "Inexistente", false));

        assertEquals(List.of(uno, dos, tres), primero.getMochila().getObjetos());
        assertEquals(List.of(otro), segundo.getMochila().getObjetos());
        assertSame(dos, primero.getMochila().getObjetos().get(1));
    }

    @Test
    void falloPorPesoRestauraAmbosInventariosYReferencias() {
        Personaje primero = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        Aliado segundo = new Aliado("Comerciante", new Posicion(0, 1), new Mochila(3, 20), 2);
        Objeto uno = pieza("Uno", 1);
        Objeto dos = pieza("Dos", 1);
        Objeto pesado = pieza("Pesado", 19);
        Objeto ligero = pieza("Ligero", 1);
        primero.getMochila().setObjetos(List.of(uno, dos));
        segundo.getMochila().setObjetos(List.of(pesado, ligero));

        assertThrows(AccionInvalidaException.class, () ->
                new ServicioIntercambio(1).intercambiar(
                        primero, "Uno", segundo, "Pesado", false));

        assertEquals(List.of(uno, dos), primero.getMochila().getObjetos());
        assertEquals(List.of(pesado, ligero), segundo.getMochila().getObjetos());
        assertSame(uno, primero.getMochila().getObjetos().get(0));
        assertSame(pesado, segundo.getMochila().getObjetos().get(0));
    }

    @Test
    void intercambioValidoTransfiereSinDuplicarObjetos() throws Exception {
        Personaje primero = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        Aliado segundo = new Aliado("Comerciante", new Posicion(0, 1), new Mochila(3, 10), 2);
        Objeto uno = pieza("Uno", 1);
        Objeto dos = pieza("Dos", 1);
        Objeto tres = pieza("Tres", 1);
        Objeto otro = pieza("Otro", 1);
        primero.getMochila().setObjetos(List.of(uno, dos, tres));
        segundo.getMochila().setObjetos(List.of(otro));

        new ServicioIntercambio(1).intercambiar(primero, "Dos", segundo, "Otro", false);

        assertEquals(List.of(uno, tres, otro), primero.getMochila().getObjetos());
        assertEquals(List.of(dos), segundo.getMochila().getObjetos());
        assertSame(otro, primero.getMochila().getObjetos().get(2));
        assertSame(dos, segundo.getMochila().getObjetos().get(0));
    }

    private static Componente pieza(String nombre, double peso) {
        return new Componente(nombre, "Prueba", peso);
    }
}
