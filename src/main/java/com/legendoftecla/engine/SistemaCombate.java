package com.legendoftecla.engine;

import com.legendoftecla.audio.EventoSonido;
import com.legendoftecla.audio.GestorSonido;
import com.legendoftecla.console.TipoMensaje;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Jugador;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.world.Juego;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Unifica daño, registro, sonido y efectos ambientales de todos los combatientes. */
public final class SistemaCombate {
    private SistemaCombate() { }

    public static ResultadoAtaque atacar(Juego juego, Personaje atacante, Personaje objetivo, Random random) {
        int vidaAntes = objetivo.getSalud();
        GestorSonido.reproducir(EventoSonido.ATAQUE, atacante.getPosicion(), juego.getJugador().getPosicion());
        atacante.atacar(objetivo);
        ResultadoAtaque resultado = resultado(atacante, objetivo, vidaAntes);
        informar(juego, resultado, objetivo);
        SistemaIncendios.intentarDerribarAntorcha(juego, objetivo.getPosicion(), random);
        return resultado;
    }

    public static List<ResultadoAtaque> atacarTodos(Juego juego, Personaje atacante,
            List<? extends Personaje> objetivos, Random random) {
        List<Integer> vidas = objetivos.stream().map(Personaje::getSalud).toList();
        GestorSonido.reproducir(EventoSonido.ATAQUE, atacante.getPosicion(), juego.getJugador().getPosicion());
        atacante.atacar(objetivos);
        List<ResultadoAtaque> resultados = new ArrayList<>();
        for (int i = 0; i < objetivos.size(); i++) {
            Personaje objetivo = objetivos.get(i);
            ResultadoAtaque resultado = resultado(atacante, objetivo, vidas.get(i));
            resultados.add(resultado);
            informar(juego, resultado, objetivo);
        }
        if (!objetivos.isEmpty()) {
            SistemaIncendios.intentarDerribarAntorcha(juego, objetivos.get(0).getPosicion(), random);
        }
        return List.copyOf(resultados);
    }

    private static ResultadoAtaque resultado(Personaje atacante, Personaje objetivo, int vidaAntes) {
        return new ResultadoAtaque(atacante.getNombre(), objetivo.getNombre(),
                Math.max(0, vidaAntes - objetivo.getSalud()), objetivo.getSalud(),
                objetivo.getSaludMaxima(), vidaAntes > 0 && objetivo.getSalud() <= 0);
    }

    private static void informar(Juego juego, ResultadoAtaque resultado, Personaje objetivo) {
        String texto = resultado.atacante() + " ataca a " + resultado.objetivo()
                + ": quita " + resultado.vidaQuitada() + " de vida; quedan "
                + resultado.vidaRestante() + "/" + resultado.vidaMaxima() + ".";
        juego.getConsola().imprimir(texto, resultado.mortal() ? TipoMensaje.EXITO : TipoMensaje.INFO);
        GestorSonido.reproducir(EventoSonido.DANIO, objetivo.getPosicion(), juego.getJugador().getPosicion());
        if (resultado.mortal()) {
            EventoSonido muerte = objetivo instanceof Jugador ? EventoSonido.MUERTE_JUGADOR
                    : objetivo instanceof Aliado ? EventoSonido.MUERTE_ALIADO : EventoSonido.MUERTE_ENEMIGO;
            GestorSonido.reproducir(muerte, objetivo.getPosicion(), juego.getJugador().getPosicion());
            juego.getConsola().imprimir(resultado.objetivo() + " muere.", TipoMensaje.ADVERTENCIA);
        }
    }
}
