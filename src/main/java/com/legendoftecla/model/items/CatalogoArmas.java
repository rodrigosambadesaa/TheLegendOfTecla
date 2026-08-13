package com.legendoftecla.model.items;

import com.legendoftecla.validation.Validaciones;

import java.util.List;
import java.util.Random;

/**
 * Catalogo de modelos humanos inspirado en los roles tacticos de los shooters,
 * pero con nombres propios. Cada modelo conserva dano, cargador, peso, alcance
 * y penetracion independientes de la precision del tirador.
 */
public final class CatalogoArmas {
    private static final List<Modelo> MODELOS = List.of(
            m("Gladius", "Espada corta equilibrada", 2.0, 16, false, ClaseArma.ESPADA_UNA_MANO, 3),
            m("Sable de frontera", "Espada ligera de una mano", 1.8, 14, false, ClaseArma.ESPADA_UNA_MANO, 2),
            m("Espada bastarda", "Espada versatil de una mano", 2.7, 20, false, ClaseArma.ESPADA_UNA_MANO, 5),
            m("Mandoble titan", "Gran espada de dos manos", 5.8, 34, true, ClaseArma.ESPADA_DOS_MANOS, 10),
            m("Nodachi", "Hoja larga de dos manos", 4.1, 29, true, ClaseArma.ESPADA_DOS_MANOS, 8),
            m("Claymore", "Espada pesada de dos manos", 5.2, 32, true, ClaseArma.ESPADA_DOS_MANOS, 9),
            m("Cuchillo tactico", "Hoja rapida de combate", 0.6, 10, false, ClaseArma.CUCHILLO, 2),
            m("Kukri", "Cuchillo curvo de gran corte", 0.8, 13, false, ClaseArma.CUCHILLO, 3),
            m("Trio arrojadizo", "Juego de cuchillos equilibrados", 0.9, 12, false,
                    ClaseArma.CUCHILLO_ARROJADIZO, TipoMunicion.CUCHILLO_ARROJADIZO, 3, 1),
            m("Arco recurvo", "Arco movil de cadencia alta", 1.6, 15, true,
                    ClaseArma.ARCO, TipoMunicion.FLECHA, 12, 2),
            m("Arco largo", "Arco potente de dos manos", 2.2, 21, true,
                    ClaseArma.ARCO, TipoMunicion.FLECHA, 8, 4),
            m("Ballesta ligera", "Ballesta compacta", 2.7, 20, true,
                    ClaseArma.BALLESTA, TipoMunicion.VIROTE, 5, 5),
            m("Ballesta de torno", "Ballesta pesada perforante", 4.8, 31, true,
                    ClaseArma.BALLESTA, TipoMunicion.VIROTE, 2, 11),
            m("P9 Compact", "Pistola fiable de servicio", 1.0, 12, false,
                    ClaseArma.PISTOLA, TipoMunicion.PISTOLA, 15, 4),
            m("Falcon .50", "Pistola pesada de gran impacto", 1.8, 28, false,
                    ClaseArma.PISTOLA, TipoMunicion.PISTOLA, 7, 12),
            m("Five Star", "Pistola ligera de alta capacidad", 1.1, 15, false,
                    ClaseArma.PISTOLA, TipoMunicion.PISTOLA, 20, 6),
            m("Revolver R8", "Revolver lento y contundente", 1.5, 25, false,
                    ClaseArma.PISTOLA, TipoMunicion.PISTOLA, 8, 10),
            m("Viper-9", "Subfusil compacto", 2.4, 11, true,
                    ClaseArma.SUBFUSIL, TipoMunicion.SUBFUSIL, 30, 3),
            m("Cyclone .45", "Subfusil de alto impacto", 2.8, 14, true,
                    ClaseArma.SUBFUSIL, TipoMunicion.SUBFUSIL, 25, 5),
            m("Storm-57", "Subfusil de cargador extendido", 2.6, 10, true,
                    ClaseArma.SUBFUSIL, TipoMunicion.SUBFUSIL, 50, 4),
            m("Kobra Vector", "Subfusil de control avanzado", 2.7, 13, true,
                    ClaseArma.SUBFUSIL, TipoMunicion.SUBFUSIL, 33, 5),
            m("Breacher-12", "Escopeta tactica de corredera", 3.4, 26, true,
                    ClaseArma.ESCOPETA, TipoMunicion.ESCOPETA, 8, 7),
            m("Auto-12", "Escopeta semiautomatica", 4.1, 18, true,
                    ClaseArma.ESCOPETA, TipoMunicion.ESCOPETA, 12, 6),
            m("Coach Gun", "Escopeta doble de corto alcance", 3.0, 32, true,
                    ClaseArma.ESCOPETA, TipoMunicion.ESCOPETA, 2, 9),
            m("Vandal-47", "Rifle de asalto robusto", 3.8, 22, true,
                    ClaseArma.RIFLE_ASALTO, TipoMunicion.RIFLE, 30, 9),
            m("Sentinel-4", "Rifle de asalto controlable", 3.2, 18, true,
                    ClaseArma.RIFLE_ASALTO, TipoMunicion.RIFLE, 30, 7),
            m("Ares Bullpup", "Rifle compacto con optica", 3.5, 17, true,
                    ClaseArma.RIFLE_ASALTO, TipoMunicion.RIFLE, 30, 8),
            m("Trident-25", "Rifle ligero de rafaga", 3.1, 16, true,
                    ClaseArma.RIFLE_ASALTO, TipoMunicion.RIFLE, 25, 6),
            m("Longshot .338", "Rifle de precision de cerrojo", 6.0, 42, true,
                    ClaseArma.RIFLE_PRECISION, TipoMunicion.RIFLE, 5, 20),
            m("Ranger Scout", "Rifle de precision ligero", 3.3, 28, true,
                    ClaseArma.RIFLE_PRECISION, TipoMunicion.RIFLE, 10, 13),
            m("Specter DMR", "Rifle de precision semiautomatico", 4.5, 24, true,
                    ClaseArma.RIFLE_PRECISION, TipoMunicion.RIFLE, 20, 12),
            m("Titan-249", "Ametralladora de apoyo", 8.1, 18, true,
                    ClaseArma.AMETRALLADORA, TipoMunicion.PESADA, 100, 10),
            m("Maelstrom", "Ametralladora de gran capacidad", 9.2, 16, true,
                    ClaseArma.AMETRALLADORA, TipoMunicion.PESADA, 150, 9),
            m("Bastion RPK", "Ametralladora pesada precisa", 7.4, 21, true,
                    ClaseArma.AMETRALLADORA, TipoMunicion.PESADA, 75, 13),
            m("Lanza-Raptor", "Lanzacohetes antiblindaje", 8.8, 48, true,
                    ClaseArma.LANZACOHETES, TipoMunicion.COHETE, 1, 30),
            m("Carabina ionica", "Arma energetica estable", 3.1, 19, true,
                    ClaseArma.ENERGIA, TipoMunicion.ENERGIA, 24, 14));

