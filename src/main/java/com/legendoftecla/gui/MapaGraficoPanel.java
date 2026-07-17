package com.legendoftecla.gui;

import com.legendoftecla.engine.MotorPartida;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.util.Set;

/** Dibuja el estado del mapa mediante formas y colores, nunca como texto ASCII. */
public final class MapaGraficoPanel extends JPanel {
    private static final int TAMANO_CELDA = 32;
    /**
     * Valor publico {@code motor} utilizado por el modelo del juego.
     */
    private final MotorPartida motor;

    /**
     * Crea una instancia de {@code MapaGraficoPanel}.
      * @param motor valor de {@code motor}
     */
    public MapaGraficoPanel(MotorPartida motor) {
        this.motor = motor;
        Mapa mapa = motor.getJuego().getMapa();
        setPreferredSize(new Dimension(mapa.getColumnas() * TAMANO_CELDA, mapa.getFilas() * TAMANO_CELDA));
        setBackground(new Color(20, 24, 31));
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Juego juego = motor.getJuego();
        Mapa mapa = juego.getMapa();
        Set<Posicion> enemigosVisibles = motor.getEnemigosVisibles();
        Set<Posicion> aliadosVisibles = motor.getAliadosVisibles();

        for (int fila = 0; fila < mapa.getFilas(); fila++) {
            for (int columna = 0; columna < mapa.getColumnas(); columna++) {
                Posicion posicion = new Posicion(fila, columna);
                Celda celda = mapa.getCelda(posicion);
                int x = columna * TAMANO_CELDA;
                int y = fila * TAMANO_CELDA;

                g.setColor(celda.isTransitable() ? new Color(49, 57, 66) : new Color(20, 23, 28));
                g.fillRect(x, y, TAMANO_CELDA, TAMANO_CELDA);
                g.setColor(new Color(76, 86, 96));
                g.drawRect(x, y, TAMANO_CELDA, TAMANO_CELDA);

                if (!celda.isTransitable()) {
                    g.setColor(new Color(95, 103, 112));
                    g.fillRect(x + 5, y + 5, TAMANO_CELDA - 10, TAMANO_CELDA - 10);
                    continue;
                }
                if (posicion.equals(mapa.getInicio())) {
                    g.setColor(new Color(64, 190, 120));
                    g.drawRect(x + 3, y + 3, TAMANO_CELDA - 6, TAMANO_CELDA - 6);
                    g.drawRect(x + 5, y + 5, TAMANO_CELDA - 10, TAMANO_CELDA - 10);
                }
                if (posicion.equals(mapa.getObjetivo())) {
                    g.setColor(new Color(255, 196, 64));
                    Polygon estrella = new Polygon(
                            new int[]{x + 16, x + 20, x + 28, x + 22, x + 24, x + 16, x + 8, x + 10, x + 4, x + 12},
                            new int[]{y + 3, y + 11, y + 11, y + 17, y + 27, y + 21, y + 27, y + 17, y + 11, y + 11},
                            10);
                    g.fillPolygon(estrella);
                }
                if (!celda.getObjetos().isEmpty()) {
                    g.setColor(new Color(235, 187, 70));
                    g.fillRoundRect(x + 4, y + 21, 9, 8, 3, 3);
                }
                if (!celda.getAliados().isEmpty() && aliadosVisibles.contains(posicion)) {
                    g.setColor(new Color(65, 145, 245));
                    g.fillPolygon(new int[]{x + 5, x + 15, x + 5}, new int[]{y + 18, y + 13, y + 8}, 3);
                }
                if (!celda.getEnemigos().isEmpty() && enemigosVisibles.contains(posicion)) {
                    g.setColor(new Color(224, 68, 74));
                    g.fillPolygon(new int[]{x + 23, x + 29, x + 23, x + 17},
                            new int[]{y + 6, y + 12, y + 18, y + 12}, 4);
                }
                if (posicion.equals(juego.getJugador().getPosicion())) {
                    g.setColor(new Color(58, 220, 210));
                    g.fillOval(x + 8, y + 8, 17, 17);
                    g.setColor(Color.WHITE);
                    g.drawOval(x + 8, y + 8, 17, 17);
                }
            }
        }
        g.dispose();
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        int columna = event.getX() / TAMANO_CELDA;
        int fila = event.getY() / TAMANO_CELDA;
        Mapa mapa = motor.getJuego().getMapa();
        Posicion posicion = new Posicion(fila, columna);
        if (!mapa.estaDentro(posicion)) {
            return null;
        }
        Celda celda = mapa.getCelda(posicion);
        StringBuilder detalle = new StringBuilder("<html><b>")
                .append(fila).append(",").append(columna).append("</b> - ")
                .append(celda.getDescripcion());
        if (!celda.isTransitable()) detalle.append("<br>Muro / no transitable");
        if (!celda.getObjetos().isEmpty()) detalle.append("<br>Objetos: ").append(celda.getObjetos());
        if (!celda.getAliados().isEmpty()) detalle.append("<br>Aliados: ").append(celda.getAliados().size());
        if (!celda.getEnemigos().isEmpty() && motor.getEnemigosVisibles().contains(posicion)) {
            detalle.append("<br>Enemigos: ").append(celda.getEnemigos().size());
        }
        return detalle.append("</html>").toString();
    }
}
