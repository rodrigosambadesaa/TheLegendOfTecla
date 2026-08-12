package com.legendoftecla.achievements;

import com.legendoftecla.events.BusEventos;
import com.legendoftecla.events.EventoJuego;
import com.legendoftecla.events.Suscripcion;
import com.legendoftecla.stats.EstadisticasPartida;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Evalua logros tras eventos sin acoplarlos al motor. */
public final class GestorLogros implements AutoCloseable {
    private final EstadisticasPartida estadisticas;
    private final Map<String, Logro> catalogo = new LinkedHashMap<>();
    private final Set<String> desbloqueados = new LinkedHashSet<>();
    private final Suscripcion suscripcion;
    public GestorLogros(BusEventos bus, EstadisticasPartida estadisticas) {
        this.estadisticas = estadisticas;
        registrarPredeterminados();
        suscripcion = bus.suscribir(EventoJuego.class, evento -> evaluar());
    }
    public void registrar(Logro logro) { catalogo.put(logro.id(), logro); }
    public List<Logro> evaluar() {
        return catalogo.values().stream().filter(logro -> !desbloqueados.contains(logro.id()))
                .filter(logro -> logro.condicion().test(estadisticas)).peek(logro ->
                        desbloqueados.add(logro.id())).toList();
    }
    public Set<String> getDesbloqueados() { return Set.copyOf(desbloqueados); }
    /** Restaura IDs conocidos; los desconocidos se conservan para compatibilidad futura. */
    public void restaurar(Set<String> ids) {
        desbloqueados.clear();
        if (ids != null) ids.stream().filter(java.util.Objects::nonNull)
                .filter(id -> !id.isBlank()).forEach(desbloqueados::add);
    }
    private void registrarPredeterminados() {
        registrar(new Logro("primer-contacto", "Primer contacto", "Realiza un ataque",
                e -> e.getDisparos() >= 1));
        registrar(new Logro("todos-a-casa", "Todos a casa", "Evacua un aliado",
                e -> e.getAliadosEvacuados() >= 1));
        registrar(new Logro("bombero", "Bombero", "Apaga un incendio",
                e -> e.getIncendiosApagados() >= 1));
        registrar(new Logro("explorador", "Explorador", "Inspecciona diez celdas",
                e -> e.getCeldasInspeccionadas() >= 10));
        registrar(new Logro("zapador-experto", "Zapador experto", "Desactiva cinco trampas",
                e -> e.getTrampasDesactivadas() >= 5));
    }
    @Override public void close() { suscripcion.close(); }
}
