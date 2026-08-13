package com.legendoftecla.ai;

import com.legendoftecla.events.RuidoGenerado;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.world.Posicion;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/** Maquina State/Strategy determinista con memoria temporal. */
public final class ControladorIA {
    private final Enemigo enemigo;
    private final Map<NivelAlerta, EstadoIA> estrategias = new EnumMap<>(NivelAlerta.class);
    private NivelAlerta estado = NivelAlerta.PATRULLA;
    private Posicion ultimaPosicionConocida;
    private int turnosMemoria;

    public ControladorIA(Enemigo enemigo) {
        this.enemigo = Objects.requireNonNull(enemigo, "Enemigo");
        registrarEstrategias();
    }

    /** Percibe un ruido cuando cae dentro del alcance auditivo. */
    public boolean percibir(RuidoGenerado ruido) {
        int distancia = enemigo.getPosicion().distanciaManhattan(ruido.origen());
        if (distancia > Math.min(enemigo.getRangoAudicion(), ruido.intensidad())) return false;
        ultimaPosicionConocida = ruido.origen();
        turnosMemoria = Math.max(2, ruido.intensidad() / 2);
        estado = ruido.intensidad() >= 8 ? NivelAlerta.ALERTA : NivelAlerta.INVESTIGANDO;
        return true;
    }

    /** Evalua transiciones y delega en la estrategia del estado resultante. */
    public AccionIA decidir(ContextoIA contexto) {
        if (enemigo.getSalud() * 100 / enemigo.getSaludMaxima() < 20) {
            estado = NivelAlerta.HUYENDO;
        } else if (contexto.veJugador()) {
            estado = NivelAlerta.COMBATE;
            ultimaPosicionConocida = contexto.posicionObjetivo();
            turnosMemoria = 3;
        } else if (estado == NivelAlerta.COMBATE) {
            estado = NivelAlerta.BUSQUEDA;
        }
        AccionIA accion = estrategias.get(estado).decidir(contexto);
        envejecerMemoria(contexto.veJugador());
        return accion;
    }

    public NivelAlerta getEstado() { return estado; }
    public void setEstado(NivelAlerta estado) {
        this.estado = Objects.requireNonNull(estado, "Estado");
    }
    public Posicion getUltimaPosicionConocida() { return ultimaPosicionConocida; }
    /** @return turnos durante los que se recuerda el ultimo estimulo */
    public int getTurnosMemoria() { return turnosMemoria; }

    /** Comunica una alerta externa con memoria acotada. */
    public void alertar(Posicion posicion, int turnos) {
        ultimaPosicionConocida = Objects.requireNonNull(posicion, "Posicion");
        turnosMemoria = Math.max(1, turnos);
        estado = NivelAlerta.ALERTA;
    }

    private void registrarEstrategias() {
        estrategias.put(NivelAlerta.PATRULLA, contexto -> accion(
                TipoAccionIA.PATRULLAR, null, "ruta asignada"));
        estrategias.put(NivelAlerta.SOSPECHA, contexto -> accion(
                TipoAccionIA.ESPERAR, ultimaPosicionConocida, "escuchando"));
        estrategias.put(NivelAlerta.INVESTIGANDO, contexto -> accion(
                TipoAccionIA.INVESTIGAR, ultimaPosicionConocida, "ruido detectado"));
        estrategias.put(NivelAlerta.ALERTA, contexto -> accion(
                TipoAccionIA.ALERTAR, ultimaPosicionConocida, "amenaza intensa"));
        estrategias.put(NivelAlerta.COMBATE, contexto -> accion(
                contexto.armaVacia() ? TipoAccionIA.RECARGAR : TipoAccionIA.ATACAR,
                contexto.posicionObjetivo(), "objetivo visible"));
        estrategias.put(NivelAlerta.BUSQUEDA, contexto -> accion(
                TipoAccionIA.BUSCAR, ultimaPosicionConocida, "vision perdida"));
        estrategias.put(NivelAlerta.HUYENDO, contexto -> accion(
                TipoAccionIA.ALEJARSE, contexto.posicionObjetivo(), "salud critica"));
        estrategias.put(NivelAlerta.PROTEGIENDO, contexto -> accion(
                TipoAccionIA.PROTEGER, ultimaPosicionConocida, "proteger objetivo"));
    }

    private AccionIA accion(TipoAccionIA tipo, Posicion objetivo, String motivo) {
        return new AccionIA(tipo, objetivo, motivo);
    }

    private void envejecerMemoria(boolean visionActual) {
        if (visionActual || turnosMemoria <= 0) return;
        turnosMemoria--;
        if (turnosMemoria == 0 && estado != NivelAlerta.HUYENDO) {
            ultimaPosicionConocida = null;
            estado = NivelAlerta.PATRULLA;
        }
    }
}
