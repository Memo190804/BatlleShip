package vista;

import modelo.Tablero;
import modelo.TipoBarco;

import javax.swing.*;
import java.awt.*;

/**
 * Pantalla donde el usuario coloca sus barcos (punto 3 del requerimiento).
 * Al presionar "Listo" se dispara ColocacionListener.onListo(), donde tu
 * compañero debe avisar al servidor que el jugador ya está listo (punto 4).
 */
public class VentanaColocacion extends JFrame {

    public interface ColocacionListener {
        void onListo(Tablero tableroPropio);
    }

    private final Tablero tablero = new Tablero();
    private final TableroPanel panelTablero = new TableroPanel(tablero, true);
    private final DefaultListModel<TipoBarco> modeloLista = new DefaultListModel<>();
    private final JList<TipoBarco> listaBarcos = new JList<>(modeloLista);
    private final JRadioButton radioHorizontal = new JRadioButton("Horizontal", true);
    private final JRadioButton radioVertical = new JRadioButton("Vertical");
    private final JButton botonAleatorio = new JButton("Colocar aleatoriamente");
    private final JButton botonReiniciar = new JButton("Reiniciar");
    private final JButton botonListo = new JButton("Listo");
    private final JLabel etiquetaInfo = new JLabel(" ");

    private ColocacionListener listener;
    private TipoBarco tipoSeleccionado;

    public VentanaColocacion(String nombreUsuario) {
        super("Batalla Naval - Coloca tus barcos (" + nombreUsuario + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        for (TipoBarco t : TipoBarco.values()) modeloLista.addElement(t);
        listaBarcos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaBarcos.setCellRenderer(new BarcoCellRenderer());
        listaBarcos.setVisibleRowCount(4);
        listaBarcos.setSelectedIndex(0);
        tipoSeleccionado = modeloLista.get(0);
        listaBarcos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaBarcos.getSelectedValue() != null) {
                tipoSeleccionado = listaBarcos.getSelectedValue();
            }
        });

        ButtonGroup grupoOrientacion = new ButtonGroup();
        grupoOrientacion.add(radioHorizontal);
        grupoOrientacion.add(radioVertical);

        JPanel panelLateral = new JPanel();
        panelLateral.setLayout(new BoxLayout(panelLateral, BoxLayout.Y_AXIS));
        panelLateral.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panelLateral.setPreferredSize(new Dimension(240, 420));

        JLabel tituloLista = new JLabel("Barcos por colocar:");
        tituloLista.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelLateral.add(tituloLista);
        JScrollPane scrollLista = new JScrollPane(listaBarcos);
        scrollLista.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelLateral.add(scrollLista);
        panelLateral.add(Box.createVerticalStrut(10));

        JPanel panelOrientacion = new JPanel(new GridLayout(1, 2));
        panelOrientacion.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelOrientacion.setBorder(BorderFactory.createTitledBorder("Orientación"));
        panelOrientacion.add(radioHorizontal);
        panelOrientacion.add(radioVertical);
        panelLateral.add(panelOrientacion);
        panelLateral.add(Box.createVerticalStrut(15));

        botonAleatorio.setAlignmentX(Component.LEFT_ALIGNMENT);
        botonReiniciar.setAlignmentX(Component.LEFT_ALIGNMENT);
        botonListo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelLateral.add(botonAleatorio);
        panelLateral.add(Box.createVerticalStrut(6));
        panelLateral.add(botonReiniciar);
        panelLateral.add(Box.createVerticalStrut(15));
        panelLateral.add(botonListo);
        panelLateral.add(Box.createVerticalStrut(10));
        etiquetaInfo.setForeground(Color.RED);
        etiquetaInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelLateral.add(etiquetaInfo);

        botonListo.setEnabled(false);

        panelTablero.setClickCeldaListener((fila, columna) -> {
            boolean horizontal = radioHorizontal.isSelected();
            if (tipoSeleccionado == null) return;
            if (tablero.cantidadColocada(tipoSeleccionado) >= tipoSeleccionado.getCantidad()) {
                mostrarInfo("Ya colocaste todos los " + tipoSeleccionado.getNombre() + "s.");
                return;
            }
            boolean colocado = tablero.colocarBarco(tipoSeleccionado, fila, columna, horizontal);
            if (!colocado) {
                mostrarInfo("No se puede colocar ahí (fuera del tablero o muy cerca de otro barco).");
            } else {
                mostrarInfo(" ");
                panelTablero.actualizar();
                listaBarcos.repaint();
                botonListo.setEnabled(tablero.todosLosBarcosColocados());
            }
        });

        botonAleatorio.addActionListener(e -> {
            tablero.reiniciar();
            tablero.colocarBarcosAleatoriamente();
            panelTablero.actualizar();
            listaBarcos.repaint();
            botonListo.setEnabled(tablero.todosLosBarcosColocados());
        });

        botonReiniciar.addActionListener(e -> {
            tablero.reiniciar();
            panelTablero.actualizar();
            listaBarcos.repaint();
            botonListo.setEnabled(false);
        });

        botonListo.addActionListener(e -> {
            if (listener != null) listener.onListo(tablero);
        });

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panelPrincipal.add(panelTablero, BorderLayout.CENTER);
        panelPrincipal.add(panelLateral, BorderLayout.EAST);

        setContentPane(panelPrincipal);
        pack();
        setLocationRelativeTo(null);
    }

    private void mostrarInfo(String texto) { etiquetaInfo.setText(texto); }

    public void setColocacionListener(ColocacionListener listener) { this.listener = listener; }
    public void setBotonListoHabilitado(boolean habilitado) { botonListo.setEnabled(habilitado); }

    private class BarcoCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                        boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof TipoBarco) {
                TipoBarco t = (TipoBarco) value;
                int restantes = t.getCantidad() - tablero.cantidadColocada(t);
                label.setText(t.getNombre() + " (long. " + t.getLongitud() + ") - faltan: " + restantes);
                if (restantes == 0 && !isSelected) label.setForeground(Color.GRAY);
            }
            return label;
        }
    }
}
