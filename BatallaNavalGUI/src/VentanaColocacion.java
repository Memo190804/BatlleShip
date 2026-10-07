import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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
        panelTablero.ventanaColocacion = this;
        panelTablero.activo = true;

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

    void clickEnCasilla(int fila, int columna) {
        if (tablero.colocarBarco(fila, columna, horizontal)) {
            actualizar();
        } else {
            etiqueta.setText("No cabe ahi o queda pegado a otro barco");
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btGirar) {

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
            ClienteBatalla.listo(tablero);
        }
    }

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
