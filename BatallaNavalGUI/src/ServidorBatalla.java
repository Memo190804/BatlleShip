import java.io.*;
import java.net.*;

/*
 * ============================================================================
 *  SERVIDOR DE BATALLA NAVAL  (el servidor ES la computadora / PC)
 * ============================================================================
 *
 * Esta hecho igual que el ejemplo S1 de clase, pero con DatagramSocket:
 *   1. Abre el socket en el puerto 1234 y espera en un for(;;).
 *   2. Llega una SOLICITUD: guarda quien es el jugador (IP y puerto),
 *      la dificultad, coloca los barcos de la PC al azar y manda INICIO.
 *   3. Llega LISTO: se echa un volado para ver quien empieza.
 *   4. Se juega por turnos: hasta 3 tiros seguidos, o hasta fallar (punto 6).
 *   5. Cuando alguien hunde todos los barcos del otro, se acaba el juego.
 */
public class ServidorBatalla {

    static DatagramSocket s;
    static java.util.Random random = new java.util.Random(); // para sacar cosas al azar

    // Datos del jugador. Si dirCliente es null, no hay nadie jugando.
    static InetAddress dirCliente = null;
    static int ptoCliente = -1;
    static String nombre = "";
    static int dificultad = BotPC.FACIL;

    static Tablero tableroPC; // donde estan los barcos de la PC
    static Tablero tirosPC;   // donde la PC anota a donde disparo y que paso

    static int disparosCliente = 0; // tiros que le quedan al jugador en su turno
    static int disparosPC = 0;      // tiros que le quedan a la PC en su turno


