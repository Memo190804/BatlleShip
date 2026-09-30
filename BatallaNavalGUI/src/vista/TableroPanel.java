package vista;

import modelo.EstadoCelda;
import modelo.Tablero;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * Panel que dibuja un tablero de 10x10 (con etiquetas 1-10 / A-J) y permite
 * hacer click en las celdas.
 *
 * mostrarBarcos = true  -> se ven los barcos propios (tablero de juego propio).
 * mostrarBarcos = false -> los barcos permanecen ocultos (tablero de tiro al rival),
 *                          solo se ven aciertos/fallos ya disparados.
 */
public class TableroPanel extends JPanel {

    public interface ClickCeldaListener {
        void onClickCelda(int fila, int columna);
    }

    private static final int MARGEN = 25;

    private Tablero tablero;
    private final boolean mostrarBarcos;
    private ClickCeldaListener listener;
    private boolean interactivo = true;
    private int celdaResaltadaFila = -1, celdaResaltadaColumna = -1;

    public TableroPanel(Tablero tablero, boolean mostrarBarcos) {
        this.tablero = tablero;
        this.mostrarBarcos = mostrarBarcos;
        setPreferredSize(new Dimension(370, 370));
        setBackground(Color.WHITE);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!interactivo) return;
                int tam = getTamCelda();
                int col = (e.getX() - MARGEN) / tam;
                int fila = (e.getY() - MARGEN) / tam;
                if (fila >= 0 && fila < Tablero.TAMANIO && col >= 0 && col < Tablero.TAMANIO && listener != null) {
                    listener.onClickCelda(fila, col);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                celdaResaltadaFila = -1; celdaResaltadaColumna = -1;
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int tam = getTamCelda();
                int col = (e.getX() - MARGEN) / tam;
                int fila = (e.getY() - MARGEN) / tam;
                if (interactivo && fila >= 0 && fila < Tablero.TAMANIO && col >= 0 && col < Tablero.TAMANIO) {
                    celdaResaltadaFila = fila; celdaResaltadaColumna = col;
                } else {
                    celdaResaltadaFila = -1; celdaResaltadaColumna = -1;
                }
                repaint();
            }
        });
    }

    public void setClickCeldaListener(ClickCeldaListener listener) { this.listener = listener; }
    public void setInteractivo(boolean interactivo) { this.interactivo = interactivo; }
    public void setTablero(Tablero tablero) { this.tablero = tablero; repaint(); }
    public void actualizar() { repaint(); }

    private int getTamCelda() {
        int disponible = Math.min(getWidth(), getHeight()) - MARGEN;
        return Math.max(20, disponible / Tablero.TAMANIO);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int tam = getTamCelda();
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.setColor(Color.BLACK);

        for (int c = 0; c < Tablero.TAMANIO; c++) {
            String etiqueta = String.valueOf(c + 1);
            g2.drawString(etiqueta, MARGEN + c * tam + tam / 2 - 4, MARGEN - 8);
        }
        for (int f = 0; f < Tablero.TAMANIO; f++) {
            String etiqueta = String.valueOf((char) ('A' + f));
            g2.drawString(etiqueta, MARGEN - 18, MARGEN + f * tam + tam / 2 + 4);
        }

        for (int f = 0; f < Tablero.TAMANIO; f++) {
            for (int c = 0; c < Tablero.TAMANIO; c++) {
                int x = MARGEN + c * tam;
                int y = MARGEN + f * tam;

                EstadoCelda estado = tablero.getEstado(f, c);
                Color relleno;
                switch (estado) {
                    case BARCO:
                        relleno = mostrarBarcos ? new Color(105, 105, 105) : new Color(173, 216, 230);
                        break;
                    case AGUA:
                        relleno = new Color(70, 130, 180);
                        break;
                    case TOCADO:
                        relleno = new Color(255, 165, 0);
                        break;
                    case HUNDIDO:
                        relleno = new Color(178, 34, 34);
                        break;
                    default:
                        relleno = new Color(173, 216, 230);
                }
                if (f == celdaResaltadaFila && c == celdaResaltadaColumna && interactivo) {
                    relleno = relleno.brighter();
                }

                g2.setColor(relleno);
                g2.fillRect(x, y, tam, tam);
                g2.setColor(Color.DARK_GRAY);
                g2.drawRect(x, y, tam, tam);

                if (estado == EstadoCelda.AGUA) {
                    g2.setColor(Color.WHITE);
                    g2.drawOval(x + tam / 3, y + tam / 3, tam / 3, tam / 3);
                } else if (estado == EstadoCelda.TOCADO || estado == EstadoCelda.HUNDIDO) {
                    g2.setColor(Color.WHITE);
                    g2.drawLine(x + 4, y + 4, x + tam - 4, y + tam - 4);
                    g2.drawLine(x + tam - 4, y + 4, x + 4, y + tam - 4);
                }
            }
        }
    }
}
