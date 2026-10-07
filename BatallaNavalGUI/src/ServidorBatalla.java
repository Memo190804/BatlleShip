import java.io.*;
import java.net.*;

public class ServidorBatalla {

    static DatagramSocket s;
    static java.util.Random random = new java.util.Random();

    static InetAddress dirCliente = null;
    static int ptoCliente = -1;
    static String nombre = "";
    static int dificultad = BotPC.FACIL;

    static Tablero tableroPC;
    static Tablero tirosPC;

    static int disparosCliente = 0;
    static int disparosPC = 0;

    public static void main(String[] args){
        try{
            s = new DatagramSocket(Protocolo.PUERTO);
            System.out.println("Servidor iniciado en el puerto "+s.getLocalPort()+"\nEsperando solicitud de juego..");
            for(;;){

                byte[] b = new byte[Protocolo.TAM_BUFFER];
                DatagramPacket p = new DatagramPacket(b, b.length);
                s.receive(p);
                DataInputStream dis = new DataInputStream(new ByteArrayInputStream(p.getData(), 0, p.getLength()));
                int tipo = dis.readInt();

                if(dirCliente == null){
                    if(tipo == Protocolo.SOLICITUD){
                        dirCliente = p.getAddress();
                        ptoCliente = p.getPort();
                        nombre = dis.readUTF();
                        dificultad = dis.readInt();
                        System.out.println("Solicitud de juego de "+nombre+" desde "+dirCliente+":"+ptoCliente);
                        if(dificultad == BotPC.DIFICIL){
                            System.out.println("Dificultad: DIFICIL");
                        }else{
                            System.out.println("Dificultad: FACIL");
                        }

                        tableroPC = new Tablero();
                        tableroPC.colocarAleatorio();
                        tirosPC = new Tablero();
                        imprimirTableros();

                        enviarInicio();
                    }
                    continue;
                }

                boolean mismaIp = p.getAddress().equals(dirCliente);
                boolean mismoPuerto = (p.getPort() == ptoCliente);
                if(mismaIp == false || mismoPuerto == false){
                    System.out.println("Se rechazo un datagrama de "+p.getAddress()+":"+p.getPort());
                    enviarRechazo(p.getAddress(), p.getPort());
                    continue;
                }

                if(tipo == Protocolo.LISTO){
                    System.out.println(nombre+" ya coloco sus barcos");

                    boolean empiezaJugador = random.nextBoolean();
                    if(empiezaJugador){
                        System.out.println("Empieza "+nombre);
                        disparosCliente = Protocolo.MAX_DISPAROS;
                        enviarTurno(true, disparosCliente);
                    }else{
                        System.out.println("Empieza la PC");
                        turnoPC();
                    }

                }else if(tipo == Protocolo.DISPARO){

                    int fila = dis.readInt();
                    int columna = dis.readInt();
                    disparoDelCliente(fila, columna);

                }else if(tipo == Protocolo.RESULTADO){

                    int fila = dis.readInt();
                    int columna = dis.readInt();
                    int resultado = dis.readInt();
                    boolean fin = dis.readBoolean();
                    resultadoDisparoPC(fila, columna, resultado, fin);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    static void disparoDelCliente(int fila, int columna) throws Exception {
        int resultado = tableroPC.recibirDisparo(fila, columna);
        boolean fin = tableroPC.todosHundidos();
        System.out.println(nombre+" disparo a "+Tablero.coordenada(fila, columna)+": "+Tablero.texto(resultado));
        imprimirTableros();

        enviarResultado(fila, columna, resultado, fin);

        if(fin){
            finDelJuego("Gano "+nombre);
            return;
        }

        disparosCliente = disparosCliente - 1;
        if(sigueTirando(resultado, disparosCliente)){
            enviarTurno(true, disparosCliente);
        }else{
            turnoPC();
        }
    }

    static void turnoPC() throws Exception {
        disparosPC = Protocolo.MAX_DISPAROS;
        enviarTurno(false, 0);
        dispararPC();
    }

    static void dispararPC() throws Exception {
        Thread.sleep(1000);

        BotPC.elegirTiro(tirosPC, dificultad);
        int fila = BotPC.fila;
        int columna = BotPC.columna;
        System.out.println("La PC dispara a "+Tablero.coordenada(fila, columna));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(Protocolo.DISPARO);
        dos.writeInt(fila);
        dos.writeInt(columna);
        dos.flush();
        enviar(baos.toByteArray());

    }

    static void resultadoDisparoPC(int fila, int columna, int resultado, boolean fin) throws Exception {
        tirosPC.anotarTiro(fila, columna, resultado);
        System.out.println("Resultado del disparo de la PC en "+Tablero.coordenada(fila, columna)+": "+Tablero.texto(resultado));
        imprimirTableros();

        if(fin){
            finDelJuego("Gano la PC");
            return;
        }

        disparosPC = disparosPC - 1;
        if(sigueTirando(resultado, disparosPC)){
            dispararPC();
        }else{
            disparosCliente = Protocolo.MAX_DISPAROS;
            enviarTurno(true, disparosCliente);
        }
    }

    static boolean sigueTirando(int resultado, int restantes) {
        if(resultado == Tablero.AGUA) return false;
        return restantes > 0;
    }

    static void finDelJuego(String mensaje) {
        System.out.println("\n*** "+mensaje+" ***\nEsperando nueva solicitud de juego..");
        dirCliente = null;
        ptoCliente = -1;
    }

    static void enviarInicio() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(Protocolo.INICIO);
        dos.writeUTF("Hola "+nombre+", coloca tus barcos");
        dos.flush();
        enviar(baos.toByteArray());
        System.out.println("Se envio el inicio de juego a "+nombre);
    }

    static void enviarTurno(boolean turnoCliente, int restantes) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(Protocolo.TURNO);
        dos.writeBoolean(turnoCliente);
        dos.writeInt(restantes);
        dos.flush();
        enviar(baos.toByteArray());
    }

    static void enviarResultado(int fila, int columna, int resultado, boolean fin) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(Protocolo.RESULTADO);
        dos.writeInt(fila);
        dos.writeInt(columna);
        dos.writeInt(resultado);
        dos.writeBoolean(fin);
        dos.flush();
        enviar(baos.toByteArray());
    }

    static void enviarRechazo(InetAddress dir, int pto) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(Protocolo.RECHAZO);
        dos.writeUTF("El servidor ya esta jugando con otro cliente");
        dos.flush();
        byte[] b = baos.toByteArray();
        DatagramPacket p = new DatagramPacket(b, b.length, dir, pto);
        s.send(p);
    }

    static void enviar(byte[] b) throws Exception {
        DatagramPacket p = new DatagramPacket(b, b.length, dirCliente, ptoCliente);
        s.send(p);
    }

    static void imprimirTableros() {
        System.out.println("   Tablero de la PC        Tiros de la PC");
        for(int f = 0; f < Tablero.N; f++){
            String linea = Tablero.LETRAS[f] + "  ";
            for(int c = 0; c < Tablero.N; c++){
                linea = linea + simbolo(tableroPC.casilla[f][c]) + " ";
            }
            linea = linea + "   ";
            for(int c = 0; c < Tablero.N; c++){
                linea = linea + simbolo(tirosPC.casilla[f][c]) + " ";
            }
            System.out.println(linea);
        }
        System.out.println("(~ vacio, B barco, o agua, X tocado, # hundido)\n");
    }

    static String simbolo(int estado) {
        if(estado == Tablero.BARCO) return "B";
        if(estado == Tablero.AGUA) return "o";
        if(estado == Tablero.TOCADO) return "X";
        if(estado == Tablero.HUNDIDO) return "#";
        return "~";
    }
}
