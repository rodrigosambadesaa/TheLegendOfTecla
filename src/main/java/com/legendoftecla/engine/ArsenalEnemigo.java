package com.legendoftecla.engine;

import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.model.characters.Berserker;
import com.legendoftecla.model.characters.Commander;
import com.legendoftecla.model.characters.CommanderPrime;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.HeavyFloater;
import com.legendoftecla.model.characters.LightFloater;
import com.legendoftecla.model.characters.Medic;
import com.legendoftecla.model.characters.Pyro;
import com.legendoftecla.model.characters.PyroOverlord;
import com.legendoftecla.model.characters.Scout;
import com.legendoftecla.model.characters.Sniper;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Armadura;
import com.legendoftecla.model.items.CategoriaArma;
import com.legendoftecla.model.items.ClaseArma;
import com.legendoftecla.model.items.FaccionEquipo;
import com.legendoftecla.model.items.Municion;
import com.legendoftecla.model.items.TipoMunicion;

/** Genera armas y protecciones enemigas coherentes con el lore de cada rol. */
public final class ArsenalEnemigo {
    private ArsenalEnemigo() { }

    /** Equipa una carga inicial y guarda una reserva escalada por dificultad. */
    public static void asignar(Enemigo enemigo, Dificultad dificultad) {
        if (enemigo.getArmasEquipadas().isEmpty()) {
            Arma arma = crearArma(enemigo);
            equipar(enemigo, arma);
            agregarReserva(enemigo, arma, dificultad);
        }
        if (enemigo.getArmaduraEquipada() == null) {
            equipar(enemigo, crearArmadura(enemigo));
        }
        if ((enemigo instanceof Pyro || enemigo instanceof PyroOverlord)
                && enemigo.getMochila().getObjetos().stream()
                        .noneMatch(com.legendoftecla.model.items.Granada.class::isInstance)) {
            enemigo.getMochila().guardar(new com.legendoftecla.model.items.Granada(
                    "incendiaria-" + enemigo.getNombre(), "Granada incendiaria", 0.6,
                    com.legendoftecla.model.items.TipoGranada.INCENDIARIA));
        }
    }

    private static void agregarReserva(Enemigo enemigo, Arma arma,
            Dificultad dificultad) {
        if (!arma.usaMunicionInfinita()) {
            int reserva = Math.max(1, (int) Math.round(
                    arma.getCapacidadCargador()
                            * dificultad.getMultiplicadorEnemigos()));
            enemigo.getMochila().guardar(new Municion(
                    "reserva-" + enemigo.getNombre(), peso(arma.getTipoMunicion()),
                    arma.getTipoMunicion(), reserva));
        }
    }

    private static void equipar(Enemigo enemigo,
            com.legendoftecla.model.items.Objeto equipo) {
        try {
            enemigo.equipar(equipo);
        } catch (AccionInvalidaException error) {
            throw new IllegalStateException("Perfil de enemigo incompatible", error);
        }
    }

    private static Arma crearArma(Enemigo enemigo) {
        if (enemigo instanceof Berserker) {
            return arma(enemigo, "garras de asedio", "Implantes oseos de ruptura",
                    4.0, 22, true, CategoriaArma.MELE, TipoMunicion.INFINITA, 0);
        }
        if (enemigo instanceof Sniper) {
            return arma(enemigo, "aguja de vacio", "Rifle xeno de precision silenciosa",
                    3.0, 20, true, CategoriaArma.FUEGO, TipoMunicion.RIFLE, 5);
        }
        if (enemigo instanceof Medic) {
            return arma(enemigo, "bisturi de plasma", "Emisor quirurgico defensivo",
                    1.2, 10, false, CategoriaArma.FUEGO, TipoMunicion.ENERGIA, 6);
        }
        if (enemigo instanceof Scout) {
            return arma(enemigo, "esquirlas rastreadoras", "Proyectiles guiados de explorador",
                    1.0, 9, false, CategoriaArma.ARROJADIZA,
                    TipoMunicion.CUCHILLO_ARROJADIZO, 4);
        }
        if (enemigo instanceof CommanderPrime || enemigo instanceof HeavyFloater) {
            return arma(enemigo, "canon gravitico", "Artilleria xeno integrada en el arnes",
                    7.0, 25, true, CategoriaArma.FUEGO, TipoMunicion.PESADA, 6);
        }
        if (enemigo instanceof Pyro || enemigo instanceof PyroOverlord) {
            return arma(enemigo, "proyector de magma", "Organo termico de negacion de zona",
                    3.5, 19, true, CategoriaArma.FUEGO, TipoMunicion.ENERGIA, 6);
        }
        if (enemigo instanceof Commander) {
            return arma(enemigo, "carabina de mando", "Enlace balistico de la red de combate",
                    3.5, 17, true, CategoriaArma.FUEGO, TipoMunicion.RIFLE, 8);
        }
        if (enemigo instanceof LightFloater) {
            return arma(enemigo, "repetidor ionico", "Emisor ligero para ataques de acoso",
                    2.0, 13, false, CategoriaArma.FUEGO, TipoMunicion.ENERGIA, 7);
        }
        if (enemigo instanceof Sectoid) {
            return arma(enemigo, "canalizador psionico", "Foco neural de hostigamiento",
                    1.5, 12, false, CategoriaArma.FUEGO, TipoMunicion.ENERGIA, 6);
        }
        return arma(enemigo, "emisor xeno", "Tecnologia hostil sin clasificar",
                3.0, 16, true, CategoriaArma.FUEGO, TipoMunicion.ENERGIA, 6);
    }

