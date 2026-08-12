package com.legendoftecla.gui;

import com.legendoftecla.engine.MotorPartida;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.model.world.TipoSuelo;
import com.legendoftecla.model.items.CuboAgua;
import com.legendoftecla.model.items.Linterna;

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
        var ultimoRuido = motor.getSistemaRuido().getUltimoRuido();

        for (int fila = 0; fila < mapa.getFilas(); fila++) {
            for (int columna = 0; columna < mapa.getColumnas(); columna++) {
                Posicion posicion = new Posicion(fila, columna);
                Celda celda = mapa.getCelda(posicion);
                int x = columna * TAMANO_CELDA;
                int y = fila * TAMANO_CELDA;

                Color suelo = celda.getTipoSuelo() == TipoSuelo.MADERA
                        ? new Color(104, 73, 43) : new Color(49, 57, 66);
                g.setColor(celda.isTerrenoTransitable() ? suelo : new Color(20, 23, 28));
                g.fillRect(x, y, TAMANO_CELDA, TAMANO_CELDA);
                if (celda.isTerrenoTransitable() && celda.getTipoSuelo() == TipoSuelo.MADERA) {
                    g.setColor(new Color(139, 96, 55));
                    g.drawLine(x + 3, y + 9, x + 29, y + 9);
                    g.drawLine(x + 3, y + 20, x + 29, y + 20);
                }
                g.setColor(new Color(76, 86, 96));
                g.drawRect(x, y, TAMANO_CELDA, TAMANO_CELDA);

                if (ultimoRuido.isPresent() && posicion.equals(ultimoRuido.get().origen())) {
                    int diametro = Math.min(28, 10 + ultimoRuido.get().intensidad());
                    int margen = (TAMANO_CELDA - diametro) / 2;
                    g.setColor(new Color(255, 214, 72, 175));
                    g.drawOval(x + margen, y + margen, diametro, diametro);
                    g.drawOval(x + margen + 3, y + margen + 3, diametro - 6, diametro - 6);
                }

                if (!celda.getElementos().isEmpty()) {
                    var elemento = celda.getElementos().get(0);
                    if (elemento instanceof com.legendoftecla.model.elements.Puerta) {
                        g.setColor(new Color(154, 101, 54));
                        g.fillRect(x + 11, y + 2, 10, 28);
                    } else if (elemento instanceof com.legendoftecla.model.elements.Trampa trampa
                            && trampa.isDetectada()) {
                        g.setColor(new Color(255, 120, 50));
                        g.drawPolygon(new int[]{x + 16, x + 5, x + 27},
                                new int[]{y + 5, y + 27, y + 27}, 3);
                    } else if (elemento instanceof com.legendoftecla.model.elements.Barricada) {
                        g.setColor(new Color(124, 93, 65));
                        g.fillRect(x + 3, y + 12, 26, 9);
                    } else if (elemento instanceof com.legendoftecla.model.elements.Terminal) {
                        g.setColor(new Color(70, 190, 205));
                        g.fillRect(x + 7, y + 7, 18, 18);
                    } else if (elemento instanceof com.legendoftecla.model.elements.ParedDebil) {
                        g.setColor(new Color(145, 150, 158));
                        g.fillRect(x + 3, y + 4, 26, 24);
                        g.setColor(new Color(60, 65, 72));
                        g.drawLine(x + 5, y + 8, x + 27, y + 24);
                        g.drawLine(x + 25, y + 6, x + 8, y + 27);
                    }
                }

                if (!celda.isTerrenoTransitable()) {
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
                if (celda.hasFuenteAgua()) {
                    g.setColor(new Color(65, 170, 235));
                    g.fillArc(x + 6, y + 14, 20, 13, 180, 180);
                    g.drawLine(x + 16, y + 5, x + 16, y + 17);
                    g.fillOval(x + 13, y + 7, 6, 8);
                }
                if (celda.hasAntorchaMural()) {
                    g.setColor(new Color(115, 72, 35));
                    g.fillRect(x + 5, y + 8, 3, 17);
                    g.setColor(new Color(255, 163, 35));
                    g.fillOval(x + 2, y + 3, 9, 11);
                    g.setColor(new Color(255, 224, 75));
                    g.fillOval(x + 5, y + 6, 4, 7);
                }
                if (!celda.getObjetos().isEmpty() && juego.isCeldaInspeccionada(posicion)) {
                    if (celda.getObjetos().stream().anyMatch(Linterna.class::isInstance)) {
                        g.setColor(new Color(255, 231, 120));
                        g.fillPolygon(new int[]{x + 4, x + 15, x + 15},
                                new int[]{y + 24, y + 18, y + 29}, 3);
                    } else if (celda.getObjetos().stream().anyMatch(CuboAgua.class::isInstance)) {
                        g.setColor(new Color(75, 177, 235));
                        g.fillRoundRect(x + 4, y + 20, 11, 10, 3, 3);
                    } else {
                        g.setColor(new Color(235, 187, 70));
                        g.fillRoundRect(x + 4, y + 21, 9, 8, 3, 3);
                    }
                }
                if (celda.isOscura() && !motor.hayLuzEn(posicion)) {
                    g.setColor(new Color(3, 5, 9, 225));
                    g.fillRect(x + 1, y + 1, TAMANO_CELDA - 1, TAMANO_CELDA - 1);
                    g.setColor(new Color(82, 90, 105));
                    g.drawString("?", x + 13, y + 21);
                    continue;
                }
                if (celda.estaArdiendo()) {
                    g.setColor(new Color(215, 48, 28, 220));
                    g.fillOval(x + 7, y + 9, 18, 20);
                    g.setColor(new Color(255, 158, 25));
                    g.fillOval(x + 11, y + 5, 12, 21);
                    g.setColor(new Color(255, 231, 80));
                    g.fillOval(x + 14, y + 13, 7, 13);
                }
                if (!celda.getAliados().isEmpty() && aliadosVisibles.contains(posicion)) {
                    g.setColor(new Color(65, 145, 245));
                    g.fillPolygon(new int[]{x + 5, x + 15, x + 5}, new int[]{y + 18, y + 13, y + 8}, 3);
                }
                if (!celda.getEnemigos().isEmpty() && enemigosVisibles.contains(posicion)) {
                    var enemigo = celda.getEnemigos().get(0);
                    if (enemigo instanceof com.legendoftecla.model.characters.Jefe) {
                        g.setColor(new Color(178, 62, 225));
                        g.fillOval(x + 16, y + 4, 14, 14);
                        g.setColor(Color.WHITE);
                        g.drawString("!", x + 21, y + 15);
                    } else {
                        g.setColor(new Color(224, 68, 74));
                        g.fillPolygon(new int[]{x + 23, x + 29, x + 23, x + 17},
                                new int[]{y + 6, y + 12, y + 18, y + 12}, 4);
                    }
                    if (enemigo.getControladorIA().getEstado()
                            != com.legendoftecla.ai.NivelAlerta.PATRULLA) {
                        g.setColor(colorAlerta(enemigo.getControladorIA().getEstado()));
                        g.fillOval(x + 23, y + 22, 7, 7);
                    }
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
        if (celda.isOscura()) detalle.append("<br>Zona oscura")
                .append(motor.hayLuzEn(posicion) ? " (iluminada)" : " (sin luz)");
        if (celda.getTipoSuelo() == TipoSuelo.MADERA) detalle.append("<br>Suelo de madera");
        if (celda.hasAntorchaMural()) detalle.append("<br>Antorcha mural anclada");
        if (celda.hasFuenteAgua()) detalle.append("<br>Fuente de agua");
        if (celda.estaArdiendo()) detalle.append("<br><b>INCENDIO nivel ")
                .append(celda.getNivelFuego()).append("</b>");
        if (!celda.getElementos().isEmpty()) detalle.append("<br>Elemento: ")
                .append(celda.getElementos().get(0).getClass().getSimpleName());
        motor.getSistemaRuido().getUltimoRuido()
                .filter(ruido -> ruido.origen().equals(posicion))
                .ifPresent(ruido -> detalle.append("<br>Ruido: ").append(ruido.causa())
                        .append(" (").append(ruido.intensidad()).append(")"));
        if (!celda.getObjetos().isEmpty() && motor.getJuego().isCeldaInspeccionada(posicion)) {
            detalle.append("<br>Objetos: ").append(celda.getObjetos());
        }
        if (!celda.getAliados().isEmpty()) detalle.append("<br>Aliados: ").append(celda.getAliados().size());
        if (!celda.getEnemigos().isEmpty() && motor.getEnemigosVisibles().contains(posicion)) {
            detalle.append("<br>Enemigos: ").append(celda.getEnemigos().size())
                    .append(" | alerta: ")
                    .append(celda.getEnemigos().get(0).getControladorIA().getEstado());
        }
        return detalle.append("</html>").toString();
    }

    private Color colorAlerta(com.legendoftecla.ai.NivelAlerta alerta) {
        return switch (alerta) {
            case COMBATE, ALERTA -> new Color(255, 45, 45);
            case INVESTIGANDO, BUSQUEDA, SOSPECHA -> new Color(255, 190, 35);
            case HUYENDO -> new Color(115, 190, 255);
            case PROTEGIENDO -> new Color(190, 105, 255);
            case PATRULLA -> new Color(100, 185, 110);
        };
    }
}
