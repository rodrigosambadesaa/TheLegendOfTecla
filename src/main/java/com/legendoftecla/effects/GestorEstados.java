package com.legendoftecla.effects;

import com.legendoftecla.events.BusEventos;
import com.legendoftecla.events.EstadoAplicado;
import com.legendoftecla.events.EstadoEliminado;
import com.legendoftecla.events.PersonajeDanado;
import com.legendoftecla.events.PersonajeMuerto;
import com.legendoftecla.model.characters.Personaje;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Ciclo de vida de estados de un personaje, independiente del motor. */
public final class GestorEstados {
    private final Personaje personaje;
    private final Map<TipoEstado, Aplicacion> activos = new EnumMap<>(TipoEstado.class);
    private BusEventos eventos;

    /** @param personaje propietario no nulo */
    public GestorEstados(Personaje personaje) {
        this.personaje = Objects.requireNonNull(personaje, "Personaje");
    }

    /** @return personaje propietario del gestor */
    public Personaje getPersonaje() {
        return personaje;
    }

    /** @return bus de observabilidad opcional */
    public BusEventos getBusEventos() {
        return eventos;
    }

    /** @param eventos bus opcional usado para observabilidad */
    public void setBusEventos(BusEventos eventos) {
        this.eventos = eventos;
    }

    /** Aplica, acumula o renueva el efecto segun su estrategia. */
    public boolean aplicar(EfectoEstado efecto) {
        Objects.requireNonNull(efecto, "Efecto");
        if (efecto.duracionInicial() < 1) {
            throw new IllegalArgumentException("La duracion del estado debe ser positiva.");
        }
        if (efecto.tipo() == TipoEstado.QUEMADO && contiene(TipoEstado.MOJADO)) {
            return false;
        }
        Aplicacion actual = activos.get(efecto.tipo());
        if (actual == null) {
            activos.put(efecto.tipo(), new Aplicacion(efecto,
                    efecto.duracionInicial(), 1));
            efecto.alAplicar(personaje);
            publicarAplicado(efecto.tipo());
            return true;
        }
        int acumulaciones = actual.acumulaciones()
                + (efecto.acumulable() ? 1 : 0);
        int duracion = efecto.renuevaDuracion()
                ? Math.max(actual.turnos(), efecto.duracionInicial()) : actual.turnos();
        activos.put(efecto.tipo(), new Aplicacion(efecto, duracion, acumulaciones));
        return efecto.acumulable() || efecto.renuevaDuracion();
    }

    /** Apaga fuego antes de aplicar proteccion humeda. */
    public void mojar(int turnos) {
        eliminar(TipoEstado.QUEMADO);
        aplicar(new Mojado(turnos));
    }

    /** Ejecuta efectos de comienzo de turno. */
    public void inicioTurno() {
        for (Aplicacion aplicacion : instantanea()) {
            ejecutarConDanioObservable(aplicacion,
                    () -> aplicacion.efecto().alInicioTurno(
                            personaje, aplicacion.acumulaciones()));
            eliminarSiCorresponde(aplicacion);
        }
    }

    /** Ejecuta hooks, descuenta duracion y elimina caducados. */
    public void finTurno() {
        for (Aplicacion aplicacion : instantanea()) {
            ejecutarConDanioObservable(aplicacion,
                    () -> aplicacion.efecto().alFinTurno(
                            personaje, aplicacion.acumulaciones()));
            int restantes = aplicacion.turnos() - 1;
            if (restantes <= 0 || aplicacion.efecto().debeEliminarse(personaje)) {
                eliminar(aplicacion.efecto().tipo());
            } else if (activos.containsKey(aplicacion.efecto().tipo())) {
                activos.put(aplicacion.efecto().tipo(), aplicacion.conTurnos(restantes));
            }
        }
    }

    /** Notifica el movimiento a sangrados y otros efectos reactivos. */
    public void alMover() {
        for (Aplicacion aplicacion : instantanea()) {
            ejecutarConDanioObservable(aplicacion,
                    () -> aplicacion.efecto().alMover(personaje, aplicacion.acumulaciones()));
            eliminarSiCorresponde(aplicacion);
        }
    }

    /** Consume un bloqueo como una accion perdida. */
    public boolean consumirBloqueoAccion() {
        for (Aplicacion aplicacion : instantanea()) {
            if (aplicacion.efecto().bloqueaAccion()) {
                eliminar(aplicacion.efecto().tipo());
                return true;
            }
        }
        return false;
    }

