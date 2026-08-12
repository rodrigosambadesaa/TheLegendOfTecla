package com.legendoftecla.audio;

import com.legendoftecla.events.IncendioExtinguido;
import com.legendoftecla.events.ArmaRecargada;
import com.legendoftecla.events.IncendioIniciado;
import com.legendoftecla.events.IncendioPropagado;
import com.legendoftecla.events.ObjetoTirado;
import com.legendoftecla.events.ObjetoUsado;
import com.legendoftecla.events.PersonajeAtacado;
import com.legendoftecla.events.PersonajeDanado;
import com.legendoftecla.events.PersonajeMovido;
import com.legendoftecla.events.PersonajeMuerto;
import com.legendoftecla.events.PuertaAbierta;
import com.legendoftecla.events.PuertaCerrada;
import com.legendoftecla.events.TrampaActivada;
import com.legendoftecla.events.TrampaDetectada;
import com.legendoftecla.events.MisionCompletada;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.validation.Validaciones;

/** Traduce eventos de dominio a audio posicional sin contaminar el motor. */
public final class SuscriptorAudioEventos {
    private SuscriptorAudioEventos() { }

    /** Registra el adaptador de produccion en el bus de una partida. */
    public static void registrar(Juego juego) {
        registrar(juego, GestorSonido::reproducir);
    }

    /**
     * Registra un puerto de audio sustituible en pruebas.
     *
     * @param juego partida que origina los eventos
     * @param reproductor puerto de salida
     */
    public static void registrar(Juego juego, ReproductorSonido reproductor) {
        Juego partida = Validaciones.noNulo(juego, "Juego");
        ReproductorSonido salida = Validaciones.noNulo(reproductor, "Reproductor");
        partida.getBusEventos().suscribir(PersonajeMovido.class,
                evento -> sonar(salida, partida, EventoSonido.MOVIMIENTO, evento.destino()));
        partida.getBusEventos().suscribir(PersonajeAtacado.class,
                evento -> sonar(salida, partida, EventoSonido.ATAQUE, evento.origen()));
        partida.getBusEventos().suscribir(PersonajeDanado.class,
                evento -> sonar(salida, partida, EventoSonido.DANIO, evento.posicion()));
        partida.getBusEventos().suscribir(PersonajeMuerto.class,
                evento -> sonar(salida, partida, sonidoMuerte(partida, evento.personaje()),
                        evento.posicion()));
        partida.getBusEventos().suscribir(ObjetoTirado.class,
                evento -> sonar(salida, partida, EventoSonido.TIRAR, evento.posicion()));
        partida.getBusEventos().suscribir(IncendioIniciado.class,
                evento -> sonar(salida, partida, EventoSonido.INCENDIO, evento.posicion()));
        partida.getBusEventos().suscribir(IncendioPropagado.class,
                evento -> sonar(salida, partida, EventoSonido.INCENDIO, evento.destino()));
        partida.getBusEventos().suscribir(IncendioExtinguido.class,
                evento -> sonar(salida, partida, EventoSonido.APAGAR_FUEGO, evento.posicion()));
        partida.getBusEventos().suscribir(PuertaAbierta.class,
                evento -> sonar(salida, partida, EventoSonido.PUERTA,
                        partida.getJugador().getPosicion()));
        partida.getBusEventos().suscribir(PuertaCerrada.class,
                evento -> sonar(salida, partida, EventoSonido.PUERTA,
                        partida.getJugador().getPosicion()));
        partida.getBusEventos().suscribir(TrampaActivada.class,
                evento -> sonar(salida, partida,
                        evento.trampa().toLowerCase().contains("alarma")
                                ? EventoSonido.ALARMA : EventoSonido.TRAMPA,
                        partida.getJugador().getPosicion()));
        partida.getBusEventos().suscribir(TrampaDetectada.class,
                evento -> sonar(salida, partida, EventoSonido.DESCUBRIMIENTO,
                        partida.getJugador().getPosicion()));
        partida.getBusEventos().suscribir(MisionCompletada.class,
                evento -> sonar(salida, partida, EventoSonido.MISION,
                        partida.getJugador().getPosicion()));
        partida.getBusEventos().suscribir(ArmaRecargada.class,
                evento -> sonar(salida, partida, EventoSonido.RECARGA,
                        evento.posicion()));
        partida.getBusEventos().suscribir(ObjetoUsado.class, evento -> {
            String objeto = evento.objeto().toLowerCase();
            if (objeto.contains("agua") || objeto.contains("cubo")) {
                sonar(salida, partida, EventoSonido.AGUA, evento.posicion());
            }
        });
    }

    private static EventoSonido sonidoMuerte(Juego juego, String nombre) {
        if (juego.getJugador().getNombre().equals(nombre)) {
            return EventoSonido.MUERTE_JUGADOR;
        }
        boolean esAliado = juego.getAliadosRegistrados().stream()
                .anyMatch(aliado -> aliado.getNombre().equals(nombre));
        return esAliado ? EventoSonido.MUERTE_ALIADO : EventoSonido.MUERTE_ENEMIGO;
    }

    private static void sonar(ReproductorSonido reproductor, Juego juego,
            EventoSonido sonido, Posicion origen) {
        reproductor.reproducir(sonido, origen, juego.getJugador().getPosicion());
    }
}
