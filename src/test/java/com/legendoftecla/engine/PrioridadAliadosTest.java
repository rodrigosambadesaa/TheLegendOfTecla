package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.RolAliado;
import com.legendoftecla.model.items.Botiquin;
import com.legendoftecla.model.items.ToritoRojo;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrioridadAliadosTest {
    @Test
    void ayudarAlJugadorTienePrioridadSobreExplorar() throws Exception {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Aliado aliado = agregarAliadoEnInicio(juego);
        aliado.getMochila().guardar(new Botiquin("apoyo", "Cura al jugador", 1, 25));
        juego.getJugador().recibirDanio(20);
        Posicion inicio = juego.getMapa().getInicio();

        new MotorPartida(juego).ejecutarComando("mirar");

        assertEquals(juego.getJugador().getSaludMaxima(), juego.getJugador().getSalud());
        assertEquals(inicio, aliado.getPosicion());
    }

    @Test
    void sinAyudaPendienteElAliadoExploraUnaCeldaTransitable() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Aliado aliado = agregarAliadoEnInicio(juego);
        Posicion inicio = juego.getMapa().getInicio();

        new MotorPartida(juego).ejecutarComando("mirar");

        assertNotEquals(inicio, aliado.getPosicion());
        assertTrue(juego.getMapa().esTransitable(aliado.getPosicion()));
    }

    @Test
    void elMedicoPriorizaBotiquinesYToritosConocidos() throws Exception {
        for (boolean buscarBotiquin : new boolean[] {true, false}) {
            Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
            Aliado medico = agregarAliadoEnInicio(juego);
            medico.setRol(RolAliado.MEDICO);
            Posicion suministro = new Posicion(0, 1);
            if (buscarBotiquin) {
                juego.getMapa().getCelda(suministro).agregarObjeto(
                        new Botiquin("botiquin medico", "Reserva", 1, 25));
            } else {
                medico.getMochila().guardar(new Botiquin("reserva 1", "Reserva", 1, 10));
                medico.getMochila().guardar(new Botiquin("reserva 2", "Reserva", 1, 10));
                juego.getMapa().getCelda(suministro).agregarObjeto(
                        new ToritoRojo("torito medico", "Reserva", 1, 25));
            }
            juego.setCeldasInspeccionadasAliados(
                    java.util.Map.of(medico, java.util.Set.of(
                            juego.getMapa().getInicio(), suministro)));

            MotorPartida motor = new MotorPartida(juego);
            motor.ejecutarComando("mirar");

            assertEquals(suministro, medico.getPosicion());
            assertTrue(motor.getEstadoAliados().contains("Rol Medico"));
        }
    }

    private Aliado agregarAliadoEnInicio(Juego juego) {
        Posicion inicio = juego.getMapa().getInicio();
        Aliado aliado = new Aliado("Apoyo", inicio, new Mochila(6, 30), 3);
        juego.agregarAliado(aliado);
        juego.getMapa().getCelda(inicio).agregarAliado(aliado);
        return aliado;
    }
}
