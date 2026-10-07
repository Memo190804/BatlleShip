import java.io.*;
import java.net.*;

/*
 * ============================================================================
 *  CLIENTE DE BATALLA NAVAL  (el programa que usa el jugador)
 * ============================================================================
 *
 *   1. Abre la ventana de CONEXION (nombre, IP, puerto y dificultad).
 *   2. Al darle "Conectar" manda la SOLICITUD al servidor.
 *   3. Cuando llega INICIO abre la ventana para COLOCAR los barcos.
 *   4. Al darle "Listo" avisa al servidor (LISTO) y abre la ventana de JUEGO.
 *   5. Durante el juego manda DISPAROS y responde con RESULTADOS.
 *
 * El cliente hace DOS cosas al mismo tiempo:
 *   - Mostrar las ventanas y atender los clicks.
 *   - Esperar mensajes del servidor (cl.receive se queda detenido).
 * Por eso esta clase "extends Thread": el metodo run() es un segundo
 * trabajador que solo escucha al servidor mientras las ventanas siguen vivas.
 */
public class ClienteBatalla extends Thread {

    static DatagramSocket cl;
    static InetAddress dirServidor;
    static int ptoServidor;
    static String nombre;

    static Tablero tableroPropio;               // mis barcos
    static Tablero tableroTiro = new Tablero(); // a donde dispare yo y que paso

    static VentanaConexion ventanaConexion;
    static VentanaColocacion ventanaColocacion;
    static VentanaJuego ventanaJuego;


    public static void main(String[] args){
        try{
            cl = new DatagramSocket(); // sin puerto: el sistema le da uno libre
            ventanaConexion = new VentanaConexion();
            ventanaConexion.setVisible(true);

            ClienteBatalla hilo = new ClienteBatalla();
            hilo.start(); // arranca run() en paralelo
        }catch(Exception e){
            e.printStackTrace();
        }//catch
    }//main


    // ========================================================================
    // HILO QUE ESCUCHA AL SERVIDOR (igual que el for(;;) del servidor)
    // ========================================================================
    public void run(){
        try{
            for(;;){
                byte[] b = new byte[Protocolo.TAM_BUFFER];
                DatagramPacket p = new DatagramPacket(b, b.length);
                cl.receive(p);
                DataInputStream dis = new DataInputStream(new ByteArrayInputStream(p.getData(), 0, p.getLength()));
                int tipo = dis.readInt();

                if(tipo == Protocolo.INICIO){
                    // El servidor acepto: pasamos a colocar barcos (punto 3)
                    String mensaje = dis.readUTF();
                    System.out.println("Mensaje recibido: "+mensaje);
                    ventanaConexion.dispose(); // cerrar ventana
                    ventanaColocacion = new VentanaColocacion(nombre);
                    ventanaColocacion.setVisible(true);

                }else if(tipo == Protocolo.RECHAZO){
                    ventanaConexion.setEstado(dis.readUTF());

                }else if(tipo == Protocolo.TURNO){
                    boolean esMiTurno = dis.readBoolean();
                    int restantes = dis.readInt();
                    ventanaJuego.setTurno(esMiTurno, restantes);

                }else if(tipo == Protocolo.RESULTADO){
                    // Que paso con MI disparo
                    int fila = dis.readInt();
                    int columna = dis.readInt();
                    int resultado = dis.readInt();
                    boolean fin = dis.readBoolean();
                    tableroTiro.anotarTiro(fila, columna, resultado);
                    ventanaJuego.pintar();
                    ventanaJuego.anotar("Disparaste a "+Tablero.coordenada(fila, columna)+": "+Tablero.texto(resultado));
                    if(fin){
                        ventanaJuego.finDelJuego(true); // gane (punto 9)
                    }

                }else if(tipo == Protocolo.DISPARO){
                    // La PC me disparo
                    int fila = dis.readInt();
                    int columna = dis.readInt();
                    disparoDeLaPC(fila, columna);
                }
            }//for
        }catch(Exception e){
            e.printStackTrace();
        }//catch
    }


    // ========================================================================
    // LO QUE PASA CUANDO EL USUARIO PRESIONA BOTONES
    // (las ventanas llaman a estos metodos)
    // ========================================================================

    // Boton "Conectar": manda la SOLICITUD con el nombre y la dificultad (punto 2)
    static void conectar(String nombreUsuario, String ip, int puerto, int dificultad){
        try{
            nombre = nombreUsuario;
            dirServidor = InetAddress.getByName(ip);
            ptoServidor = puerto;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeInt(Protocolo.SOLICITUD);
            dos.writeUTF(nombre);
            dos.writeInt(dificultad);
            dos.flush();
            enviar(baos.toByteArray());

            System.out.println("Solicitud enviada a "+dirServidor+":"+ptoServidor);
            ventanaConexion.setEstado("Esperando respuesta del servidor...");
        }catch(Exception e){
            e.printStackTrace();
            ventanaConexion.setEstado("No se pudo conectar: "+e.getMessage());
        }
    }

    // Boton "Listo": abrimos la ventana de juego y avisamos al servidor (punto 4)
    static void listo(Tablero tablero){
        try{
            tableroPropio = tablero;
            ventanaColocacion.dispose();
            ventanaJuego = new VentanaJuego(nombre, tableroPropio, tableroTiro);
            ventanaJuego.setVisible(true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeInt(Protocolo.LISTO);
            dos.flush();
            enviar(baos.toByteArray());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // Click en el tablero del rival: mandamos el DISPARO (punto 7)
    static void disparar(int fila, int columna){
        // Si esa casilla ya no esta VACIA es que ya le habiamos disparado
        if(tableroTiro.casilla[fila][columna] != Tablero.VACIO){
            ventanaJuego.anotar("Ya disparaste ahi, elige otra casilla");
            return;
        }
        try{
            ventanaJuego.bloquear(); // no dejamos dar mas clicks hasta que llegue la respuesta

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeInt(Protocolo.DISPARO);
            dos.writeInt(fila);
            dos.writeInt(columna);
            dos.flush();
            enviar(baos.toByteArray());
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    // ========================================================================
    // LA PC ME DISPARO: reviso mi tablero y le regreso el RESULTADO (punto 8)
    // ========================================================================
    static void disparoDeLaPC(int fila, int columna) throws Exception {
        int resultado = tableroPropio.recibirDisparo(fila, columna);
        boolean fin = tableroPropio.todosHundidos();
        ventanaJuego.pintar();
        ventanaJuego.anotar("La PC disparo a "+Tablero.coordenada(fila, columna)+": "+Tablero.texto(resultado));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(Protocolo.RESULTADO);
        dos.writeInt(fila);
        dos.writeInt(columna);
        dos.writeInt(resultado);
        dos.writeBoolean(fin);
        dos.flush();
        enviar(baos.toByteArray());

        if(fin){
            ventanaJuego.finDelJuego(false); // perdi (punto 9)
        }
    }

    // Mete los bytes en un datagrama y lo manda al servidor
    static void enviar(byte[] b) throws Exception {
        DatagramPacket p = new DatagramPacket(b, b.length, dirServidor, ptoServidor);
        cl.send(p);
    }
}
