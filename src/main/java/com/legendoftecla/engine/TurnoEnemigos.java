package com.legendoftecla.engine;

import com.legendoftecla.ai.EnemigoTactico;
import com.legendoftecla.ai.SistemaTurnosIA;
import com.legendoftecla.audio.EventoSonido;
import com.legendoftecla.audio.GestorSonido;
import com.legendoftecla.constants.FormacionAliada;
import com.legendoftecla.events.ArmaRecargada;
import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.inventory.ServicioRecarga;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Jefe;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.validation.Validaciones;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/** Ejecuta el turno enemigo sin mezclarlo con comandos ni cooperacion aliada. */
public final class TurnoEnemigos {
    private TurnoEnemigos() { }

    /** Ejecuta una accion determinista por enemigo vivo. */
    public static void ejecutar(Juego juego, SistemaTurnosIA sistemaIA,
                                Random random, boolean jugadorDescansando) {
        Validaciones.noNulo(juego, "Juego");
        Validaciones.noNulo(sistemaIA, "Sistema IA");
        Validaciones.noNulo(random, "Generador aleatorio");
        IndiceEspacialPersonajes<Aliado> indiceAliados =
                new IndiceEspacialPersonajes<>(juego.getAliados());
        CacheNavegacion cacheNavegacion = new CacheNavegacion(juego.getMapa());
        boolean hayAliadosVivos = indiceAliados.alguno(aliado -> aliado.getSalud() > 0);
        sistemaIA.prepararTurno(juego);
        try {
            for (Enemigo enemigo : List.copyOf(juego.getEnemigos())) {
                ejecutarEnemigo(juego, sistemaIA, random, jugadorDescansando,
                        enemigo, indiceAliados, hayAliadosVivos, cacheNavegacion);
            }
        } finally {
            sistemaIA.finalizarTurno();
        }
    }

    private static void ejecutarEnemigo(Juego juego, SistemaTurnosIA sistemaIA,
                                        Random random, boolean descansando, Enemigo enemigo,
                                        IndiceEspacialPersonajes<Aliado> indiceAliados,
                                        boolean hayAliadosVivos,
                                        CacheNavegacion cacheNavegacion) {
        if (enemigo.getSalud() <= 0 || enemigo.getEstados().consumirBloqueoAccion()) return;
        if (enemigo instanceof EnemigoTactico || enemigo instanceof Jefe) {
            sistemaIA.ejecutar(juego, enemigo, random);
            return;
        }
        boolean formacionDetectada = detectaFormacion(
                juego, enemigo, indiceAliados, hayAliadosVivos);
        Personaje objetivoTactico = formacionDetectada
                ? seleccionarObjetivo(juego, enemigo, indiceAliados) : null;
        if (formacionDetectada) anunciarDeteccion(juego, enemigo);
        int movimientos = descansando || formacionDetectada
                ? Math.max(1, random.nextInt(3)) : random.nextInt(3);
        for (int indice = 0; indice < movimientos; indice++) {
            Personaje objetivo = objetivoTactico == null
                    ? objetivoPredeterminado(juego, enemigo, indiceAliados) : objetivoTactico;
            if (objetivo == null) return;
            if (puedeDisparar(juego, enemigo, objetivo)) {
                if (!enemigo.puedeAtacar()) recargar(juego, enemigo);
                else SistemaCombate.atacar(juego, enemigo, objetivo, random);
                return;
            }
            Direccion direccion = descansando || formacionDetectada
                    ? cacheNavegacion.primerPaso(enemigo.getPosicion(), objetivo.getPosicion())
                    : Direccion.values()[random.nextInt(Direccion.values().length)];
            if (direccion == null) return;
            mover(juego, enemigo, direccion, descansando, formacionDetectada);
        }
    }

    private static void anunciarDeteccion(Juego juego, Enemigo enemigo) {
        String despliegue = juego.getFormacionAliada() == FormacionAliada.SIN_FORMACION
                ? "el escuadron aliado" : "la formacion "
                        + juego.getFormacionAliada().getEtiqueta();
        juego.getConsola().imprimirInfo(enemigo.getNombre() + " detecta "
                + despliegue + " y coordina su ataque.");
    }

    private static boolean puedeDisparar(Juego juego, Enemigo enemigo, Personaje objetivo) {
        return enemigo.getPosicion().distanciaManhattan(objetivo.getPosicion())
                <= enemigo.getRangoVision() && juego.getMapa().hayLineaAtaque(
                        enemigo.getPosicion(), objetivo.getPosicion());
    }