    /** @return producto de modificadores de precision */
    public double multiplicadorPrecision() {
        return activos.values().stream().map(Aplicacion::efecto)
                .mapToDouble(EfectoEstado::multiplicadorPrecision)
                .reduce(1.0, (a, b) -> a * b);
    }

    /** @return producto de modificadores de alcance visual */
    public double multiplicadorVision() {
        return activos.values().stream().map(Aplicacion::efecto)
                .mapToDouble(EfectoEstado::multiplicadorVision)
                .reduce(1.0, (a, b) -> a * b);
    }

    /** @return si el tipo esta activo */
    public boolean contiene(TipoEstado tipo) {
        return activos.containsKey(Objects.requireNonNull(tipo, "Tipo"));
    }

    /** Elimina explicitamente un estado. */
    public boolean eliminar(TipoEstado tipo) {
        Aplicacion eliminada = activos.remove(Objects.requireNonNull(tipo, "Tipo"));
        if (eliminada == null) {
            return false;
        }
        eliminada.efecto().alEliminar(personaje);
        if (eventos != null) {
            eventos.publicar(new EstadoEliminado(eventos.ahora(),
                    personaje.getNombre(), tipo.name()));
        }
        return true;
    }

    /** Descanso completo elimina el agotamiento. */
    public void descansar() {
        eliminar(TipoEstado.EXHAUSTO);
    }

    /** @return instantanea inmutable adecuada para GUI y persistencia */
    public List<EstadoActivo> getActivos() {
        return activos.values().stream().map(aplicacion -> new EstadoActivo(
                aplicacion.efecto().tipo(), aplicacion.turnos(),
                aplicacion.acumulaciones())).toList();
    }

    /** Restaura una aplicacion ya validada desde una partida versionada. */
    public void restaurar(EfectoEstado efecto, int turnos, int acumulaciones) {
        Objects.requireNonNull(efecto, "Efecto");
        if (turnos < 1 || acumulaciones < 1) {
            throw new IllegalArgumentException("Estado persistido invalido");
        }
        activos.put(efecto.tipo(), new Aplicacion(efecto, turnos, acumulaciones));
    }

    /** Sustituye el estado interno desde una vista persistida validada. */
    public void restaurar(List<EstadoActivo> estados) {
        activos.clear();
        for (EstadoActivo estado : List.copyOf(Objects.requireNonNull(estados, "Estados"))) {
            EfectoEstado efecto = crear(estado.tipo());
            activos.put(estado.tipo(), new Aplicacion(efecto,
                    estado.turnosRestantes(), estado.acumulaciones()));
            efecto.alAplicar(personaje);
        }
    }

    private List<Aplicacion> instantanea() {
        return new ArrayList<>(activos.values());
    }

    private void publicarAplicado(TipoEstado tipo) {
        if (eventos != null) {
            eventos.publicar(new EstadoAplicado(eventos.ahora(),
                    personaje.getNombre(), tipo.name()));
        }
    }

    private void eliminarSiCorresponde(Aplicacion aplicacion) {
        if (aplicacion.efecto().debeEliminarse(personaje)) {
            eliminar(aplicacion.efecto().tipo());
        }
    }

    private void ejecutarConDanioObservable(Aplicacion aplicacion, Runnable accion) {
        int saludAntes = personaje.getSalud();
        accion.run();
        int danio = saludAntes - personaje.getSalud();
        if (eventos != null && danio > 0) {
            eventos.publicar(new PersonajeDanado(eventos.ahora(), personaje.getNombre(),
                    danio, personaje.getPosicion()));
            if (saludAntes > 0 && personaje.getSalud() <= 0) {
                eventos.publicar(new PersonajeMuerto(eventos.ahora(), personaje.getNombre(),
                        personaje.getPosicion()));
            }
        }
    }

    private EfectoEstado crear(TipoEstado tipo) {
        return switch (tipo) {
            case QUEMADO -> new Quemado();
            case ENVENENADO -> new Envenenado();
            case SANGRADO -> new Sangrado();
            case ATURDIDO -> new Aturdido();
            case CEGADO -> new Cegado();
            case MOJADO -> new Mojado();
            case EXHAUSTO -> new Exhausto();
            case ASUSTADO -> new Asustado();
            case INSPIRADO -> new Inspirado();
        };
    }

    private record Aplicacion(EfectoEstado efecto, int turnos, int acumulaciones) {
        private Aplicacion conTurnos(int nuevosTurnos) {
            return new Aplicacion(efecto, nuevosTurnos, acumulaciones);
        }
    }
}
