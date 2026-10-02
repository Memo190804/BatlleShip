/*
 * ============================================================================
 *  CLIENTE DE BATALLA NAVAL  (el programa que usa el jugador)
 * ============================================================================
 *
 * Que hace este programa, en orden:
 *   1. Abre la ventana de CONEXION (nombre, IP y puerto).
 *   2. Al darle "Conectar", manda una SOLICITUD al servidor con el nombre.
 *   3. Cuando llega el INICIO, abre la ventana para COLOCAR los barcos.
 *   4. Al darle "Listo", avisa al servidor (LISTO) y abre la ventana de JUEGO.
 *   5. Durante el juego:
 *        - Cuando el jugador hace click en el tablero del rival -> manda DISPARO.
 *        - Cuando llega un RESULTADO -> lo pinta en el tablero del rival.
 *        - Cuando llega un DISPARO de la PC -> revisa su tablero y regresa el RESULTADO.
 *        - Cuando llega un TURNO -> activa o desactiva los clicks.
 *
 * IMPORTANTE: el cliente hace DOS cosas al mismo tiempo:
 *   - Mostrar las ventanas y responder a los clicks del usuario.
 *   - Esperar los mensajes del servidor (s.receive se queda detenido esperando).
 * Por eso usamos un HILO (Thread): es como un segundo "trabajador" que solo
 * se dedica a escuchar al servidor mientras las ventanas siguen funcionando.
 */
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import modelo.Coordenada;
import modelo.EstadoCelda;
import modelo.ResultadoDisparo;
import modelo.Tablero;
import red.Protocolo;
import vista.VentanaColocacion;
import vista.VentanaConexion;
import vista.VentanaJuego;

public class ClienteBatalla {

    // ------------------------------------------------------------------------
    // VARIABLES DEL CLIENTE
    // ------------------------------------------------------------------------
    static DatagramSocket cl;            // socket del cliente
    static InetAddress dirServidor;      // IP del servidor
    static int ptoServidor;              // puerto del servidor
    static String nombre;                // nombre del jugador

    static VentanaConexion ventanaConexion;  // ventana 1: conexion
    static VentanaJuego ventanaJuego;        // ventana 3: juego