    private static void mover(Juego juego, Enemigo enemigo, Direccion direccion,
                              boolean descansando, boolean formacionDetectada) {
        Posicion origen = enemigo.getPosicion();
        if (!juego.getMapa().esTransitable(origen.mover(direccion))) return;
        juego.getMapa().getCelda(origen).quitarEnemigo(enemigo);
        try {
            enemigo.mover(direccion, juego);
            juego.getMapa().getCelda(enemigo.getPosicion()).agregarEnemigo(enemigo);
            GestorSonido.reproducir(EventoSonido.MOVIMIENTO,
                    enemigo.getPosicion(), juego.getJugador().getPosicion());
            if (descansando && !formacionDetectada) {
                juego.getConsola().imprimirInfo(
                        enemigo.getNombre() + " se acerca mientras descansas.");
            }
        } catch (Exception ignorada) {
            juego.getMapa().getCelda(origen).agregarEnemigo(enemigo);
        }
    }

    private static boolean detectaFormacion(Juego juego, Enemigo enemigo,
                                            IndiceEspacialPersonajes<Aliado> indiceAliados,
                                            boolean hayAliadosVivos) {
        if (!hayAliadosVivos) return false;
        if (ve(juego, enemigo, juego.getJugador())) return true;
        return !indiceAliados.cercanos(enemigo.getPosicion(), enemigo.getRangoVision(),
                aliado -> ve(juego, enemigo, aliado)).isEmpty();
    }

    private static boolean ve(Juego juego, Enemigo enemigo, Personaje personaje) {
        return personaje.getSalud() > 0 && enemigo.getPosicion().distanciaManhattan(
                personaje.getPosicion()) <= enemigo.getRangoVision()
                && juego.getMapa().hayLineaAtaque(enemigo.getPosicion(), personaje.getPosicion());
    }

    private static Personaje seleccionarObjetivo(Juego juego, Enemigo enemigo,
                                                 IndiceEspacialPersonajes<Aliado> indiceAliados) {
        if (juego.getFormacionAliada() == FormacionAliada.DEFENSIVA) {
            List<Personaje> visibles = new ArrayList<>();
            if (ve(juego, enemigo, juego.getJugador())) visibles.add(juego.getJugador());
            indiceAliados.cercanos(enemigo.getPosicion(), enemigo.getRangoVision(),
                            aliado -> ve(juego, enemigo, aliado)).stream()
                    .forEach(visibles::add);
            if (visibles.isEmpty()) return objetivoPredeterminado(juego, enemigo, indiceAliados);
            return visibles.stream().min(Comparator.comparingDouble(personaje ->
                    (double) personaje.getSalud() / Math.max(1, personaje.getSaludMaxima())))
                    .orElseThrow();
        }
        Personaje jugador = ve(juego, enemigo, juego.getJugador()) ? juego.getJugador() : null;
        Aliado aliado = indiceAliados.masCercano(enemigo.getPosicion(),
                enemigo.getRangoVision(),
                candidato -> ve(juego, enemigo, candidato));
        if (jugador == null && aliado == null) return objetivoPredeterminado(juego, enemigo, indiceAliados);
        if (jugador == null) return aliado;
        if (aliado == null) return jugador;
        int distanciaJugador = enemigo.getPosicion().distanciaManhattan(jugador.getPosicion());
        int distanciaAliado = enemigo.getPosicion().distanciaManhattan(aliado.getPosicion());
        return distanciaJugador <= distanciaAliado ? jugador : aliado;
    }

    private static Personaje objetivoPredeterminado(Juego juego, Enemigo enemigo,
                                                  IndiceEspacialPersonajes<Aliado> indiceAliados) {
        if (juego.getJugador().getSalud() > 0) return juego.getJugador();
        return indiceAliados.masCercano(enemigo.getPosicion(), aliado -> aliado.getSalud() > 0);
    }

    private static boolean recargar(Juego juego, Enemigo enemigo) {
        try {
            var resultado = new ServicioRecarga().recargar(enemigo, null);
            juego.getConsola().imprimirInfo(enemigo.getNombre() + " recarga "
                    + resultado.arma().getNombre() + " (" + resultado.cantidad() + ").");
            juego.publicarEvento(new ArmaRecargada(juego.getBusEventos().ahora(),
                    enemigo.getNombre(), resultado.arma().getNombre(), resultado.cantidad(),
                    enemigo.getPosicion()));
            return true;
        } catch (AccionInvalidaException error) {
            return false;
        }
    }
}
