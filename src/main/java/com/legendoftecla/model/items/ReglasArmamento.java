package com.legendoftecla.model.items;

import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Berserker;
import com.legendoftecla.model.characters.Commander;
import com.legendoftecla.model.characters.CommanderPrime;
import com.legendoftecla.model.characters.Francotirador;
import com.legendoftecla.model.characters.HeavyFloater;
import com.legendoftecla.model.characters.Marine;
import com.legendoftecla.model.characters.Medic;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.characters.Pyro;
import com.legendoftecla.model.characters.PyroOverlord;
import com.legendoftecla.model.characters.Scout;
import com.legendoftecla.model.characters.Sniper;
import com.legendoftecla.model.characters.Zapador;

import java.util.EnumSet;
import java.util.Set;

/** Tabla central y testeable de competencias de jugador, aliados y enemigos. */
public final class ReglasArmamento {
    private static final Set<TipoMunicion> FUEGO_COMUN = EnumSet.of(
            TipoMunicion.PISTOLA, TipoMunicion.SUBFUSIL, TipoMunicion.ESCOPETA,
            TipoMunicion.RIFLE, TipoMunicion.ENERGIA);
    private static final Set<TipoMunicion> FUEGO_COMPLETO = EnumSet.of(
            TipoMunicion.PISTOLA, TipoMunicion.SUBFUSIL, TipoMunicion.ESCOPETA,
            TipoMunicion.RIFLE, TipoMunicion.PESADA,
            TipoMunicion.COHETE, TipoMunicion.ENERGIA);

    private ReglasArmamento() { }

    /** @return perfil inmutable correspondiente al rol concreto */
    public static PerfilArmamento perfil(Personaje personaje) {
        if (personaje instanceof Marine) {
            return perfil(EnumSet.of(CategoriaArma.MELE, CategoriaArma.ARROJADIZA,
                    CategoriaArma.FUEGO), FUEGO_COMPLETO, true, false);
        }
        if (personaje instanceof Francotirador) {
            return perfil(EnumSet.of(CategoriaArma.ARROJADIZA, CategoriaArma.ARCO,
                    CategoriaArma.BALLESTA, CategoriaArma.FUEGO),
                    unir(FUEGO_COMUN, TipoMunicion.CUCHILLO_ARROJADIZO,
                            TipoMunicion.FLECHA, TipoMunicion.VIROTE), true, false);
        }
        if (personaje instanceof Zapador) {
            return perfil(EnumSet.of(CategoriaArma.MELE, CategoriaArma.ARROJADIZA,
                    CategoriaArma.FUEGO),
                    unir(FUEGO_COMPLETO, TipoMunicion.CUCHILLO_ARROJADIZO),
                    true, true);
        }
        if (personaje instanceof Aliado) {
            return perfil(EnumSet.allOf(CategoriaArma.class),
                    unir(FUEGO_COMUN, TipoMunicion.CUCHILLO_ARROJADIZO,
                            TipoMunicion.FLECHA, TipoMunicion.VIROTE), true, false);
        }
        if (personaje instanceof Berserker) {
            return perfil(EnumSet.of(CategoriaArma.MELE), Set.of(), false, false);
        }
        if (personaje instanceof Sniper) {
            return perfil(EnumSet.of(CategoriaArma.BALLESTA, CategoriaArma.FUEGO),
                    unir(FUEGO_COMUN, TipoMunicion.VIROTE), true, false);
        }
        if (personaje instanceof Medic || personaje instanceof Scout) {
            return perfil(EnumSet.of(CategoriaArma.ARROJADIZA, CategoriaArma.FUEGO),
                    unir(FUEGO_COMUN, TipoMunicion.CUCHILLO_ARROJADIZO), true, false);
        }
        if (personaje instanceof CommanderPrime || personaje instanceof PyroOverlord
                || personaje instanceof Commander || personaje instanceof Pyro
                || personaje instanceof HeavyFloater) {
            return perfil(EnumSet.of(CategoriaArma.MELE, CategoriaArma.FUEGO),
                    FUEGO_COMPLETO, true, false);
        }
        return perfil(EnumSet.of(CategoriaArma.MELE, CategoriaArma.FUEGO),
                FUEGO_COMUN, true, false);
    }

    private static PerfilArmamento perfil(Set<CategoriaArma> categorias,
            Set<TipoMunicion> municiones, boolean granadas, boolean demolicion) {
        return new PerfilArmamento(categorias, municiones, granadas, demolicion);
    }

    private static Set<TipoMunicion> unir(Set<TipoMunicion> base,
            TipoMunicion... adicionales) {
        EnumSet<TipoMunicion> resultado = EnumSet.copyOf(base);
        java.util.Collections.addAll(resultado, adicionales);
        return resultado;
    }
}