    private CatalogoArmas() { }

    /** @return todos los modelos disponibles para editor, generadores y tests */
    public static List<String> nombres() {
        return MODELOS.stream().map(Modelo::nombre).toList();
    }

    /** Crea un modelo cualquiera usando exclusivamente el RNG inyectado. */
    public static Arma crearAleatoria(Random random, String identificador) {
        Random validado = Validaciones.noNulo(random, "Generador aleatorio");
        return crear(MODELOS.get(validado.nextInt(MODELOS.size())), identificador);
    }

    /**
     * Recorre el catalogo sin repetir modelo hasta completar una vuelta. Es la
     * opcion indicada para poblar una partida con botin variado.
     */
    public static Arma crearPorIndice(long semilla, int indice, String identificador) {
        int desplazamiento = Math.floorMod(Long.hashCode(semilla), MODELOS.size());
        int posicion = Math.floorMod(desplazamiento + indice, MODELOS.size());
        return crear(MODELOS.get(posicion), identificador);
    }

    /** Crea una variante de una subfamilia concreta con seleccion reproducible. */
    public static Arma crearDeClase(ClaseArma clase, Random random, String identificador) {
        ClaseArma validada = Validaciones.noNulo(clase, "Clase del arma");
        List<Modelo> candidatos = MODELOS.stream()
                .filter(modelo -> modelo.clase() == validada).toList();
        if (candidatos.isEmpty()) throw new IllegalArgumentException("Clase sin modelos: " + clase);
        Random rng = Validaciones.noNulo(random, "Generador aleatorio");
        return crear(candidatos.get(rng.nextInt(candidatos.size())), identificador);
    }

    private static Arma crear(Modelo modelo, String identificador) {
        String sufijo = identificador == null || identificador.isBlank()
                ? "" : " [" + identificador.trim() + "]";
        CategoriaArma categoria = categoria(modelo.clase());
        return new Arma(modelo.nombre() + sufijo, modelo.descripcion(), modelo.peso(),
                modelo.danio(), modelo.dosManos(), categoria, modelo.municion(),
                modelo.capacidad(), modelo.capacidad(), FaccionEquipo.HUMANA,
                modelo.clase(), modelo.penetracion());
    }

    private static CategoriaArma categoria(ClaseArma clase) {
        return switch (clase) {
            case ESPADA_UNA_MANO, ESPADA_DOS_MANOS, CUCHILLO -> CategoriaArma.MELE;
            case CUCHILLO_ARROJADIZO -> CategoriaArma.ARROJADIZA;
            case ARCO -> CategoriaArma.ARCO;
            case BALLESTA -> CategoriaArma.BALLESTA;
            default -> CategoriaArma.FUEGO;
        };
    }

    private static Modelo m(String nombre, String descripcion, double peso, int danio,
            boolean dosManos, ClaseArma clase, int penetracion) {
        return m(nombre, descripcion, peso, danio, dosManos, clase,
                TipoMunicion.INFINITA, 0, penetracion);
    }

    private static Modelo m(String nombre, String descripcion, double peso, int danio,
            boolean dosManos, ClaseArma clase, TipoMunicion municion,
            int capacidad, int penetracion) {
        return new Modelo(nombre, descripcion, peso, danio, dosManos, clase,
                municion, capacidad, penetracion);
    }

    private record Modelo(String nombre, String descripcion, double peso, int danio,
            boolean dosManos, ClaseArma clase, TipoMunicion municion,
            int capacidad, int penetracion) { }
}
