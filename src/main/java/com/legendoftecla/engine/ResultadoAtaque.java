package com.legendoftecla.engine;

/** Resultado observable de un golpe despues de aplicar armadura y otros modificadores. */
public record ResultadoAtaque(String atacante, String objetivo, int vidaQuitada,
        int vidaRestante, int vidaMaxima, boolean mortal) { }