    private static Arma arma(Enemigo enemigo, String nombre, String descripcion,
            double peso, int danio, boolean dosManos, CategoriaArma categoria,
            TipoMunicion municion, int cargador) {
        int variante = Math.floorMod(enemigo.getNombre().hashCode(), 3);
        String serie = switch (variante) {
            case 0 -> " Alfa";
            case 1 -> " Sigma";
            default -> " Omega";
        };
        int capacidad = municion == TipoMunicion.INFINITA ? 0 : cargador + variante;
        ClaseArma clase = switch (categoria) {
            case MELE -> dosManos ? ClaseArma.ESPADA_DOS_MANOS : ClaseArma.CUCHILLO;
            case ARROJADIZA -> ClaseArma.CUCHILLO_ARROJADIZO;
            case ARCO -> ClaseArma.ARCO;
            case BALLESTA -> ClaseArma.BALLESTA;
            case FUEGO -> switch (municion) {
                case PISTOLA -> ClaseArma.PISTOLA;
                case SUBFUSIL -> ClaseArma.SUBFUSIL;
                case ESCOPETA -> ClaseArma.ESCOPETA;
                case RIFLE -> enemigo instanceof Sniper
                        ? ClaseArma.RIFLE_PRECISION : ClaseArma.RIFLE_ASALTO;
                case PESADA -> ClaseArma.AMETRALLADORA;
                case COHETE -> ClaseArma.LANZACOHETES;
                default -> ClaseArma.ENERGIA;
            };
        };
        return new Arma(nombre + serie + " de " + enemigo.getNombre(), descripcion, peso,
                danio + variante * 2, dosManos, categoria, municion, capacidad, capacidad,
                FaccionEquipo.ENEMIGA, clase, 4 + variante * 3);
    }

    private static Armadura crearArmadura(Enemigo enemigo) {
        if (enemigo instanceof CommanderPrime) {
            return armadura(enemigo, "bastion de mando", "Nodo blindado de la colmena",
                    9.0, 10, 35, 20);
        }
        if (enemigo instanceof PyroOverlord) {
            return armadura(enemigo, "coraza de obsidiana", "Caparazon termorresistente",
                    8.0, 9, 40, 10);
        }
        if (enemigo instanceof HeavyFloater) {
            return armadura(enemigo, "coraza de plastiacero", "Blindaje gravitico pesado",
                    7.0, 8, 30, 0);
        }
        if (enemigo instanceof Commander) {
            return armadura(enemigo, "exocoraza de enlace", "Amplifica la red de combate",
                    5.0, 7, 20, 15);
        }
        if (enemigo instanceof Berserker) {
            return armadura(enemigo, "dermoplacas", "Hueso exterior cultivado para el asalto",
                    6.0, 5, 25, 0);
        }
        if (enemigo instanceof Pyro) {
            return armadura(enemigo, "manto termico", "Tejido ignifugo de primera linea",
                    4.0, 6, 15, 10);
        }
        if (enemigo instanceof Sniper) {
            return armadura(enemigo, "velo refractivo", "Camuflaje protector de tirador",
                    2.0, 4, 5, 15);
        }
        if (enemigo instanceof Medic) {
            return armadura(enemigo, "bioarmadura sanitaria", "Simbionte de soporte vital",
                    2.5, 3, 10, 20);
        }
        if (enemigo instanceof Scout) {
            return armadura(enemigo, "malla de infiltracion", "Proteccion ultraligera sensorial",
                    1.5, 2, 0, 20);
        }
        if (enemigo instanceof LightFloater) {
            return armadura(enemigo, "arnes gravitico", "Armazon de vuelo y evasion",
                    2.0, 3, 5, 15);
        }
        return armadura(enemigo, "malla psionica", "Tejido neural de defensa alienigena",
                2.0, 2, 5, 10);
    }

    private static Armadura armadura(Enemigo enemigo, String nombre,
            String descripcion, double peso, int defensa, int salud, int energia) {
        return new Armadura(nombre + " de " + enemigo.getNombre(), descripcion,
                peso, defensa, salud, energia, FaccionEquipo.ENEMIGA);
    }

    private static double peso(TipoMunicion tipo) {
        return switch (tipo) {
            case PESADA, COHETE -> 1.5;
            case ENERGIA -> 0.8;
            default -> 0.5;
        };
    }
}
