/*
 * ============================================================================
 *  SERVIDOR DE BATALLA NAVAL  (el servidor ES la computadora / PC)
 * ============================================================================
 *
 * Que hace este programa, en orden:
 *   1. Abre un socket de datagramas en el puerto 1234 y se queda esperando.
 *   2. Cuando llega una SOLICITUD, guarda quien es el jugador (su IP y puerto)
 *      y coloca los barcos de la PC al azar.
 *   3. Le manda al jugador el mensaje de INICIO.
 *   4. Cuando el jugador avisa LISTO, se echa un "volado" para ver quien empieza.
 *   5. Se juega por turnos: cada quien puede tirar hasta 3 veces seguidas,
 *      pero si falla (AGUA) le toca al otro.
 *   6. Cuando alguien hunde todos los barcos del otro, se acaba el juego.
 *
 * Los "import" de abajo le dicen a Java que herramientas vamos a usar:
 *   - java.io.*   -> para convertir datos (numeros, texto) a bytes y al reves
 *   - java.net.*  -> para los sockets (mandar y recibir datagramas)
 *   - modelo.*    -> nuestras clases del juego (Tablero, Coordenada, etc.)
 */
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.Random;

import modelo.Coordenada;
import modelo.EstadoCelda;
import modelo.ResultadoDisparo;
import modelo.Tablero;
import red.Protocolo;

public class ServidorBatalla {

    // ------------------------------------------------------------------------
    // VARIABLES DEL SERVIDOR
    // "static" quiere decir que la variable es una sola y la pueden usar
    // todos los metodos (funciones) de este archivo.
    // ------------------------------------------------------------------------

    static DatagramSocket s;              // el socket por donde se mandan y reciben datagramas
    static Random random = new Random();  // sirve para sacar numeros al azar

    // Datos del jugador que esta conectado.
    // Si dirCliente vale null, quiere decir que NO hay nadie jugando.
    static InetAddress dirCliente = null; // IP del jugador
    static int ptoCliente = -1;           // puerto del jugador
    static String nombre = "";            // nombre del jugador

    // La PC tiene dos tableros (punto 1 de la practica):
    static Tablero tableroPC;             // donde estan los barcos de la PC
    static Tablero tableroTiroPC;         // donde la PC anota a donde ya disparo

    // Cuantos tiros le quedan a cada uno en su turno (maximo 3)
    static int disparosCliente = 0;
    static int disparosPC = 0;


