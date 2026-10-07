import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TableroPanel extends JPanel implements ActionListener {

    Tablero tablero;
    boolean mostrarBarcos;
    boolean activo = false;
    JButton[][] botones = new JButton[Tablero.N][Tablero.N];

    VentanaColocacion ventanaColocacion = null;
    VentanaJuego ventanaJuego = null;

    public TableroPanel(Tablero tablero, boolean mostrarBarcos) {

        this.tablero = tablero;
        this.mostrarBarcos = mostrarBarcos;

        setLayout(new GridLayout(Tablero.N + 1, Tablero.N + 1));

        add(new JLabel(""));
        for (int c = 0; c < Tablero.N; c++) {
            String numero = Integer.toString(c + 1);
            add(new JLabel(numero, SwingConstants.CENTER));
        }
        for (int f = 0; f < Tablero.N; f++) {
            String letra = Tablero.LETRAS[f];
            add(new JLabel(letra, SwingConstants.CENTER));
            for (int c = 0; c < Tablero.N; c++) {
                JButton bt = new JButton();
                bt.setPreferredSize(new Dimension(32, 32));
                bt.setMargin(new Insets(0, 0, 0, 0));
                bt.setFocusPainted(false);
                bt.addActionListener(this);
                botones[f][c] = bt;
                add(bt);
            }
        }
        pintar();
    }

    void pintar() {
        for (int f = 0; f < Tablero.N; f++) {
            for (int c = 0; c < Tablero.N; c++) {
                int estado = tablero.casilla[f][c];
                JButton bt = botones[f][c];
                if (estado == Tablero.BARCO && mostrarBarcos) {
                    bt.setBackground(Color.GRAY);
                    bt.setText("");
                } else if (estado == Tablero.AGUA) {
                    bt.setBackground(new Color(70, 130, 180));
                    bt.setText("o");
                } else if (estado == Tablero.TOCADO) {
                    bt.setBackground(Color.ORANGE);
                    bt.setText("X");
                } else if (estado == Tablero.HUNDIDO) {
                    bt.setBackground(Color.RED);
                    bt.setText("#");
                } else {
                    bt.setBackground(new Color(173, 216, 230));
                    bt.setText("");
                }
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (activo == false) {
            return;
        }

        for (int f = 0; f < Tablero.N; f++) {
            for (int c = 0; c < Tablero.N; c++) {
                if (e.getSource() == botones[f][c]) {

                    if (ventanaColocacion != null) {
                        ventanaColocacion.clickEnCasilla(f, c);
                    }
                    if (ventanaJuego != null) {
                        ventanaJuego.clickEnCasilla(f, c);
                    }
                }
            }
        }
    }
}
