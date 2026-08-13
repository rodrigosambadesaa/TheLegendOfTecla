package com.legendoftecla.ai;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Scout;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class EscalabilidadPercepcionTest {
    @Test
    void ruidoDePartidaSoloVisitaCeldasDentroDelRadio() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Scout cercano = scout("Cercano", new Posicion(0, 1));
        Scout lejano = scout("Lejano", new Posicion(2, 2));
        registrar(juego, cercano);
        registrar(juego, lejano);

        try (SistemaRuido ruido = new SistemaRuido(juego)) {
            ruido.generar(new Posicion(0, 0), FuenteRuido.CAMINAR);
        }

        assertEquals(NivelAlerta.INVESTIGANDO, cercano.getControladorIA().getEstado());
        assertEquals(NivelAlerta.PATRULLA, lejano.getControladorIA().getEstado());
    }

    @Test
    void percepcionMasivaConsultaElIndiceDeCeldas() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        for (int indice = 0; indice < 129; indice++) {
            Aliado aliado = new Aliado("A" + indice, new Posicion(2, 2),
                    new Mochila(1, 1), 2);
            juego.agregarAliado(aliado);
            juego.getMapa().getCelda(aliado.getPosicion()).agregarAliado(aliado);
        }
        Scout scout = scout("Observador", new Posicion(1, 2));

        ContextoIA percepcion = new PercepcionIA().percibir(juego, scout);

        assertSame(juego.getAliados().get(0), percepcion.objetivo());
    }

    private Scout scout(String nombre, Posicion posicion) {
        return new Scout(nombre, posicion, new Mochila(2, 10), 4);
    }

    private void registrar(Juego juego, Scout enemigo) {
        juego.agregarEnemigo(enemigo);
        juego.getMapa().getCelda(enemigo.getPosicion()).agregarEnemigo(enemigo);
    }
}
