import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class VentanaConexion extends JFrame implements ActionListener {

    JTextField campoNombre = new JTextField(15);
    JTextField campoIp = new JTextField("127.0.0.1", 15);
    JTextField campoPuerto = new JTextField("1234", 15);

    JComboBox<String> comboDificultad = new JComboBox<>(new String[]{"Facil", "Dificil"});
    JButton btConectar = new JButton("Conectar");
    JLabel etiquetaEstado = new JLabel(" ", SwingConstants.CENTER);

    public VentanaConexion() {
        super("Batalla Naval - Conexion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.add(new JLabel("Nombre de usuario:"));
        panel.add(campoNombre);
        panel.add(new JLabel("IP del servidor:"));
        panel.add(campoIp);
        panel.add(new JLabel("Puerto:"));
        panel.add(campoPuerto);
        panel.add(new JLabel("Dificultad de la PC:"));
        panel.add(comboDificultad);
        panel.add(new JLabel(""));
        panel.add(btConectar);

        etiquetaEstado.setForeground(Color.RED);
        add(panel, BorderLayout.CENTER);
        add(etiquetaEstado, BorderLayout.SOUTH);

        btConectar.addActionListener(this);

        pack();
        setLocationRelativeTo(null);
    }

    public void actionPerformed(ActionEvent e) {
        String nombre = campoNombre.getText().trim();
        String ip = campoIp.getText().trim();
        int puerto;
        if (nombre.isEmpty()) {
            setEstado("Escribe tu nombre");
            return;
        }
        try {
            puerto = Integer.parseInt(campoPuerto.getText().trim());
        } catch (Exception ex) {
            setEstado("El puerto debe ser un numero");
            return;
        }

        int dificultad;
        if (comboDificultad.getSelectedItem().equals("Dificil")) {
            dificultad = BotPC.DIFICIL;
        } else {
            dificultad = BotPC.FACIL;
        }
        ClienteBatalla.conectar(nombre, ip, puerto, dificultad);
    }

    void setEstado(String mensaje) {
        etiquetaEstado.setText(mensaje);
    }
}
