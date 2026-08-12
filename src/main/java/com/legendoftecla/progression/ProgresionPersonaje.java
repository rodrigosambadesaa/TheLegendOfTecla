package com.legendoftecla.progression;
import com.legendoftecla.model.characters.Jugador;
import java.util.LinkedHashSet;
import java.util.Set;
/** XP, niveles y desbloqueos persistibles del jugador. */
public final class ProgresionPersonaje {
    private int nivel = 1;
    private int experiencia;
    private final Set<String> desbloqueadas = new LinkedHashSet<>();
    public int getNivel() { return nivel; }
    public int getExperiencia() { return experiencia; }
    public Set<String> getDesbloqueadas() { return Set.copyOf(desbloqueadas); }
    public int experienciaSiguienteNivel() { return nivel * 100; }
    public int ganarExperiencia(int cantidad) {
        if (cantidad < 0) throw new IllegalArgumentException("XP negativa");
        experiencia += cantidad;
        int subidas = 0;
        while (experiencia >= experienciaSiguienteNivel()) {
            experiencia -= experienciaSiguienteNivel(); nivel++; subidas++;
        }
        return subidas;
    }
    public boolean desbloquear(String id, ArbolHabilidades arbol, Jugador jugador) {
        Habilidad habilidad = arbol.buscar(id);
        if (habilidad == null || desbloqueadas.contains(id)) return false;
        RequisitoHabilidad requisito = habilidad.requisito();
        if (nivel < requisito.nivelMinimo() || requisito.habilidadPrevia() != null
                && !desbloqueadas.contains(requisito.habilidadPrevia())) return false;
        desbloqueadas.add(id);
        habilidad.efecto().aplicar(jugador);
        return true;
    }

    /** Restaura un snapshot validado sin volver a aplicar efectos ya reflejados en el personaje. */
    public void restaurar(int nivel, int experiencia, Set<String> habilidades) {
        if (nivel < 1 || experiencia < 0 || experiencia >= nivel * 100) {
            throw new IllegalArgumentException("Progresion guardada invalida");
        }
        this.nivel = nivel;
        this.experiencia = experiencia;
        desbloqueadas.clear();
        if (habilidades != null) {
            habilidades.stream().filter(java.util.Objects::nonNull)
                    .filter(id -> !id.isBlank()).forEach(desbloqueadas::add);
        }
    }
}
