import modelo.Tablero;
import vista.VentanaColocacion;
import vista.VentanaConexion;
import vista.VentanaJuego;

import javax.swing.*;

/**
 * Punto de entrada de DEMOSTRACIÓN de la interfaz gráfica.
 *
 * Aquí se muestra cómo se encadenan las tres ventanas (Conexión -> Colocación
 * -> Juego). Los bloques marcados con TODO son exactamente los puntos donde
 * tu compañero debe integrar su código de sockets datagrama (cliente/servidor).
 * Ninguna de las clases de "vista" ni "modelo" conoce nada de sockets: solo
 * exponen listeners y métodos públicos para que la capa de red las controle.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::iniciar);
    }

    private static void iniciar() {
        VentanaConexion ventanaConexion = new VentanaConexion();

        ventanaConexion.setConexionListener((nombreUsuario, ip, puerto) -> {
            // TODO (red): abrir el DatagramSocket, enviar el mensaje de solicitud
            // de juego con "nombreUsuario" al servidor (punto 2 del requerimiento).
            // Si la conexión fue exitosa:
            ventanaConexion.setEstadoConexion("Conectado. Esperando inicio de juego...", false);

            // Cuando el servidor confirme el inicio (punto 3), pasar a colocación:
            ventanaConexion.dispose();
            abrirVentanaColocacion(nombreUsuario);
        });

        ventanaConexion.setVisible(true);
    }

    private static void abrirVentanaColocacion(String nombreUsuario) {
        VentanaColocacion ventanaColocacion = new VentanaColocacion(nombreUsuario);

        ventanaColocacion.setColocacionListener(tableroPropio -> {
            // TODO (red): enviar al servidor el aviso de que el jugador ya
            // colocó sus barcos y está listo para empezar (punto 4).
            // "tableroPropio" contiene el objeto Tablero con todos los barcos
            // colocados, listo para que la capa de red lo use si lo necesita.
            ventanaColocacion.dispose();
            abrirVentanaJuego(nombreUsuario, tableroPropio);
        });

        ventanaColocacion.setVisible(true);
    }

    private static void abrirVentanaJuego(String nombreUsuario, Tablero tableroPropio) {
        VentanaJuego ventanaJuego = new VentanaJuego(nombreUsuario, tableroPropio);

        ventanaJuego.setJuegoListener((fila, columna) -> {
            // TODO (red): enviar la coordenada elegida al otro extremo (punto 7).
            // Cuando llegue la respuesta con el resultado (AGUA/TOCADO/HUNDIDO),
            // llamar:
            //   ventanaJuego.mostrarResultadoPropio(fila, columna, resultado);
            // Si el resultado fue AGUA, el turno pasa al rival:
            //   ventanaJuego.setTurno(false);
            // Si fue TOCADO/HUNDIDO y aún quedan disparos consecutivos (máx. 3):
            //   ventanaJuego.setDisparosRestantes(restantes);
        });

        // TODO (red): cuando llegue una coordenada disparada por el rival:
        //   ResultadoDisparo resultado = ventanaJuego.recibirDisparoRival(fila, columna);
        //   (enviar "resultado" de vuelta al otro extremo)

        // Estado inicial de ejemplo (esto lo debe definir el servidor, punto 5):
        ventanaJuego.setTurno(true);
        ventanaJuego.setDisparosRestantes(3);

        ventanaJuego.setVisible(true);
    }
}
