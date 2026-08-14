package com.legendoftecla.tools;

import com.legendoftecla.engine.MotorPartida;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Jugador;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Botiquin;
import com.legendoftecla.model.items.Municion;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.items.ToritoRojo;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.validation.Validaciones;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Politica autonoma conservadora que juega mediante los mismos comandos que una
 * persona. Se utiliza para medir partidas completas sin atajos de simulacion.
 */
public final class JugadorAutomatico {
    private boolean formacionElegida;

    /**
     * Decide una accion reproducible a partir exclusivamente del estado visible
     * y del inventario del jugador.
     *
     * @param motor motor real de la partida
     * @return comando valido para {@link MotorPartida#ejecutarComando(String)}
     */
    public String decidir(MotorPartida motor) {
        MotorPartida validado = Validaciones.noNulo(motor, "Motor de partida");
        Juego juego = validado.getJuego();
        Jugador jugador = juego.getJugador();

        if (!formacionElegida && !juego.getAliados().isEmpty()) {
            formacionElegida = true;
            return "reagrupar defensiva";
        }

        Objeto botiquin = primerObjeto(jugador, Botiquin.class);
        if (botiquin != null && jugador.getSalud() * 100 <= jugador.getSaludMaxima() * 55) {
            return "usar " + botiquin.getNombre();
        }
        Objeto torito = primerObjeto(jugador, ToritoRojo.class);
        int reservaEnergia = Math.max(jugador.estimarCosteMovimiento() * 4,
                jugador.getEnergiaMaxima() / 4);
        if (torito != null && jugador.getEnergia() <= reservaEnergia) {
            return "usar " + torito.getNombre();
        }

        String ataque = ataqueVisible(juego, validado.getEnemigosVisibles());
        if (ataque != null) {
            return ataque;
        }
        if (debeRecargar(jugador)) {
            return "recargar";
        }

        Posicion actual = jugador.getPosicion();
        List<Objeto> objetos = juego.getMapa().getCelda(actual).getObjetos();
        if (!objetos.isEmpty() && !juego.isCeldaInspeccionada(actual)) {
            return "mirar";
        }
        Objeto recogible = objetos.stream()
                .filter(objeto -> objeto instanceof Botiquin
                        || objeto instanceof ToritoRojo
                        || objeto instanceof Municion)
                .filter(jugador::puedeCoger)
                .findFirst()
                .orElse(null);
        if (recogible != null) {
            return "coger " + recogible.getNombre();
        }

        if (actual.equals(juego.getMapa().getObjetivo())) {
            return "descansar";
        }
        if (jugador.getEnergia() < jugador.estimarCosteMovimiento() * 2) {
            return "descansar";
        }
        Direccion direccion = primerPaso(juego.getMapa(), actual,
                juego.getMapa().getObjetivo());
        return direccion == null ? "descansar" : "mover " + token(direccion);
    }

    private Objeto primerObjeto(Jugador jugador, Class<? extends Objeto> tipo) {
        return jugador.getMochila().getObjetos().stream()
                .filter(tipo::isInstance)
                .findFirst()
                .orElse(null);
    }

    private boolean debeRecargar(Jugador jugador) {
        for (Arma arma : jugador.getArmasEquipadas()) {
            if (arma.usaMunicionInfinita()
                    || arma.getMunicionActual() >= arma.getCapacidadCargador()) {
                continue;
            }
            boolean compatible = jugador.getMochila().getObjetos().stream()
                    .filter(Municion.class::isInstance)
                    .map(Municion.class::cast)
                    .anyMatch(municion -> municion.getCantidad() > 0
                            && municion.getTipo() == arma.getTipoMunicion());
            if (compatible) {
                return true;
            }
        }
        return false;
    }

    private String ataqueVisible(Juego juego, Set<Posicion> visibles) {
        Posicion origen = juego.getJugador().getPosicion();
        Enemigo objetivo = juego.getEnemigos().stream()
                .filter(enemigo -> enemigo.getSalud() > 0)
                .filter(enemigo -> visibles.contains(enemigo.getPosicion()))
                .filter(enemigo -> alineado(origen, enemigo.getPosicion()))
                .filter(enemigo -> juego.getMapa().hayLineaAtaque(origen, enemigo.getPosicion()))
                .filter(enemigo -> juego.getJugador().puedeAtacarA(
                        origen.distanciaManhattan(enemigo.getPosicion())))
                .min((primero, segundo) -> Integer.compare(
                        origen.distanciaManhattan(primero.getPosicion()),
                        origen.distanciaManhattan(segundo.getPosicion())))
                .orElse(null);
        if (objetivo == null) {
            return null;
        }
        Posicion destino = objetivo.getPosicion();
        int distancia = origen.distanciaManhattan(destino);
        if (distancia == 0) {
            return "atacar";
        }
        Direccion direccion;
        if (destino.getFila() < origen.getFila()) direccion = Direccion.NORTE;
        else if (destino.getFila() > origen.getFila()) direccion = Direccion.SUR;
        else if (destino.getColumna() < origen.getColumna()) direccion = Direccion.OESTE;
        else direccion = Direccion.ESTE;
        return "atacar " + distancia + token(direccion);
    }

    private boolean alineado(Posicion origen, Posicion destino) {
        return origen.getFila() == destino.getFila()
                || origen.getColumna() == destino.getColumna();
    }

    private Direccion primerPaso(Mapa mapa, Posicion origen, Posicion destino) {
        if (origen.equals(destino)) return null;
        ArrayDeque<Posicion> pendientes = new ArrayDeque<>();
        Set<Posicion> visitadas = new HashSet<>();
        Map<Posicion, Direccion> primerPaso = new HashMap<>();
        pendientes.add(origen);
        visitadas.add(origen);
        while (!pendientes.isEmpty()) {
            Posicion actual = pendientes.removeFirst();
            for (Direccion direccion : Direccion.values()) {
                Posicion siguiente = actual.mover(direccion);
                if (!mapa.estaDentro(siguiente) || !mapa.esTransitable(siguiente)
                        || !visitadas.add(siguiente)) {
                    continue;
                }
                Direccion inicial = actual.equals(origen)
                        ? direccion : primerPaso.get(actual);
                primerPaso.put(siguiente, inicial);
                if (siguiente.equals(destino)) {
                    return inicial;
                }
                pendientes.addLast(siguiente);
            }
        }
        return null;
    }

    private String token(Direccion direccion) {
        return switch (direccion) {
            case NORTE -> "n";
            case SUR -> "s";
            case ESTE -> "e";
            case OESTE -> "o";
        };
    }
}
