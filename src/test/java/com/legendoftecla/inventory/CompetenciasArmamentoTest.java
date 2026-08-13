package com.legendoftecla.inventory;

import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.engine.ArsenalEnemigo;
import com.legendoftecla.engine.ServicioBotinEnemigo;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Berserker;
import com.legendoftecla.model.characters.Commander;
import com.legendoftecla.model.characters.CommanderPrime;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Francotirador;
import com.legendoftecla.model.characters.Marine;
import com.legendoftecla.model.characters.Medic;
import com.legendoftecla.model.characters.LightFloater;
import com.legendoftecla.model.characters.HeavyFloater;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Pyro;
import com.legendoftecla.model.characters.PyroOverlord;
import com.legendoftecla.model.characters.Scout;
import com.legendoftecla.model.characters.Sniper;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.characters.Zapador;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Armeria;
import com.legendoftecla.model.items.Explosivo;
import com.legendoftecla.model.items.FaccionEquipo;
import com.legendoftecla.model.items.TipoMunicion;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompetenciasArmamentoTest {
    private static final Posicion POSICION = new Posicion(1, 1);

    @ParameterizedTest
    @MethodSource("enemigosArmados")
    void cadaEnemigoYJefeRecibeUnArmaQueSabeUsar(
            Supplier<Enemigo> fabrica) {
        Enemigo enemigo = fabrica.get();

        ArsenalEnemigo.asignar(enemigo, Dificultad.NORMAL);

        assertEquals(1, enemigo.getArmasEquipadas().size());
        Arma arma = enemigo.getArmasEquipadas().get(0);
        assertTrue(enemigo.puedeUsar(arma));
        assertEquals(FaccionEquipo.ENEMIGA, arma.getFaccion());
        assertTrue(arma.puedeDisparar());
        assertEquals(FaccionEquipo.ENEMIGA,
                enemigo.getArmaduraEquipada().getFaccion());
        if (!arma.usaMunicionInfinita()) {
            assertTrue(enemigo.getMochila().getObjetos().stream()
                    .anyMatch(objeto -> objeto instanceof com.legendoftecla.model.items.Municion));
        }
    }

    @Test
    void jugadorYAliadosSoloEquipanFamiliasQueDominan() throws Exception {
        Marine marine = new Marine("M", POSICION, new Mochila(4, 30), 3);
        Francotirador francotirador = new Francotirador(
                "F", POSICION, new Mochila(4, 30), 3);
        Zapador zapador = new Zapador("Z", POSICION, new Mochila(4, 30), 3);
        Aliado aliado = new Aliado("A", POSICION, new Mochila(4, 30), 3);
        Arma pesada = Armeria.pesada("Pesada", 4, 4);
        Arma arco = Armeria.arco("Arco", 2);

        marine.equipar(pesada);
        francotirador.equipar(arco);
        zapador.equipar(Armeria.pesada("Pesada Z", 4, 4));
        aliado.equipar(Armeria.arco("Arco A", 2));

        assertThrows(com.legendoftecla.exceptions.AccionInvalidaException.class,
                () -> new Marine("M2", POSICION, new Mochila(4, 30), 3)
                        .equipar(Armeria.arco("Arco prohibido", 1)));
        assertFalse(francotirador.puedeUsar(pesada));
        assertFalse(aliado.puedeUsar(pesada));
        assertTrue(zapador.getPerfilArmamento().permiteDemolicion());
        zapador.coger(new Explosivo("C4", "", 1));
        assertThrows(com.legendoftecla.exceptions.AccionInvalidaException.class,
                () -> aliado.coger(new Explosivo("C4 aliado", "", 1)));
    }

    @Test
    void botinDepositaArmaYReservaUnaSolaVez() {
        Sniper sniper = new Sniper("S", POSICION, new Mochila(3, 10), 5);
        ArsenalEnemigo.asignar(sniper, Dificultad.DIFICIL);
        Celda celda = new Celda("Botin", true);

        int primero = ServicioBotinEnemigo.soltar(celda, sniper);
        int segundo = ServicioBotinEnemigo.soltar(celda, sniper);

        assertEquals(3, primero);
        assertEquals(0, segundo);
        assertEquals(3, celda.getObjetos().size());
        assertTrue(celda.getObjetos().stream().anyMatch(Arma.class::isInstance));
        assertTrue(celda.getObjetos().stream()
                .anyMatch(com.legendoftecla.model.items.Armadura.class::isInstance));
        assertTrue(celda.getObjetos().stream().anyMatch(objeto ->
                objeto instanceof com.legendoftecla.model.items.Municion municion
                        && municion.getTipo() == TipoMunicion.RIFLE));
    }

    @Test
    void ningunBandoPuedeUsarElEquipoPropioDelContrario() throws Exception {
        Sniper enemigo = new Sniper("S", POSICION, new Mochila(3, 20), 5);
        ArsenalEnemigo.asignar(enemigo, Dificultad.NORMAL);
        Marine marine = new Marine("M", POSICION, new Mochila(5, 40), 4);
        Arma xeno = enemigo.getArmasEquipadas().get(0);
        var armaduraXeno = enemigo.getArmaduraEquipada();
        Arma humana = Armeria.rifle("Rifle humano", 4, 4);

        assertFalse(marine.puedeUsar(xeno));
        assertFalse(marine.puedeUsar(armaduraXeno));
        assertFalse(marine.puedeCoger(xeno));
        assertThrows(com.legendoftecla.exceptions.AccionInvalidaException.class,
                () -> marine.equipar(xeno));
        assertFalse(enemigo.puedeUsar(humana));
        assertThrows(com.legendoftecla.exceptions.AccionInvalidaException.class,
                () -> enemigo.equipar(humana));
    }

    private static Stream<Arguments> enemigosArmados() {
        return Stream.of(
                Arguments.of((Supplier<Enemigo>) () -> new Sectoid(
                        "SE", POSICION, new Mochila(3, 10), 3)),
                Arguments.of((Supplier<Enemigo>) () -> new LightFloater(
                        "LF", POSICION, new Mochila(3, 10), 3)),
                Arguments.of((Supplier<Enemigo>) () -> new HeavyFloater(
                        "HF", POSICION, new Mochila(3, 10), 3)),
                Arguments.of((Supplier<Enemigo>) () -> new Berserker(
                        "B", POSICION, new Mochila(3, 10), 3)),
                Arguments.of((Supplier<Enemigo>) () -> new Medic(
                        "M", POSICION, new Mochila(3, 10), 3)),
                Arguments.of((Supplier<Enemigo>) () -> new Sniper(
                        "N", POSICION, new Mochila(3, 10), 6)),
                Arguments.of((Supplier<Enemigo>) () -> new Pyro(
                        "P", POSICION, new Mochila(3, 10), 4)),
                Arguments.of((Supplier<Enemigo>) () -> new Scout(
                        "S", POSICION, new Mochila(3, 10), 6)),
                Arguments.of((Supplier<Enemigo>) () -> new Commander(
                        "C", POSICION, new Mochila(3, 10), 5)),
                Arguments.of((Supplier<Enemigo>) () -> new CommanderPrime(
                        "CP", POSICION, new Mochila(3, 10), 5)),
                Arguments.of((Supplier<Enemigo>) () -> new PyroOverlord(
                        "PO", POSICION, new Mochila(3, 10), 5)));
    }
}
