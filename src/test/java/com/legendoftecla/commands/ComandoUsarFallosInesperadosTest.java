package com.legendoftecla.commands;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.events.ObjetoUsado;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.exceptions.JuegoException;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.items.Botiquin;
import com.legendoftecla.model.items.Componente;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.world.Juego;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** El uso fallido no debe perder objetos ni reordenar el inventario. */
class ComandoUsarFallosInesperadosTest {
    @Test
    void errorRuntimeDuranteElUsoRestauraObjetoEnSuIndice() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Objeto primero = new Componente("Primero", "Prueba", 1);
        Objeto fallido = new ObjetoDePrueba("Error", TipoFallo.RUNTIME);
        Objeto ultimo = new Componente("Ultimo", "Prueba", 1);
        juego.getJugador().getMochila().setObjetos(List.of(primero, fallido, ultimo));

        assertThrows(IllegalStateException.class,
                () -> new ComandoUsar(new CommandContext(juego), "Error").ejecutar());

        assertSame(primero, juego.getJugador().getMochila().getObjetos().get(0));
        assertSame(fallido, juego.getJugador().getMochila().getObjetos().get(1));
        assertSame(ultimo, juego.getJugador().getMochila().getObjetos().get(2));
    }

    @Test
    void excepcionComprobadaSeConvierteEnErrorDeComandoSinPerderObjeto() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Objeto fallido = new ObjetoDePrueba("Error", TipoFallo.JUEGO);
        juego.getJugador().getMochila().guardar(fallido);

        ComandoException error = assertThrows(ComandoException.class,
                () -> new ComandoUsar(new CommandContext(juego), "Error").ejecutar());

        assertTrue(error.getMessage().contains("efecto fallido"));
        assertSame(fallido, juego.getJugador().getMochila().getObjetos().get(0));
    }

    @Test
    void falloAlDeterminarSiEsConsumibleRestauraElObjeto() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Objeto fallido = new ObjetoDePrueba("Error", TipoFallo.CONSUMIBLE);
        juego.getJugador().getMochila().guardar(fallido);

        assertThrows(IllegalStateException.class,
                () -> new ComandoUsar(new CommandContext(juego), "Error").ejecutar());

        assertSame(fallido, juego.getJugador().getMochila().getObjetos().get(0));
    }

    @Test
    void falloEnUnListenerTrasElUsoNoDuplicaUnConsumible() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        juego.getJugador().setSalud(70);
        Botiquin botiquin = new Botiquin("Cura", "Un solo uso", 1, 15);
        juego.getJugador().getMochila().guardar(botiquin);
        juego.getBusEventos().suscribir(ObjetoUsado.class,
                evento -> { throw new AssertionError("Fallo posterior al consumo"); });

        assertThrows(AssertionError.class,
                () -> new ComandoUsar(new CommandContext(juego), "Cura").ejecutar());

        assertEquals(85, juego.getJugador().getSalud());
        assertTrue(juego.getJugador().getMochila().getObjetos().isEmpty());
    }

    private enum TipoFallo { RUNTIME, JUEGO, CONSUMIBLE }

    private static final class ObjetoDePrueba extends Objeto {
        private final TipoFallo fallo;

        ObjetoDePrueba(String nombre, TipoFallo fallo) {
            super(nombre, "Objeto de prueba", 1);
            this.fallo = fallo;
        }

        @Override
        public void usar(Personaje personaje) throws JuegoException {
            if (fallo == TipoFallo.RUNTIME) {
                throw new IllegalStateException("Efecto fallido");
            }
            if (fallo == TipoFallo.JUEGO) {
                throw new JuegoException("efecto fallido");
            }
        }

        @Override
        public boolean isConsumible() {
            if (fallo == TipoFallo.CONSUMIBLE) {
                throw new IllegalStateException("Clasificacion fallida");
            }
            return true;
        }
    }
}
