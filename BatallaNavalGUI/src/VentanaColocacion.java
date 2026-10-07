import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/*
 * ============================================================================
 *  VENTANA 2: COLOCAR BARCOS  (punto 3)
 * ============================================================================
 *
 * Los barcos se colocan en orden (Submarino, Acorazado, Crucero...).
 * Le das click a la casilla donde empieza el barco: si es horizontal
 * crece hacia la derecha, si es vertical crece hacia abajo.
 */
public class VentanaColocacion extends JFrame implements ActionListener {

    Tablero tablero = new Tablero();
    TableroPanel panelTablero;
    boolean horizontal = true;

    JLabel etiqueta = new JLabel(" ", SwingConstants.CENTER);
    JButton btGirar = new JButton("Orientacion: Horizontal");
    JButton btAleatorio = new JButton("Colocar aleatoriamente");
    JButton btReiniciar = new JButton("Reiniciar");
    JButton btListo = new JButton("Listo");


    public VentanaColocacion(String nombre) {
        super("Batalla Naval - Coloca tus barcos (" + nombre + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panelTablero = new TableroPanel(tablero, true);
        panelTablero.ventanaColocacion = this; // que nos avise a nosotros de los clicks
        panelTablero.activo = true;

        // Botones de la derecha, uno debajo del otro
        JPanel panelBotones = new JPanel(new GridLayout(4, 1, 5, 5));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelBotones.add(btGirar);
        panelBotones.add(btAleatorio);
        panelBotones.add(btReiniciar);
        panelBotones.add(btListo);

        btGirar.addActionListener(this);
        btAleatorio.addActionListener(this);
        btReiniciar.addActionListener(this);
        btListo.addActionListener(this);

        etiqueta.setFont(new Font("SansSerif", Font.BOLD, 14));
        add(etiqueta, BorderLayout.NORTH);
        add(panelTablero, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.EAST);

        actualizar();
        pack();
        setLocationRelativeTo(null);
    }

    // Click en una casilla del tablero
    void clickEnCasilla(int fila, int columna) {
        if (tablero.colocarBarco(fila, columna, horizontal)) {
            actualizar();
        } else {
            etiqueta.setText("No cabe ahi o queda pegado a otro barco");
        }
    }

    // Click en uno de los botones de la derecha
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btGirar) {
            // Si estaba horizontal pasa a vertical, y al reves
            if (horizontal) {
                horizontal = false;
                btGirar.setText("Orientacion: Vertical");
            } else {
                horizontal = true;
                btGirar.setText("Orientacion: Horizontal");
            }

        } else if (e.getSource() == btAleatorio) {
            tablero.colocarAleatorio();
            actualizar();

        } else if (e.getSource() == btReiniciar) {
            tablero.reiniciar();
            actualizar();

        } else if (e.getSource() == btListo) {
            ClienteBatalla.listo(tablero); // punto 4
        }
    }

    // Repinta el tablero y dice que barco sigue
    void actualizar() {
        panelTablero.pintar();
        if (tablero.todosColocados()) {
            etiqueta.setText("Ya colocaste todos tus barcos, presiona Listo");
            btListo.setEnabled(true);
        } else {
            int i = tablero.colocados;
            etiqueta.setText("Coloca tu " + Tablero.NOMBRES[i] + " (" + Tablero.TAMANIOS[i] + " casillas)");
            btListo.setEnabled(false);
        }
    }
}