    // ========================================================================
    // MAIN: aqui empieza el programa
    // ========================================================================
    public static void main(String[] args) {
        // try/catch: si algo falla adentro del try, en lugar de que el programa
        // truene, se brinca al catch y muestra el error.
        try {
            // Abrimos el socket en el puerto 1234
            s = new DatagramSocket(Protocolo.PUERTO);
            System.out.println("Servidor iniciado en el puerto " + s.getLocalPort() + "\nEsperando solicitud de juego..");

            // for(;;) es un ciclo infinito: el servidor nunca deja de escuchar
            for (;;) {

                // ---- 1) RECIBIR UN DATAGRAMA ----
                byte[] b = new byte[Protocolo.TAM_BUFFER];         // espacio vacio para guardar lo que llegue
                DatagramPacket p = new DatagramPacket(b, b.length); // "sobre" vacio donde caera el datagrama
                s.receive(p);                                       // el programa se DETIENE aqui hasta que llegue algo

                // ---- 2) LEER LO QUE TRAE ----
                // El datagrama trae puros bytes. DataInputStream nos deja leerlos
                // como numeros (readInt), texto (readUTF) o true/false (readBoolean).
                DataInputStream dis = new DataInputStream(new ByteArrayInputStream(p.getData(), 0, p.getLength()));
                int tipo = dis.readInt(); // lo primero siempre es el tipo de mensaje

                // ---- 3) SI NO HAY NADIE JUGANDO, SOLO ACEPTAMOS UNA SOLICITUD ----
                if (dirCliente == null) {
                    if (tipo == Protocolo.SOLICITUD) {
                        // Guardamos quien es el jugador (de donde vino el datagrama)
                        dirCliente = p.getAddress();
                        ptoCliente = p.getPort();
                        nombre = dis.readUTF();
                        System.out.println("Solicitud de juego de " + nombre + " desde " + dirCliente + ":" + ptoCliente);

                        // La PC coloca sus barcos al azar (punto 5)
                        tableroPC = new Tablero();
                        tableroPC.colocarBarcosAleatoriamente();
                        tableroTiroPC = new Tablero();
                        imprimirTableros();

                        // Le avisamos al jugador que ya puede colocar sus barcos (punto 3)
                        enviarInicio();
                    }
                    continue; // "continue" = regresar al inicio del for y esperar otro datagrama
                }

                // ---- 4) VALIDAR QUE SEA EL MISMO JUGADOR (punto 2) ----
                // Si el datagrama viene de otra IP u otro puerto, es otra persona: la rechazamos.
                if (!p.getAddress().equals(dirCliente) || p.getPort() != ptoCliente) {
                    System.out.println("Se ignoro un datagrama de " + p.getAddress() + ":" + p.getPort());
                    enviarRechazo(p.getAddress(), p.getPort());
                    continue;
                }

                // ---- 5) VER QUE TIPO DE MENSAJE ES Y HACER LO QUE CORRESPONDE ----
                if (tipo == Protocolo.LISTO) {
                    // El jugador ya coloco sus barcos (punto 4)
                    System.out.println(nombre + " ya coloco sus barcos");

                    // Volado para ver quien empieza (punto 5).
                    // random.nextBoolean() da true o false al azar.
                    if (random.nextBoolean()) {
                        System.out.println("Empieza " + nombre);
                        disparosCliente = Protocolo.MAX_DISPAROS;
                        enviarTurno(true, disparosCliente);
                    } else {
                        System.out.println("Empieza la PC");
                        turnoPC();
                    }

                } else if (tipo == Protocolo.DISPARO) {
                    // El jugador disparo a una casilla de la PC
                    int fila = dis.readInt();
                    int columna = dis.readInt();
                    disparoDelCliente(fila, columna);

                } else if (tipo == Protocolo.RESULTADO) {
                    // El jugador nos dice que paso con el disparo que hizo la PC
                    resultadoDisparoPC(dis);
                }
            }//for
        } catch (Exception e) {
            e.printStackTrace(); // muestra el error en consola
        }//catch
    }//main


    // ========================================================================
    // EL JUGADOR DISPARO A UNA CASILLA DE LA PC
    // ========================================================================
    static void disparoDelCliente(int fila, int columna) throws Exception {
        // El tablero revisa si en esa casilla hay barco y nos dice que paso
        ResultadoDisparo resultado = tableroPC.recibirDisparo(fila, columna);
        boolean fin = tableroPC.todosHundidos(); // true si ya no le quedan barcos a la PC
        System.out.println(nombre + " disparo a " + new Coordenada(fila, columna) + ": " + resultado);
        imprimirTableros();

        // ---- Le mandamos el RESULTADO al jugador ----
        // Paso 1: preparamos un "contenedor" de bytes vacio
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Paso 2: DataOutputStream nos deja escribir numeros y texto en ese contenedor
        DataOutputStream dos = new DataOutputStream(baos);
        // Paso 3: escribimos los datos EN EL MISMO ORDEN en que el cliente los va a leer
        dos.writeInt(Protocolo.RESULTADO);   // tipo de mensaje
        dos.writeInt(fila);                  // fila
        dos.writeInt(columna);               // columna
        dos.writeUTF(resultado.name());      // "AGUA", "TOCADO" o "HUNDIDO" como texto
        dos.writeBoolean(fin);               // si ya se acabo el juego
        if (resultado == ResultadoDisparo.HUNDIDO) {
            // Si se hundio un barco, mandamos todas sus casillas
            // para que el jugador lo pinte completo (punto 8)
            List<Coordenada> celdas = tableroPC.getCeldasBarcoEn(fila, columna);
            dos.writeInt(celdas.size());     // cuantas casillas son
            for (int i = 0; i < celdas.size(); i++) {
                dos.writeInt(celdas.get(i).getFila());
                dos.writeInt(celdas.get(i).getColumna());
            }
        } else {
            dos.writeInt(0);                 // 0 casillas hundidas
        }
        dos.flush();
        // Paso 4: sacamos los bytes y los mandamos en un datagrama
        enviar(baos.toByteArray());

        // ---- ¿Se acabo el juego? (punto 9) ----
        if (fin) {
            finDelJuego("Gano " + nombre);
            return; // "return" = salir de este metodo
        }

        // Si ya habia disparado en esa casilla, no se le cuenta el tiro
        if (resultado == ResultadoDisparo.YA_DISPARADO) {
            enviarTurno(true, disparosCliente);
            return;
        }

        // ---- ¿Sigue tirando o le toca a la PC? (punto 6) ----
        disparosCliente = disparosCliente - 1;
        if (resultado == ResultadoDisparo.AGUA || disparosCliente == 0) {
            // Fallo, o ya uso sus 3 tiros: le toca a la PC
            turnoPC();
        } else {
            // Le atino y todavia tiene tiros: sigue el jugador
            enviarTurno(true, disparosCliente);
        }
    }


