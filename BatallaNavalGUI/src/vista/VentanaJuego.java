package vista;

import modelo.ResultadoDisparo;
import modelo.Tablero;

import javax.swing.*;
import java.awt.*;

/**
 * Pantalla principal de juego: muestra el tablero propio y el tablero de tiro
 * (puntos 6, 7 y 8 del requerimiento). Tu compañero debe:
 *  - Llamar a setTurno(...) y setDisparosRestantes(...) cuando cambie el turno.
 *  - Llamar a mostrarResultadoPropio(...) cuando llegue la respuesta a un disparo propio.
 *  - Llamar a recibirDisparoRival(...) cuando llegue una coordenada disparada por el rival,
 *    y enviar el ResultadoDisparo devuelto de vuelta al otro extremo.
 *  - Llamar a mostrarFinDeJuego(...) cuando el servidor indique que el juego terminó.
 */
public class VentanaJuego extends JFrame {

    public interface JuegoListener {
        void onDisparo(int fila, int columna);
    }

    private final Tablero tableroPropio;
    private final Tablero tableroTiro = new Tablero();

    private final TableroPanel panelPropio;
    private final TableroPanel panelTiro;

    private final JLabel etiquetaTurno = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel etiquetaDisparosRestantes = new JLabel(" ", SwingConstants.CENTER);
    private final JTextArea areaRegistro = new JTextArea(8, 30);

    private JuegoListener listener;

    public VentanaJuego(String nombreUsuario, Tablero tableroPropio) {
        super("Batalla Naval - " + nombreUsuario);
        this.tableroPropio = tableroPropio;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panelPropio = new TableroPanel(tableroPropio, true);
        panelPropio.setInteractivo(false);

        panelTiro = new TableroPanel(tableroTiro, false);
        panelTiro.setClickCeldaListener((fila, columna) -> {
            if (listener != null) listener.onDisparo(fila, columna);
        });

        JPanel panelTableros = new JPanel(new GridLayout(1, 2, 20, 0));
        panelTableros.add(envolverConTitulo(panelPropio, "Tu tablero"));
        panelTableros.add(envolverConTitulo(panelTiro, "Tablero del rival (dispara aquí)"));

        etiquetaTurno.setFont(new Font("SansSerif", Font.BOLD, 16));
        etiquetaDisparosRestantes.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel panelSuperior = new JPanel(new GridLayout(2, 1));
        panelSuperior.add(etiquetaTurno);
        panelSuperior.add(etiquetaDisparosRestantes);

        areaRegistro.setEditable(false);
        JScrollPane scrollRegistro = new JScrollPane(areaRegistro);
        scrollRegistro.setBorder(BorderFactory.createTitledBorder("Registro de la partida"));

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);
        panelPrincipal.add(panelTableros, BorderLayout.CENTER);
        panelPrincipal.add(scrollRegistro, BorderLayout.SOUTH);

        setContentPane(panelPrincipal);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel envolverConTitulo(JComponent componente, String titulo) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(titulo));
        panel.add(componente, BorderLayout.CENTER);
        return panel;
    }

    // ---------- Métodos para que la capa de red actualice la interfaz ----------

    /** Habilita/deshabilita el disparo según de quién sea el turno. */
    public void setTurno(boolean esMiTurno) {
        etiquetaTurno.setText(esMiTurno ? "Es tu turno: elige coordenada en el tablero del rival"
                                         : "Turno del rival, espera...");
        etiquetaTurno.setForeground(esMiTurno ? new Color(0, 128, 0) : Color.DARK_GRAY);
        panelTiro.setInteractivo(esMiTurno);
    }

    /** Punto 6: hasta 3 tiros consecutivos mientras no se falle. */
    public void setDisparosRestantes(int restantes) {
        etiquetaDisparosRestantes.setText("Disparos consecutivos restantes: " + restantes);
    }

    /** Refleja en el tablero de tiro (rival) el resultado de un disparo propio. */
    public void mostrarResultadoPropio(int fila, int columna, ResultadoDisparo resultado) {
        tableroTiro.marcarResultadoPropio(fila, columna, resultado);
        panelTiro.actualizar();
        agregarRegistro("Disparaste a " + coordTexto(fila, columna) + ": " + textoResultado(resultado));
    }

    /** Procesa un disparo recibido del rival sobre nuestro propio tablero; devuelve el resultado a enviar de vuelta. */
    public ResultadoDisparo recibirDisparoRival(int fila, int columna) {
        ResultadoDisparo resultado = tableroPropio.recibirDisparo(fila, columna);
        panelPropio.actualizar();
        agregarRegistro("El rival disparó a " + coordTexto(fila, columna) + ": " + textoResultado(resultado));
        return resultado;
    }

    public void mostrarFinDeJuego(boolean gano) {
        panelTiro.setInteractivo(false);
        String mensaje = gano ? "¡Felicidades, hundiste toda la flota rival!"
                               : "Perdiste, tu flota fue hundida por completo.";
        agregarRegistro(mensaje);
        JOptionPane.showMessageDialog(this, mensaje, "Fin del juego",
                gano ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
    }

    public void agregarRegistro(String texto) {
        areaRegistro.append(texto + "\n");
        areaRegistro.setCaretPosition(areaRegistro.getDocument().getLength());
    }

    private String coordTexto(int fila, int columna) {
        return "" + (char) ('A' + fila) + (columna + 1);
    }

    private String textoResultado(ResultadoDisparo resultado) {
        switch (resultado) {
            case AGUA: return "AGUA";
            case TOCADO: return "TOCADO";
            case HUNDIDO: return "¡HUNDIDO!";
            case YA_DISPARADO: return "ya se había disparado ahí";
            default: return "coordenada inválida";
        }
    }

    public void setJuegoListener(JuegoListener listener) { this.listener = listener; }
    public Tablero getTableroPropio() { return tableroPropio; }
    public Tablero getTableroTiro() { return tableroTiro; }
}