    public static void main(String[] args){
        try{
            s = new DatagramSocket(Protocolo.PUERTO);
            System.out.println("Servidor iniciado en el puerto "+s.getLocalPort()+"\nEsperando solicitud de juego..");
            for(;;){
                // ---- 1) Recibir un datagrama (se queda esperando aqui) ----
                byte[] b = new byte[Protocolo.TAM_BUFFER];
                DatagramPacket p = new DatagramPacket(b, b.length);
                s.receive(p);
                DataInputStream dis = new DataInputStream(new ByteArrayInputStream(p.getData(), 0, p.getLength()));
                int tipo = dis.readInt(); // lo primero siempre es el tipo de mensaje

                // ---- 2) Si no hay nadie jugando, solo aceptamos una SOLICITUD ----
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

                        // La PC coloca sus barcos al azar (punto 5)
                        tableroPC = new Tablero();
                        tableroPC.colocarAleatorio();
                        tirosPC = new Tablero();
                        imprimirTableros();

                        enviarInicio(); // punto 3
                    }
                    continue; // regresamos al for a esperar otro datagrama
                }

                // ---- 3) Validar que sea el mismo jugador (punto 2) ----
                // Es el mismo jugador si coinciden la IP Y el puerto.
                boolean mismaIp = p.getAddress().equals(dirCliente);
                boolean mismoPuerto = (p.getPort() == ptoCliente);
                if(mismaIp == false || mismoPuerto == false){
                    System.out.println("Se rechazo un datagrama de "+p.getAddress()+":"+p.getPort());
                    enviarRechazo(p.getAddress(), p.getPort());
                    continue;
                }

                // ---- 4) Ver que mensaje es ----
                if(tipo == Protocolo.LISTO){
                    System.out.println(nombre+" ya coloco sus barcos");
                    // Volado para ver quien empieza (punto 5).
                    // random.nextBoolean() da true o false al azar.
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
                    // El jugador disparo a una casilla de la PC
                    int fila = dis.readInt();
                    int columna = dis.readInt();
                    disparoDelCliente(fila, columna);

                }else if(tipo == Protocolo.RESULTADO){
                    // El jugador nos dice que paso con el disparo de la PC
                    int fila = dis.readInt();
                    int columna = dis.readInt();
                    int resultado = dis.readInt();
                    boolean fin = dis.readBoolean();
                    resultadoDisparoPC(fila, columna, resultado, fin);
                }
            }//for
        }catch(Exception e){
            e.printStackTrace();
        }//catch
    }//main


    // ========================================================================
    // EL JUGADOR DISPARO A LA PC
    // ========================================================================
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

        disparosCliente = disparosCliente - 1; // gasto un tiro
        if(sigueTirando(resultado, disparosCliente)){
            enviarTurno(true, disparosCliente); // le atino: vuelve a tirar
        }else{
            turnoPC();                          // fallo o se le acabaron los tiros
        }
    }


    // ========================================================================
    // TURNO DE LA PC
    // ========================================================================
    static void turnoPC() throws Exception {
        disparosPC = Protocolo.MAX_DISPAROS;
        enviarTurno(false, 0); // le avisamos al jugador que NO es su turno
        dispararPC();
    }

    // La PC escoge casilla (segun la dificultad) y manda el DISPARO
    static void dispararPC() throws Exception {
        Thread.sleep(1000); // esperamos 1 segundo para que el jugador vea el tiro

        // El bot decide la casilla segun la dificultad y la deja en BotPC.fila y BotPC.columna
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
        // ahora regresamos al for(;;) del main a esperar el RESULTADO
    }

    // El jugador nos dijo que paso con el disparo de la PC
    static void resultadoDisparoPC(int fila, int columna, int resultado, boolean fin) throws Exception {
        tirosPC.anotarTiro(fila, columna, resultado);
        System.out.println("Resultado del disparo de la PC en "+Tablero.coordenada(fila, columna)+": "+Tablero.texto(resultado));
        imprimirTableros();

        if(fin){
            finDelJuego("Gano la PC");
            return;
        }

        disparosPC = disparosPC - 1; // gasto un tiro
        if(sigueTirando(resultado, disparosPC)){
            dispararPC();                          // la PC vuelve a tirar
        }else{
            disparosCliente = Protocolo.MAX_DISPAROS;
            enviarTurno(true, disparosCliente);    // le toca al jugador
        }
    }


    // ========================================================================
    // REGLA DE LOS TIROS (punto 6)
    // ========================================================================
    static boolean sigueTirando(int resultado, int restantes) {
        if(resultado == Tablero.AGUA) return false;  // fallo: se acaba el turno
        return restantes > 0;                        // le atino: sigue si le quedan tiros
    }

    // Borramos al jugador para que pueda entrar otro
    static void finDelJuego(String mensaje) {
        System.out.println("\n*** "+mensaje+" ***\nEsperando nueva solicitud de juego..");
        dirCliente = null;
        ptoCliente = -1;
    }


    // ========================================================================
    // MANDAR MENSAJES
    // Todos hacen lo mismo: escribir con DataOutputStream (como C1)
    // y mandar los bytes en un datagrama.
    // ========================================================================

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

    // Se le manda a un SEGUNDO cliente que quiera entrar
    static void enviarRechazo(InetAddress dir, int pto) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(Protocolo.RECHAZO);
        dos.writeUTF("El servidor ya esta jugando con otro cliente");
        dos.flush();
        byte[] b = baos.toByteArray();
        DatagramPacket p = new DatagramPacket(b, b.length, dir, pto); // va a OTRA direccion
        s.send(p);
    }

    // Mete los bytes en un datagrama y lo manda al jugador
    static void enviar(byte[] b) throws Exception {
        DatagramPacket p = new DatagramPacket(b, b.length, dirCliente, ptoCliente);
        s.send(p);
    }


    // ========================================================================
    // MOSTRAR LOS TABLEROS DE LA PC EN CONSOLA
    // ========================================================================
    static void imprimirTableros() {
        System.out.println("   Tablero de la PC        Tiros de la PC");
        for(int f = 0; f < Tablero.N; f++){
            String linea = Tablero.LETRAS[f] + "  "; // letra de la fila: A, B, C...
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