    // ========================================================================
    // EMPIEZA EL TURNO DE LA PC
    // ========================================================================
    static void turnoPC() throws Exception {
        disparosPC = Protocolo.MAX_DISPAROS;
        enviarTurno(false, 0); // le avisamos al jugador que NO es su turno
        dispararPC();
    }


    // ========================================================================
    // LA PC ELIGE UNA CASILLA AL AZAR Y DISPARA
    // ========================================================================
    static void dispararPC() throws Exception {
        Thread.sleep(1000); // esperar 1 segundo para que el jugador alcance a ver el tiro

        // Escogemos fila y columna al azar (numeros del 0 al 9)
        int fila = random.nextInt(Tablero.TAMANIO);
        int columna = random.nextInt(Tablero.TAMANIO);
        // Si ya habiamos disparado ahi, escogemos otra hasta encontrar una nueva
        while (tableroTiroPC.getEstado(fila, columna) != EstadoCelda.VACIO) {
            fila = random.nextInt(Tablero.TAMANIO);
            columna = random.nextInt(Tablero.TAMANIO);
        }
        System.out.println("La PC dispara a " + new Coordenada(fila, columna));

        // Mandamos el DISPARO al jugador (punto 7). Mismos pasos que antes.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(Protocolo.DISPARO);
        dos.writeInt(fila);
        dos.writeInt(columna);
        dos.flush();
        enviar(baos.toByteArray());
        // Ahora el servidor regresa al for(;;) del main a esperar el RESULTADO
    }


    // ========================================================================
    // EL JUGADOR NOS DICE QUE PASO CON EL DISPARO DE LA PC
    // ========================================================================
    static void resultadoDisparoPC(DataInputStream dis) throws Exception {
        // Leemos los datos EN EL MISMO ORDEN en que el cliente los escribio
        int fila = dis.readInt();
        int columna = dis.readInt();
        ResultadoDisparo resultado = ResultadoDisparo.valueOf(dis.readUTF()); // convierte el texto "AGUA" a AGUA
        boolean fin = dis.readBoolean();
        int n = dis.readInt(); // cuantas casillas de barco hundido vienen

        // Anotamos el resultado en el tablero de tiro de la PC
        tableroTiroPC.marcarResultadoPropio(fila, columna, resultado);
        for (int i = 0; i < n; i++) {
            int f = dis.readInt();
            int c = dis.readInt();
            tableroTiroPC.marcarResultadoPropio(f, c, ResultadoDisparo.HUNDIDO);
        }
        System.out.println("Resultado del disparo de la PC en " + new Coordenada(fila, columna) + ": " + resultado);
        imprimirTableros();

        // ¿Se acabo el juego?
        if (fin) {
            finDelJuego("Gano la PC");
            return;
        }

        // ¿La PC sigue tirando o le toca al jugador?
        disparosPC = disparosPC - 1;
        if (resultado == ResultadoDisparo.AGUA || disparosPC == 0) {
            disparosCliente = Protocolo.MAX_DISPAROS;
            enviarTurno(true, disparosCliente); // le toca al jugador
        } else {
            dispararPC(); // la PC vuelve a tirar
        }
    }


