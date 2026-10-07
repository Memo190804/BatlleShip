import javax.swing.*;
import java.awt.*;

/*
 * ============================================================================
 *  VENTANA 3: JUEGO  (puntos 6, 7, 8 y 9)
 * ============================================================================
 *
 * Izquierda: mi tablero (no se puede dar click).
 * Derecha:   tablero del rival (aqui se dispara cuando es mi turno).
 * Abajo:     registro de todo lo que va pasando.
 */
public class VentanaJuego extends JFrame {

    TableroPanel panelPropio;
    TableroPanel panelTiro;
    JLabel etiquetaTurno = new JLabel(" ", SwingConstants.CENTER);
    JLabel etiquetaTiros = new JLabel(" ", SwingConstants.CENTER);
    JTextArea registro = new JTextArea(8, 40);


    public VentanaJuego(String nombre, Tablero tableroPropio, Tablero tableroTiro) {
        super("Batalla Naval - " + nombre);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panelPropio = new TableroPanel(tableroPropio, true);
        panelPropio.setBorder(BorderFactory.createTitledBorder("Tu tablero"));

        panelTiro = new TableroPanel(tableroTiro, false);
        panelTiro.setBorder(BorderFactory.createTitledBorder("Tablero del rival (dispara aqui)"));
        panelTiro.ventanaJuego = this; // que nos avise a nosotros de los clicks

        // Los dos tableros lado a lado
        JPanel panelTableros = new JPanel(new GridLayout(1, 2, 20, 0));
        panelTableros.add(panelPropio);
        panelTableros.add(panelTiro);

        // Arriba: de quien es el turno y cuantos tiros quedan
        etiquetaTurno.setFont(new Font("SansSerif", Font.BOLD, 16));
        JPanel panelArriba = new JPanel(new GridLayout(2, 1));
        panelArriba.add(etiquetaTurno);
        panelArriba.add(etiquetaTiros);

        registro.setEditable(false);

        add(panelArriba, BorderLayout.NORTH);
        add(panelTableros, BorderLayout.CENTER);
        add(new JScrollPane(registro), BorderLayout.SOUTH);

        setTurno(false, 0);
        anotar("Esperando a que el servidor diga quien empieza...");
        pack();
        setLocationRelativeTo(null);
    }

    // Click en el tablero del rival
    void clickEnCasilla(int fila, int columna) {
        ClienteBatalla.disparar(fila, columna);
    }

    // El servidor nos dijo de quien es el turno
    void setTurno(boolean esMiTurno, int restantes) {
        panelTiro.activo = esMiTurno;
        if (esMiTurno) {
            etiquetaTurno.setText("Es tu turno: dispara en el tablero del rival");
            etiquetaTurno.setForeground(new Color(0, 128, 0));
            etiquetaTiros.setText("Tiros que te quedan: " + restantes);
        } else {
            etiquetaTurno.setText("Turno de la PC, espera...");
            etiquetaTurno.setForeground(Color.DARK_GRAY);
            etiquetaTiros.setText(" ");
        }
    }

    // Mientras llega la respuesta de un disparo no dejamos dar clicks
    void bloquear() {
        panelTiro.activo = false;
        etiquetaTurno.setText("Esperando resultado del disparo...");
    }

    void pintar() {
        panelPropio.pintar();
        panelTiro.pintar();
    }

    // Agrega una linea al registro de abajo
    void anotar(String texto) {
        registro.append(texto + "\n");
    }

    void finDelJuego(boolean gane) {
        panelTiro.activo = false;
        String mensaje;
        if (gane) {
            mensaje = "Felicidades, hundiste toda la flota de la PC!";
        } else {
            mensaje = "Perdiste, la PC hundio toda tu flota.";
        }
        anotar(mensaje);
        etiquetaTurno.setText("Fin del juego");
        etiquetaTiros.setText(" ");
        JOptionPane.showMessageDialog(this, mensaje);
    }
}
