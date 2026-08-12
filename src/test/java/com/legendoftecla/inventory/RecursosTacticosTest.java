package com.legendoftecla.inventory;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Botiquin;
import com.legendoftecla.model.items.Municion;
import com.legendoftecla.model.items.TipoMunicion;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecursosTacticosTest {
    @Test
    void cargadorVacioParcialCompletoEIncompatible() {
        Arma rifle = new Arma("Rifle", "Servicio", 3, 12, true,
                TipoMunicion.RIFLE, 5, 0);
        Municion balas = new Municion("Balas", 1, TipoMunicion.RIFLE, 3);
        Municion pistola = new Municion("9mm", 1, TipoMunicion.PISTOLA, 8);

        assertFalse(rifle.puedeDisparar());
        assertEquals(0, rifle.recargar(pistola));
        assertEquals(3, rifle.recargar(balas));
        assertEquals(0, balas.getCantidad());
        assertTrue(rifle.consumirDisparo());
        assertEquals(2, rifle.getMunicionActual());
        Municion masBalas = new Municion("Balas", 1, TipoMunicion.RIFLE, 10);
        assertEquals(3, rifle.recargar(masBalas));
        assertEquals(5, rifle.getMunicionActual());
        assertEquals(7, masBalas.getCantidad());
    }

    @Test
    void unArmaFinitaNoAtacaSinMunicionYLaLegacyDeclaraInfinita() {
        Personaje jugador = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        Personaje objetivo = new Aliado("A", new Posicion(0, 1), new Mochila(3, 10), 2);
        Arma vacia = new Arma("Pistola", "", 1, 10, false,
                TipoMunicion.PISTOLA, 6, 0);
        jugador.setArmasEquipadas(List.of(vacia));

        assertThrows(IllegalStateException.class, () -> jugador.atacar(objetivo));
        Arma legacy = new Arma("Clasica", "", 1, 5, false);
        assertTrue(legacy.usaMunicionInfinita());
        assertTrue(legacy.consumirDisparo());
    }

    @Test
    void darYPedirRespetanDistanciaCombateYCapacidad() throws Exception {
        Personaje jugador = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        Aliado aliado = new Aliado("A", new Posicion(0, 1), new Mochila(3, 10), 2);
        Botiquin botiquin = new Botiquin("Botiquin", "", 1, 5);
        jugador.getMochila().guardar(botiquin);
        ServicioIntercambio servicio = new ServicioIntercambio(1);

        assertEquals(botiquin, servicio.dar(jugador, aliado, "Botiquin", false));
        assertEquals(botiquin, servicio.pedir(jugador, aliado, "Botiquin", false));
        assertThrows(AccionInvalidaException.class,
                () -> servicio.dar(jugador, aliado, "Botiquin", true));
        aliado.setPosicion(new Posicion(2, 2));
        assertThrows(AccionInvalidaException.class,
                () -> servicio.dar(jugador, aliado, "Botiquin", false));
    }

    @Test
    void craftingConsumeIngredientesUnaVezYEsTransaccional() {
        Mochila mochila = new Mochila(4, 20);
        mochila.guardar(new Botiquin("Vendas", "", 1, 1));
        mochila.guardar(new Botiquin("Medicamento", "", 1, 1));
        SistemaFabricacion sistema = new SistemaFabricacion();
        sistema.registrar(new Receta("botiquin", List.of(
                new Ingrediente("Vendas", 1), new Ingrediente("Medicamento", 1)),
                () -> new Botiquin("Botiquin", "Fabricado", 1, 15)));

        ResultadoFabricacion resultado = sistema.fabricar("botiquin", mochila);
        assertTrue(resultado.exito());
        assertEquals(1, mochila.getObjetos().size());
        assertFalse(sistema.fabricar("botiquin", mochila).exito());
        assertFalse(sistema.fabricar("desconocido", mochila).exito());
    }

    @Test
    void craftingAgrupaIngredientesRepetidosSinConsumirParcialmente() {
        Mochila mochila = new Mochila(3, 10);
        mochila.guardar(new Botiquin("Pieza", "", 1, 1));
        SistemaFabricacion sistema = new SistemaFabricacion();
        sistema.registrar(new Receta("doble", List.of(
                new Ingrediente("Pieza", 1), new Ingrediente("pieza", 1)),
                () -> new Botiquin("Resultado", "", 1, 1)));

        assertFalse(sistema.fabricar("doble", mochila).exito());
        assertEquals(1, mochila.getObjetos().size());
        assertThrows(IllegalArgumentException.class,
                () -> sistema.registrar(new Receta("DOBLE", List.of(
                        new Ingrediente("Pieza", 1)),
                        () -> new Botiquin("Otro", "", 1, 1))));
    }
}
