package vista;

import javax.swing.*;
import java.awt.*;

/**
 * Pantalla inicial: el usuario ingresa su nombre y los datos del servidor.
 * El código de red (tu compañero) se engancha con setConexionListener().
 */
public class VentanaConexion extends JFrame {

    public interface ConexionListener {
        void onConectar(String nombreUsuario, String ip, int puerto);
    }

    private final JTextField campoNombre = new JTextField(15);
    private final JTextField campoIp = new JTextField("127.0.0.1", 15);
    private final JTextField campoPuerto = new JTextField("1234", 6);
    private final JButton botonConectar = new JButton("Conectar");
    private final JLabel etiquetaEstado = new JLabel(" ");

    private ConexionListener listener;

    public VentanaConexion() {
        super("Batalla Naval - Conexión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Nombre de usuario:"), gbc);
        gbc.gridx = 1;
        panel.add(campoNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("IP del servidor:"), gbc);
        gbc.gridx = 1;
        panel.add(campoIp, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Puerto:"), gbc);
        gbc.gridx = 1;
        panel.add(campoPuerto, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(botonConectar, gbc);

        gbc.gridy = 4;
        etiquetaEstado.setForeground(Color.RED);
        panel.add(etiquetaEstado, gbc);

        botonConectar.addActionListener(e -> intentarConectar());

        setContentPane(panel);
        pack();
        setLocationRelativeTo(null);
    }

    private void intentarConectar() {
        String nombre = campoNombre.getText().trim();
        String ip = campoIp.getText().trim();
        String puertoTexto = campoPuerto.getText().trim();

        if (nombre.isEmpty()) { mostrarError("Ingresa un nombre de usuario."); return; }
        if (ip.isEmpty()) { mostrarError("Ingresa la IP del servidor."); return; }
        int puerto;
        try {
            puerto = Integer.parseInt(puertoTexto);
        } catch (NumberFormatException ex) {
            mostrarError("El puerto debe ser un número.");
            return;
        }
        etiquetaEstado.setText(" ");
        if (listener != null) listener.onConectar(nombre, ip, puerto);
    }

    private void mostrarError(String mensaje) { etiquetaEstado.setForeground(Color.RED); etiquetaEstado.setText(mensaje); }

    /** Llamar desde la capa de red para informar éxito/fracaso de la conexión. */
    public void setEstadoConexion(String mensaje, boolean esError) {
        etiquetaEstado.setForeground(esError ? Color.RED : new Color(0, 128, 0));
        etiquetaEstado.setText(mensaje);
    }

    public void setBotonHabilitado(boolean habilitado) { botonConectar.setEnabled(habilitado); }
    public void setConexionListener(ConexionListener listener) { this.listener = listener; }
}
