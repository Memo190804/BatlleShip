import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/*
 * ============================================================================
 *  PANEL QUE DIBUJA UN TABLERO: una cuadricula de 10x10 botones
 * ============================================================================
 *
 * Cada casilla es un JButton y se pinta de un color segun lo que tenga:
 *   azul claro = vacio,  gris = mi barco,  azul fuerte "o" = agua,
 *   naranja "X" = tocado,  rojo "#" = hundido.
 *
 * mostrarBarcos = true  -> se ven los barcos (mi tablero)
 * mostrarBarcos = false -> los barcos no se ven (tablero del rival)
 */
public class TableroPanel extends JPanel implements ActionListener {

    Tablero tablero;
    boolean mostrarBarcos;
    boolean activo = false; // si es false se ignoran los clicks
    JButton[][] botones = new JButton[Tablero.N][Tablero.N];

    // A que ventana le avisamos cuando le dan click a una casilla
    // (solo se llena una de las dos, la otra se queda en null)
    VentanaColocacion ventanaColocacion = null;
    VentanaJuego ventanaJuego = null;


    public TableroPanel(Tablero tablero, boolean mostrarBarcos) {
        // "this.tablero" es la variable de la clase y "tablero" es la que nos pasaron:
        // guardamos lo que nos pasaron en las variables de la clase
        this.tablero = tablero;
        this.mostrarBarcos = mostrarBarcos;

        // 11 x 11: una fila extra para los numeros y una columna extra para las letras
        setLayout(new GridLayout(Tablero.N + 1, Tablero.N + 1));

        add(new JLabel("")); // esquina de arriba a la izquierda
        for (int c = 0; c < Tablero.N; c++) {
            String numero = Integer.toString(c + 1); // convierte el numero a texto: 1, 2, 3 ... 10
            add(new JLabel(numero, SwingConstants.CENTER));
        }
        for (int f = 0; f < Tablero.N; f++) {
            String letra = Tablero.LETRAS[f]; // A, B, C ... J
            add(new JLabel(letra, SwingConstants.CENTER));
            for (int c = 0; c < Tablero.N; c++) {
                JButton bt = new JButton();
                bt.setPreferredSize(new Dimension(32, 32));
                bt.setMargin(new Insets(0, 0, 0, 0));
                bt.setFocusPainted(false);
                bt.addActionListener(this); // al darle click se llama a actionPerformed
                botones[f][c] = bt;
                add(bt);
            }
        }
        pintar();
    }

    // Vuelve a pintar todas las casillas segun el tablero
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

    // Se ejecuta cuando le dan click a cualquier boton del tablero
    public void actionPerformed(ActionEvent e) {
        if (activo == false) {
            return; // no es momento de dar click: no hacemos nada
        }
        // Buscamos cual de los 100 botones fue
        for (int f = 0; f < Tablero.N; f++) {
            for (int c = 0; c < Tablero.N; c++) {
                if (e.getSource() == botones[f][c]) {
                    // Le avisamos a la ventana que tenga este tablero
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
