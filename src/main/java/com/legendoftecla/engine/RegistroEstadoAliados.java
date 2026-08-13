package com.legendoftecla.engine;

import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.SistemaPuntuacion;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Mantiene el estado efimero de los aliados con actualizaciones O(1).
 * Evita copiar mapas completos por cada combatiente y centraliza su presentacion.
 */
public final class RegistroEstadoAliados {
    private final Map<Aliado, SituacionAliado> situaciones = new HashMap<>();
    private final Map<Aliado, Boolean> combates = new HashMap<>();

    /** Reinicia el registro para los participantes indicados. */
    public void inicializar(List<Aliado> aliados) {
        Validaciones.noNulo(aliados, "Aliados");
        situaciones.clear();
        combates.clear();
        aliados.forEach(aliado -> {
            Aliado validado = Validaciones.noNulo(aliado, "Aliado");
            situaciones.put(validado, SituacionAliado.ACTIVO);
            combates.put(validado, false);
        });
    }

    /** @return copia inmutable de situaciones */
    public Map<Aliado, SituacionAliado> getSituaciones() {
        return Map.copyOf(situaciones);
    }

    /** Sustituye las situaciones tras validar nulidad y limite. */
    public void setSituaciones(Map<Aliado, SituacionAliado> nuevas) {
        validar(nuevas, "Situaciones aliadas");
        situaciones.clear();
        situaciones.putAll(nuevas);
    }

    /** @return copia inmutable de indicadores de combate */
    public Map<Aliado, Boolean> getCombates() {
        return Map.copyOf(combates);
    }

    /** Sustituye los indicadores de combate tras validarlos. */
    public void setCombates(Map<Aliado, Boolean> nuevos) {
        validar(nuevos, "Combates aliados");
        combates.clear();
        combates.putAll(nuevos);
    }

    /** Actualiza una situacion sin copiar el registro completo. */
    public void cambiar(Aliado aliado, SituacionAliado situacion) {
        situaciones.put(Validaciones.noNulo(aliado, "Aliado"),
                Validaciones.noNulo(situacion, "Situacion aliada"));
    }

    /** Actualiza el indicador de combate sin copiar el registro completo. */
    public void marcarCombate(Aliado aliado, boolean enCombate) {
        combates.put(Validaciones.noNulo(aliado, "Aliado"), enCombate);
    }

    /** Calcula el estado efectivo, incluyendo evacuacion y caida. */
    public SituacionAliado situacion(Juego juego, Aliado aliado) {
        if (juego.estaAliadoExtraido(aliado)) return SituacionAliado.EVACUADO;
        if (aliado.getSalud() <= 0) return SituacionAliado.CAIDO;
        return situaciones.getOrDefault(aliado, SituacionAliado.ACTIVO);
    }

    /** Indica si el aliado permanece activo y marcado en combate. */
    public boolean estaEnCombate(Juego juego, Aliado aliado) {
        return aliado.getSalud() > 0 && !juego.estaAliadoExtraido(aliado)
                && combates.getOrDefault(aliado, false);
    }

    /** Genera el informe compartido por consola y GUI. */
    public String resumen(Juego juego) {
        List<Aliado> aliados = juego.getAliadosRegistrados();
        if (aliados.isEmpty()) return "Aliados: ninguno.";
        long evacuados = aliados.stream().filter(juego::estaAliadoExtraido).count();
        long caidos = aliados.stream().filter(aliado -> aliado.getSalud() <= 0).count();
        long enCombate = aliados.stream().filter(aliado -> estaEnCombate(juego, aliado)).count();
        int puntuacion = aliados.stream().mapToInt(aliado ->
                SistemaPuntuacion.calcularAliado(juego, aliado).total()).sum();
        StringJoiner lineas = new StringJoiner("\n");
        lineas.add("ALIADOS " + aliados.size() + " | activos="
                + (aliados.size() - evacuados - caidos) + " | en combate=" + enCombate
                + " | evacuados=" + evacuados + " | caidos=" + caidos
                + " | puntuacion total=" + puntuacion);
        aliados.forEach(aliado -> agregarDetalle(lineas, juego, aliado));
        return lineas.toString();
    }

    private void agregarDetalle(StringJoiner lineas, Juego juego, Aliado aliado) {
        String posicion = juego.estaAliadoExtraido(aliado)
                ? "salida " + aliado.getPosicion() : aliado.getPosicion().toString();
        lineas.add("- " + aliado.getNombre() + " | Estado "
                + situacion(juego, aliado).getEtiqueta() + " | Combate "
                + (estaEnCombate(juego, aliado) ? "EN COMBATE" : "FUERA DE COMBATE")
                + " | Nivel " + aliado.getNivel() + " | Rol " + aliado.getRol().getEtiqueta()
                + " | Vida " + aliado.getSalud() + "/" + aliado.getSaludMaxima()
                + " | Energia " + aliado.getEnergia() + "/" + aliado.getEnergiaMaxima()
                + " | Puntuacion " + SistemaPuntuacion.calcularAliado(juego, aliado).total()
                + " | Posicion " + posicion + " | Efectos " + SistemaEstados.resumen(aliado));
        lineas.add("  Objetos: " + objetos(aliado) + " | Equipo: " + equipo(aliado));
    }

    private String objetos(Aliado aliado) {
        if (aliado.getMochila().getObjetos().isEmpty()) return "ninguno";
        return aliado.getMochila().getObjetos().stream().map(Objeto::getNombre)
                .reduce((primero, segundo) -> primero + ", " + segundo).orElse("ninguno");
    }

    private String equipo(Aliado aliado) {
        List<String> nombres = new ArrayList<>();
        aliado.getArmasEquipadas().forEach(arma -> nombres.add("arma " + arma.getNombre()));
        if (aliado.getArmaduraEquipada() != null) {
            nombres.add("armadura " + aliado.getArmaduraEquipada().getNombre());
        }
        if (aliado.getBinocularEquipado() != null) {
            nombres.add("binocular " + aliado.getBinocularEquipado().getNombre());
        }
        return nombres.isEmpty() ? "ninguno" : String.join(", ", nombres);
    }

    private void validar(Map<?, ?> mapa, String campo) {
        Validaciones.noNulo(mapa, campo);
        if (mapa.size() > Limites.ESTADISTICA || mapa.entrySet().stream()
                .anyMatch(entrada -> entrada.getKey() == null || entrada.getValue() == null)) {
            throw new IllegalArgumentException(campo + " no es valido.");
        }
    }
}
