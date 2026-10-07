import java.io.*;
import java.net.*;

public class ClienteBatalla extends Thread {

    static DatagramSocket cl;
    static InetAddress dirServidor;
    static int ptoServidor;
    static String nombre;

    static Tablero tableroPropio;
    static Tablero tableroTiro = new Tablero();

    static VentanaConexion ventanaConexion;
    static VentanaColocacion ventanaColocacion;
    static VentanaJuego ventanaJuego;

    public static void main(String[] args){
        try{
            cl = new DatagramSocket();
            ventanaConexion = new VentanaConexion();
            ventanaConexion.setVisible(true);

            ClienteBatalla hilo = new ClienteBatalla();
            hilo.start();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void run(){
        try{
            for(;;){
                byte[] b = new byte[Protocolo.TAM_BUFFER];
                DatagramPacket p = new DatagramPacket(b, b.length);
                cl.receive(p);
                DataInputStream dis = new DataInputStream(new ByteArrayInputStream(p.getData(), 0, p.getLength()));
                int tipo = dis.readInt();

                if(tipo == Protocolo.INICIO){

                    String mensaje = dis.readUTF();
                    System.out.println("Mensaje recibido: "+mensaje);
                    ventanaConexion.dispose();
                    ventanaColocacion = new VentanaColocacion(nombre);
                    ventanaColocacion.setVisible(true);

                }else if(tipo == Protocolo.RECHAZO){
                    ventanaConexion.setEstado(dis.readUTF());

                }else if(tipo == Protocolo.TURNO){
                    boolean esMiTurno = dis.readBoolean();
                    int restantes = dis.readInt();
                    ventanaJuego.setTurno(esMiTurno, restantes);

                }else if(tipo == Protocolo.RESULTADO){

                    int fila = dis.readInt();
                    int columna = dis.readInt();
                    int resultado = dis.readInt();
                    boolean fin = dis.readBoolean();
                    tableroTiro.anotarTiro(fila, columna, resultado);
                    ventanaJuego.pintar();
                    ventanaJuego.anotar("Disparaste a "+Tablero.coordenada(fila, columna)+": "+Tablero.texto(resultado));
                    if(fin){
                        ventanaJuego.finDelJuego(true);
                    }

                }else if(tipo == Protocolo.DISPARO){

                    int fila = dis.readInt();
                    int columna = dis.readInt();
                    disparoDeLaPC(fila, columna);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

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

    static void disparar(int fila, int columna){

        if(tableroTiro.casilla[fila][columna] != Tablero.VACIO){
            ventanaJuego.anotar("Ya disparaste ahi, elige otra casilla");
            return;
        }
        try{
            ventanaJuego.bloquear();

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
            ventanaJuego.finDelJuego(false);
        }
    }

    static void enviar(byte[] b) throws Exception {
        DatagramPacket p = new DatagramPacket(b, b.length, dirServidor, ptoServidor);
        cl.send(p);
    }
}
