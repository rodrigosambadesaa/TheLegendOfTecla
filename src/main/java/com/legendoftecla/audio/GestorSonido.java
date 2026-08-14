package com.legendoftecla.audio;

import com.legendoftecla.model.world.Posicion;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.awt.GraphicsEnvironment;
import java.net.URL;

/** Reproduce efectos de forma asincrona y degrada silenciosamente si no hay dispositivo de audio. */
public final class GestorSonido {
    private static final int DISTANCIA_AUDIBLE = 8;
    /** Propiedad de proceso usada por herramientas sin interfaz. */
    public static final String PROPIEDAD_DESACTIVADO = "legendoftecla.audio.disabled";

    private GestorSonido() { }

    public static void reproducir(EventoSonido evento) {
        reproducir(evento, null, null);
    }

    public static void reproducir(EventoSonido evento, Posicion origen, Posicion oyente) {
        if (evento == null || Boolean.getBoolean(PROPIEDAD_DESACTIVADO)
                || GraphicsEnvironment.isHeadless()) return;
        int distancia = origen == null || oyente == null ? 0 : origen.distanciaManhattan(oyente);
        if (distancia > DISTANCIA_AUDIBLE) return;
        Thread hilo = new Thread(() -> abrir(evento, distancia), "tecla-audio");
        hilo.setDaemon(true);
        hilo.start();
    }

    private static void abrir(EventoSonido evento, int distancia) {
        try {
            URL recurso = GestorSonido.class.getResource("/audio/" + evento.getArchivo());
            if (recurso == null) return;
            AudioInputStream flujo = AudioSystem.getAudioInputStream(recurso);
            Clip clip = AudioSystem.getClip();
            clip.open(flujo);
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl volumen = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                volumen.setValue(Math.max(volumen.getMinimum(), -3.0f * distancia));
            }
            clip.addLineListener(e -> {
                if (e.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
                    clip.close();
                    try { flujo.close(); } catch (Exception ignored) { }
                }
            });
            clip.start();
        } catch (Exception ignored) {
            // El audio es una mejora opcional: nunca debe interrumpir una partida.
        }
    }
}