    // ========================================================================
    // SE ACABO EL JUEGO: borramos al jugador para que pueda entrar otro
    // ========================================================================
    static void finDelJuego(String mensaje) {
        System.out.println("\n*** " + mensaje + " ***\nEsperando nueva solicitud de juego..");
        dirCliente = null;
        ptoCliente = -1;
    }


    // ========================================================================
    // METODOS PARA MANDAR MENSAJES
    // Todos siguen los mismos pasos:
    //   1. crear ByteArrayOutputStream y DataOutputStream
    //   2. escribir el tipo de mensaje y los datos
    //   3. mandar los bytes con enviar()
    // ========================================================================

    // Mensaje INICIO: "ya puedes colocar tus barcos"
    static void enviarInicio() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(Protocolo.INICIO);
        dos.writeUTF("Hola " + nombre + ", coloca tus barcos");
        dos.flush();
        enviar(baos.toByteArray());
        System.out.println("Se envio el inicio de juego a " + nombre);
    }

    // Mensaje TURNO: le dice al jugador si es su turno y cuantos tiros le quedan
    static void enviarTurno(boolean turnoCliente, int restantes) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(Protocolo.TURNO);
        dos.writeBoolean(turnoCliente);
        dos.writeInt(restantes);
        dos.flush();
        enviar(baos.toByteArray());
    }

    // Mensaje RECHAZO: se le manda a un SEGUNDO cliente que quiera entrar
    static void enviarRechazo(InetAddress dir, int pto) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(Protocolo.RECHAZO);
        dos.writeUTF("El servidor ya esta jugando con otro cliente");
        dos.flush();
        byte[] b = baos.toByteArray();
        // Aqui no usamos enviar() porque va a OTRA direccion, no al jugador
        DatagramPacket p = new DatagramPacket(b, b.length, dir, pto);
        s.send(p);
    }

    // Mete los bytes en un datagrama y lo manda al jugador.
    // Un DatagramPacket es como un sobre: lleva los datos (b),
    // cuantos bytes son (b.length) y a quien va (IP y puerto).
    static void enviar(byte[] b) throws Exception {
        DatagramPacket p = new DatagramPacket(b, b.length, dirCliente, ptoCliente);
        s.send(p);
    }


    // ========================================================================
    // MOSTRAR LOS DOS TABLEROS DE LA PC EN LA CONSOLA
    // ========================================================================
    static void imprimirTableros() {
        System.out.println("   Tablero de la PC        Tiros de la PC");
        // Recorremos las 10 filas (A a la J)
        for (int f = 0; f < Tablero.TAMANIO; f++) {
            String linea = (char) ('A' + f) + "  "; // letra de la fila: A, B, C...
            // Las 10 columnas del tablero de barcos
            for (int c = 0; c < Tablero.TAMANIO; c++) {
                linea = linea + simbolo(tableroPC.getEstado(f, c)) + " ";
            }
            linea = linea + "   ";
            // Las 10 columnas del tablero de tiros
            for (int c = 0; c < Tablero.TAMANIO; c++) {
                linea = linea + simbolo(tableroTiroPC.getEstado(f, c)) + " ";
            }
            System.out.println(linea);
        }
        System.out.println("(~ vacio, B barco, o agua, X tocado, # hundido)\n");
    }

    // Convierte el estado de una casilla en un simbolo para la consola
    static String simbolo(EstadoCelda estado) {
        if (estado == EstadoCelda.BARCO) return "B";
        if (estado == EstadoCelda.AGUA) return "o";
        if (estado == EstadoCelda.TOCADO) return "X";
        if (estado == EstadoCelda.HUNDIDO) return "#";
        return "~";
    }
}
