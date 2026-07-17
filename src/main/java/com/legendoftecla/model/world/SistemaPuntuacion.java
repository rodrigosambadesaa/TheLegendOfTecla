package com.legendoftecla.model.world;

import com.legendoftecla.model.characters.Jugador;


/**
 * Representa la entidad SistemaPuntuacion del juego.
 */
public final class SistemaPuntuacion {
    private static final int MAX_PUNTOS_SALUD = 300;
    private static final int MAX_PUNTOS_ENERGIA = 200;
    private static final int MAX_PUNTOS_PASOS = 250;
    private static final int MAX_PUNTOS_PROGRESO = 150;
    private static final int MAX_PUNTOS_ENEMIGOS = 100;

    private SistemaPuntuacion() {
    }

    /**
     * Ejecuta calcular.
      * @param estado valor de {@code estado}
      * @param juego valor de {@code juego}
      * @return resultado de la operacion
     */
    public static ResultadoPuntuacion calcular(Juego juego, EstadoFinalPartida estado) {
        Jugador jugador = juego.getJugador();
        Mapa mapa = juego.getMapa();

        int puntosSalud = Math.round(
                MAX_PUNTOS_SALUD * porcentajeSeguro(jugador.getSalud(), jugador.getSaludMaxima()));
        int puntosEnergia = Math.round(
                MAX_PUNTOS_ENERGIA * porcentajeSeguro(jugador.getEnergia(), jugador.getEnergiaMaxima()));

        float eficienciaPasos = 1.0f - porcentajeSeguro(juego.getPasos(), juego.getPasosMaximos());
        int puntosPasos = Math.round(MAX_PUNTOS_PASOS * clamp01(eficienciaPasos));

        int distanciaInicial = mapa.getInicio().distanciaManhattan(mapa.getObjetivo());
        int distanciaActual = jugador.getPosicion().distanciaManhattan(mapa.getObjetivo());
        float progreso = distanciaInicial <= 0 ? 1.0f : 1.0f - ((float) distanciaActual / distanciaInicial);
        int puntosProgreso = Math.round(MAX_PUNTOS_PROGRESO * clamp01(progreso));

        long derrotados = juego.getEnemigos().stream().filter(e -> e.getSalud() <= 0).count();
        int puntosEnemigos = (int) Math.min(MAX_PUNTOS_ENEMIGOS, derrotados * 20);

        int bonusResultado = switch (estado) {
            case VICTORIA -> 200;
            case MUERTE -> -200;
            case SIN_PASOS -> -100;
            case SALIDA_MANUAL -> 0;
        };

        int total = puntosSalud + puntosEnergia + puntosPasos + puntosProgreso + puntosEnemigos + bonusResultado;

        return new ResultadoPuntuacion(total, puntosSalud, puntosEnergia, puntosPasos, puntosProgreso, puntosEnemigos,
                bonusResultado, derrotados);
    }

    private static float porcentajeSeguro(int actual, int maximo) {
        if (maximo <= 0) {
            return 0.0f;
        }
        return clamp01((float) actual / maximo);
    }

    private static float clamp01(float valor) {
        return Math.max(0.0f, Math.min(1.0f, valor));
    }

    /**
     * Representa {@code EstadoFinalPartida} dentro del dominio del juego.
     */
    public enum EstadoFinalPartida {
        /**
         * Valor publico {@code VICTORIA} utilizado por el modelo del juego.
         */
        VICTORIA,
        /**
         * Valor publico {@code MUERTE} utilizado por el modelo del juego.
         */
        MUERTE,
        /**
         * Valor publico {@code SIN_PASOS} utilizado por el modelo del juego.
         */
        SIN_PASOS,
        /**
         * Valor publico {@code valor} utilizado por el modelo del juego.
         */
        SALIDA_MANUAL
    }

    /**
     * Representa {@code ResultadoPuntuacion} dentro del dominio del juego.
     */
    public static final class ResultadoPuntuacion {
        private final int total;
        private final int salud;
        private final int energia;
        private final int pasos;
        private final int progreso;
        private final int enemigos;
        private final int resultado;
        private final long enemigosDerrotados;

        private ResultadoPuntuacion(int total, int salud, int energia, int pasos, int progreso, int enemigos,
                int resultado, long enemigosDerrotados) {
            this.total = total;
            this.salud = salud;
            this.energia = energia;
            this.pasos = pasos;
            this.progreso = progreso;
            this.enemigos = enemigos;
            this.resultado = resultado;
            this.enemigosDerrotados = enemigosDerrotados;
        }

        /**
         * Ejecuta getTotal.
          * @return resultado de la operacion
         */
        public int getTotal() {
            return total;
        }

        /**
         * Ejecuta formatearDesglose.
          * @return resultado de la operacion
         */
        public String[] formatearDesglose() {
            return new String[] {
                    "Puntuacion final: " + total,
                    "  - Salud restante: " + salud,
                    "  - Energia restante: " + energia,
                    "  - Eficiencia en pasos: " + pasos,
                    "  - Progreso hacia objetivo: " + progreso,
                    "  - Enemigos derrotados (" + enemigosDerrotados + "): " + enemigos,
                    "  - Ajuste por resultado final: " + resultado
            };
        }
    }
}
