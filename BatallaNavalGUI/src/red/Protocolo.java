package red;

/*
 * ============================================================================
 *  PROTOCOLO: las "reglas de la platica" entre el cliente y el servidor
 * ============================================================================
 *
 * El cliente y el servidor se mandan mensajes dentro de DATAGRAMAS (paquetes UDP).
 * Para que los dos se entiendan, TODOS los mensajes empiezan con un numero
 * que dice de que tipo es el mensaje. Despues del numero vienen los datos.
 *
 * Aqui guardamos esos numeros con un nombre, para no tener que acordarnos
 * de que "6" significa "disparo". En el codigo escribimos Protocolo.DISPARO
 * y Java sabe que vale 6.
 *
 *  Mensaje     Quien lo manda         Que datos lleva despues del numero
 *  ---------   --------------------   -------------------------------------------
 *  SOLICITUD   cliente -> servidor    el nombre del jugador
 *  INICIO      servidor -> cliente    un saludo ("Hola Memo, coloca tus barcos")
 *  RECHAZO     servidor -> cliente    aviso de que ya hay alguien jugando
 *  LISTO       cliente -> servidor    nada (solo avisa que ya coloco sus barcos)
 *  TURNO       servidor -> cliente    si es su turno (true/false) y cuantos tiros le quedan
 *  DISPARO     los dos                fila y columna a donde se dispara
 *  RESULTADO   los dos                fila, columna, "AGUA"/"TOCADO"/"HUNDIDO",
 *                                     si ya se acabo el juego (true/false),
 *                                     y si se hundio un barco, sus casillas
 *
 * "public static final" significa: es un valor fijo (no cambia nunca)
 * y se puede usar desde cualquier otro archivo.
 */
public class Protocolo {

    public static final int PUERTO = 1234;      // puerto donde escucha el servidor (igual que en clase)
    public static final int TAM_BUFFER = 512;   // tamaño maximo de un datagrama que vamos a recibir (en bytes)
    public static final int MAX_DISPAROS = 3;   // punto 6: maximo 3 tiros seguidos

    // Tipos de mensaje
    public static final int SOLICITUD = 1;
    public static final int INICIO = 2;
    public static final int RECHAZO = 3;
    public static final int LISTO = 4;
    public static final int TURNO = 5;
    public static final int DISPARO = 6;
    public static final int RESULTADO = 7;
}
