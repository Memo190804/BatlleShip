/*
 * ============================================================================
 *  PROTOCOLO: las "reglas de la platica" entre el cliente y el servidor
 * ============================================================================
 *
 * Todos los mensajes viajan dentro de un DATAGRAMA. Lo primero que se escribe
 * siempre es un numero (writeInt) que dice que tipo de mensaje es, y despues
 * los datos, igual que en los ejemplos C1/S1 de clase.
 *
 *  Mensaje     Quien lo manda         Datos que lleva despues del tipo
 *  ---------   --------------------   ------------------------------------------
 *  SOLICITUD   cliente -> servidor    nombre (UTF), dificultad (int)
 *  INICIO      servidor -> cliente    saludo (UTF)
 *  RECHAZO     servidor -> otro       aviso de que ya hay alguien jugando (UTF)
 *  LISTO       cliente -> servidor    nada
 *  TURNO       servidor -> cliente    es tu turno (boolean), tiros que te quedan (int)
 *  DISPARO     los dos                fila (int), columna (int)
 *  RESULTADO   los dos                fila (int), columna (int),
 *                                     resultado (int: AGUA, TOCADO o HUNDIDO),
 *                                     se acabo el juego (boolean)
 */
public class Protocolo {

    static final int PUERTO = 1234;     // puerto del servidor (igual que en clase)
    static final int TAM_BUFFER = 512;  // tamanio maximo del datagrama que recibimos

    static final int MAX_DISPAROS = 3;  // punto 6: hasta 3 tiros seguidos o hasta fallar

    // ---- TIPOS DE MENSAJE ----
    static final int SOLICITUD = 1;
    static final int INICIO = 2;
    static final int RECHAZO = 3;
    static final int LISTO = 4;
    static final int TURNO = 5;
    static final int DISPARO = 6;
    static final int RESULTADO = 7;
}