    // ========================================================================
    // MAIN: aqui empieza el programa
    // ========================================================================
    public static void main(String[] args) {
        try {
            // Creamos el socket. No le damos puerto: el sistema le asigna uno libre.
            cl = new DatagramSocket();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Abrimos la ventana de conexion.
        // La flecha "->" significa: "cuando el usuario de click en Conectar,
        // ejecuta lo que esta entre las llaves { }".
        ventanaConexion = new VentanaConexion();
        ventanaConexion.setConexionListener((nombreUsuario, ip, puerto) -> {
            conectar(nombreUsuario, ip, puerto);
        });
        ventanaConexion.setVisible(true);

        // Creamos el hilo que se queda escuchando al servidor todo el tiempo
        Thread hilo = new Thread(() -> {
            escuchar();
        });
        hilo.start(); // arrancar el hilo
    }//main


    // ========================================================================
    // CONECTAR: manda la SOLICITUD de juego con el nombre (punto 2)
    // ========================================================================
    static void conectar(String nombreUsuario, String ip, int puerto) {
        try {
            nombre = nombreUsuario;
            dirServidor = InetAddress.getByName(ip); // convierte el texto "127.0.0.1" en una direccion
            ptoServidor = puerto;

            // Armamos el mensaje: tipo SOLICITUD + nombre
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeInt(Protocolo.SOLICITUD);
            dos.writeUTF(nombre);
            dos.flush();
            enviar(baos.toByteArray());

            System.out.println("Solicitud enviada a " + dirServidor + ":" + ptoServidor);
            ventanaConexion.setEstadoConexion("Esperando respuesta del servidor...", false);
        } catch (Exception e) {
            e.printStackTrace();
            ventanaConexion.setEstadoConexion("No se pudo conectar: " + e.getMessage(), true);
        }
    }


    // ========================================================================
    // ESCUCHAR: lo ejecuta el HILO. Recibe datagramas del servidor para siempre.
    // ========================================================================
    static void escuchar() {
        try {
            for (;;) {
                // Igual que en el servidor: espacio vacio + "sobre" vacio + receive
                byte[] b = new byte[Protocolo.TAM_BUFFER];
                DatagramPacket p = new DatagramPacket(b, b.length);
                cl.receive(p); // se detiene aqui hasta que llegue un datagrama
                DataInputStream dis = new DataInputStream(new ByteArrayInputStream(p.getData(), 0, p.getLength()));

                // Las ventanas solo se deben cambiar desde el hilo de las ventanas
                // (el de Swing). invokeLater le pasa el trabajo a ese hilo.
                // Si no se hace asi, las ventanas pueden fallar de vez en cuando.
                SwingUtilities.invokeLater(() -> {
                    procesarMensaje(dis);
                });
            }//for
        } catch (Exception e) {
            e.printStackTrace();
        }//catch
    }


    // ========================================================================
    // PROCESAR MENSAJE: ver que nos mando el servidor y actuar
    // ========================================================================
    static void procesarMensaje(DataInputStream dis) {
        try {
            int tipo = dis.readInt(); // lo primero siempre es el tipo de mensaje

            if (tipo == Protocolo.INICIO) {
                // El servidor acepto: cerramos la ventana de conexion
                // y abrimos la de colocar barcos (punto 3)
                String mensaje = dis.readUTF();
                System.out.println("Mensaje recibido: " + mensaje);
                ventanaConexion.dispose(); // dispose() = cerrar ventana
                abrirVentanaColocacion();

            } else if (tipo == Protocolo.RECHAZO) {
                // Ya hay alguien jugando con el servidor
                String mensaje = dis.readUTF();
                ventanaConexion.setEstadoConexion(mensaje, true);

            } else if (tipo == Protocolo.TURNO) {
                // El servidor nos dice si es nuestro turno y cuantos tiros quedan
                boolean esMiTurno = dis.readBoolean();
                int restantes = dis.readInt();
                ventanaJuego.setTurno(esMiTurno);          // activa/desactiva los clicks
                ventanaJuego.setDisparosRestantes(restantes);

            } else if (tipo == Protocolo.RESULTADO) {
                // El servidor nos dice que paso con NUESTRO disparo
                int fila = dis.readInt();
                int columna = dis.readInt();
                ResultadoDisparo resultado = ResultadoDisparo.valueOf(dis.readUTF()); // texto -> AGUA/TOCADO/HUNDIDO
                boolean fin = dis.readBoolean();
                int n = dis.readInt(); // cuantas casillas de barco hundido vienen

                // Pintamos el tiro en el tablero del rival
                ventanaJuego.mostrarResultadoPropio(fila, columna, resultado);

                if (n > 0) {
                    // Se hundio un barco: leemos sus casillas y lo pintamos completo (punto 8)
                    List<Coordenada> celdas = new ArrayList<>();
                    for (int i = 0; i < n; i++) {
                        int f = dis.readInt();
                        int c = dis.readInt();
                        celdas.add(new Coordenada(f, c));
                    }
                    ventanaJuego.marcarBarcoHundido(celdas);
                }
                if (fin) {
                    ventanaJuego.mostrarFinDeJuego(true); // ganamos (punto 9)
                }

            } else if (tipo == Protocolo.DISPARO) {
                // La PC disparo a nuestro tablero
                int fila = dis.readInt();
                int columna = dis.readInt();
                disparoDeLaPC(fila, columna);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // ========================================================================
    // VENTANA 2: COLOCAR BARCOS
    // ========================================================================
    static void abrirVentanaColocacion() {
        VentanaColocacion ventanaColocacion = new VentanaColocacion(nombre);

        // Esto se ejecuta cuando el usuario da click en "Listo"
        ventanaColocacion.setColocacionListener(tableroPropio -> {
            ventanaColocacion.dispose();       // cerramos la ventana de colocar
            abrirVentanaJuego(tableroPropio);  // abrimos la del juego
            try {
                // Le avisamos al servidor que ya estamos listos (punto 4)
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);
                dos.writeInt(Protocolo.LISTO);
                dos.flush();
                enviar(baos.toByteArray());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        ventanaColocacion.setVisible(true);
    }


    // ========================================================================
    // VENTANA 3: JUEGO
    // ========================================================================
    static void abrirVentanaJuego(Tablero tableroPropio) {
        ventanaJuego = new VentanaJuego(nombre, tableroPropio);

        // Esto se ejecuta cuando el usuario da click en el tablero del rival
        ventanaJuego.setJuegoListener((fila, columna) -> {
            disparar(fila, columna);
        });

        // Al principio no es el turno de nadie: esperamos a que el servidor decida
        ventanaJuego.setTurno(false);
        ventanaJuego.setDisparosRestantes(0);
        ventanaJuego.agregarRegistro("Esperando a que el servidor diga quien empieza...");
        ventanaJuego.setVisible(true);
    }


    // ========================================================================
    // EL JUGADOR DISPARA: mandamos la casilla al servidor (punto 7)
    // ========================================================================
    static void disparar(int fila, int columna) {
        // Si ya habiamos disparado ahi, no lo mandamos
        if (ventanaJuego.getTableroTiro().getEstado(fila, columna) != EstadoCelda.VACIO) {
            ventanaJuego.agregarRegistro("Ya disparaste ahi, elige otra casilla");
            return;
        }
        try {
            // Desactivamos los clicks mientras llega la respuesta
            ventanaJuego.bloquearDisparo();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeInt(Protocolo.DISPARO);
            dos.writeInt(fila);
            dos.writeInt(columna);
            dos.flush();
            enviar(baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // ========================================================================
    // LA PC NOS DISPARO: revisamos nuestro tablero y le regresamos el resultado
    // ========================================================================
    static void disparoDeLaPC(int fila, int columna) throws Exception {
        // Nuestro tablero revisa si le dieron a un barco y lo pinta en pantalla
        ResultadoDisparo resultado = ventanaJuego.recibirDisparoRival(fila, columna);
        Tablero tableroPropio = ventanaJuego.getTableroPropio();
        boolean fin = tableroPropio.todosHundidos(); // true si ya no nos quedan barcos

        // Le mandamos el RESULTADO al servidor (mismo orden que usa el servidor)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(Protocolo.RESULTADO);
        dos.writeInt(fila);
        dos.writeInt(columna);
        dos.writeUTF(resultado.name()); // "AGUA", "TOCADO" o "HUNDIDO"
        dos.writeBoolean(fin);
        if (resultado == ResultadoDisparo.HUNDIDO) {
            // Mandamos las casillas del barco que nos hundieron
            List<Coordenada> celdas = tableroPropio.getCeldasBarcoEn(fila, columna);
            dos.writeInt(celdas.size());
            for (int i = 0; i < celdas.size(); i++) {
                dos.writeInt(celdas.get(i).getFila());
                dos.writeInt(celdas.get(i).getColumna());
            }
        } else {
            dos.writeInt(0);
        }
        dos.flush();
        enviar(baos.toByteArray());

        if (fin) {
            ventanaJuego.mostrarFinDeJuego(false); // perdimos (punto 9)
        }
    }


    // ========================================================================
    // ENVIAR: mete los bytes en un datagrama y lo manda al servidor
    // ========================================================================
    static void enviar(byte[] b) throws Exception {
        // El DatagramPacket es el "sobre": datos, cuantos bytes, IP y puerto destino
        DatagramPacket p = new DatagramPacket(b, b.length, dirServidor, ptoServidor);
        cl.send(p);
    }
}
