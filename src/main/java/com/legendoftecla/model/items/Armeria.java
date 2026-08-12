package com.legendoftecla.model.items;

/** Catalogo de fabricas que evita construir combinaciones incompatibles. */
public final class Armeria {
    private Armeria() { }

    public static Arma espada(String nombre) {
        return new Arma(nombre, "Espada de combate", 2.5, 18, false,
                CategoriaArma.MELE, TipoMunicion.INFINITA, 0, 0);
    }
    public static Arma cuchillo(String nombre) {
        return new Arma(nombre, "Cuchillo de combate", 0.7, 10, false,
                CategoriaArma.MELE, TipoMunicion.INFINITA, 0, 0);
    }
    public static Arma cuchillosArrojadizos(String nombre, int cantidad) {
        return new Arma(nombre, "Cuchillos arrojadizos", 1.0, 12, false,
                CategoriaArma.ARROJADIZA, TipoMunicion.CUCHILLO_ARROJADIZO,
                Math.max(1, cantidad), Math.max(0, cantidad));
    }
    public static Arma arco(String nombre, int flechas) {
        return new Arma(nombre, "Arco tactico", 1.8, 14, true,
                CategoriaArma.ARCO, TipoMunicion.FLECHA,
                Math.max(1, flechas), Math.max(0, flechas));
    }
    public static Arma ballesta(String nombre, int virotes) {
        return new Arma(nombre, "Ballesta tactica", 3.0, 20, true,
                CategoriaArma.BALLESTA, TipoMunicion.VIROTE,
                Math.max(1, virotes), Math.max(0, virotes));
    }
    public static Arma rifle(String nombre, int cargador, int actual) {
        return new Arma(nombre, "Rifle de fuego", 3.5, 16, true,
                CategoriaArma.FUEGO, TipoMunicion.RIFLE, cargador, actual);
    }
    public static Arma pistola(String nombre, int cargador, int actual) {
        return new Arma(nombre, "Pistola de fuego", 1.2, 10, false,
                CategoriaArma.FUEGO, TipoMunicion.PISTOLA, cargador, actual);
    }
    public static Arma pesada(String nombre, int cargador, int actual) {
        return new Arma(nombre, "Arma pesada", 7.0, 24, true,
                CategoriaArma.FUEGO, TipoMunicion.PESADA, cargador, actual);
    }
    public static Arma energia(String nombre, int cargador, int actual) {
        return new Arma(nombre, "Arma de energia", 3.0, 18, true,
                CategoriaArma.FUEGO, TipoMunicion.ENERGIA, cargador, actual);
    }
    public static Granada granadaFragmentacion(String nombre) {
        return new Granada(nombre, "Granada de fragmentacion", 0.6,
                TipoGranada.FRAGMENTACION);
    }
}
