import java.util.Random;

/*
 * ============================================================================
 *  BOT DE LA PC: decide a que casilla dispara la computadora
 * ============================================================================
 *
 * FACIL:   siempre dispara al azar (sin repetir casillas).
 *
 * DIFICIL: dispara como lo haria una persona:
 *   1. Si no ha herido a ningun barco, dispara al azar en todo el tablero
 *      (10 x 10 = 100 posibilidades).
 *   2. Si ya le dio a un barco UNA vez, no sabe si el barco esta horizontal
 *      o vertical, asi que solo le quedan 4 casillas: arriba, abajo,
 *      izquierda o derecha.
 *   3. Si ya le dio DOS o mas veces en linea, ya sabe si es horizontal o
 *      vertical, asi que solo le quedan 2 casillas: las dos puntas de la linea.
 *   4. Cuando el barco se hunde, sus casillas pasan a HUNDIDO, ya no quedan
 *      casillas TOCADO y regresa al paso 1.
 *
 * El bot solo ve el TABLERO DE TIRO de la PC (lo que ya disparo y que paso),
 * nunca ve el tablero del jugador: no hace trampa.
 *
 * COMO SE USA (desde ServidorBatalla):
 *     BotPC.elegirTiro(tirosPC, dificultad);
 *     int fila = BotPC.fila;
 *     int columna = BotPC.columna;
 */
public class BotPC {

    static final int FACIL = 0;
    static final int DIFICIL = 1;

    static Random random = new Random();

    // Aqui queda guardada la casilla que eligio el bot
    static int fila;
    static int columna;

    // Lista de casillas posibles para rematar un barco (maximo 4).
    // opcionesFila[0] y opcionesColumna[0] son la opcion 1,
    // opcionesFila[1] y opcionesColumna[1] son la opcion 2, etc.
    static int[] opcionesFila = new int[4];
    static int[] opcionesColumna = new int[4];
    static int numOpciones = 0;


    // ========================================================================
    // ELEGIR TIRO: deja la casilla elegida en BotPC.fila y BotPC.columna
    // ========================================================================
    static void elegirTiro(Tablero tiros, int dificultad) {
        if (dificultad == DIFICIL) {
            boolean encontro = tiroCercaDeUnTocado(tiros);
            if (encontro) {
                return; // ya quedo guardada la casilla para rematar al barco
            }
        }
        // Si es FACIL, o si es DIFICIL pero no hay barco herido: al azar
        tiroAleatorio(tiros);
    }


    // ========================================================================
    // PASO 1: tiro al azar en todo el tablero
    // ========================================================================
    static void tiroAleatorio(Tablero tiros) {
        fila = random.nextInt(Tablero.N);     // numero al azar del 0 al 9
        columna = random.nextInt(Tablero.N);  // numero al azar del 0 al 9

        // Si ya habiamos disparado ahi, sacamos otra casilla hasta encontrar una nueva
        while (tiros.casilla[fila][columna] != Tablero.VACIO) {
            fila = random.nextInt(Tablero.N);
            columna = random.nextInt(Tablero.N);
        }
    }


    // ========================================================================
    // PASOS 2 y 3: rematar a un barco que ya esta herido
    // Regresa true si encontro a donde disparar, false si no hay barco herido.
    // ========================================================================
    static boolean tiroCercaDeUnTocado(Tablero tiros) {

        // ---- Buscamos la primera casilla TOCADO del tablero ----
        int filaTocada = -1;
        int columnaTocada = -1;
        for (int f = 0; f < Tablero.N; f++) {
            for (int c = 0; c < Tablero.N; c++) {
                if (filaTocada == -1 && tiros.casilla[f][c] == Tablero.TOCADO) {
                    filaTocada = f;
                    columnaTocada = c;
                }
            }
        }
        if (filaTocada == -1) {
            return false; // no hay ningun barco herido
        }

        // ---- Empezamos con la lista de opciones vacia ----
        numOpciones = 0;

        // ---- Revisamos si el barco esta horizontal o vertical ----
        // Esta horizontal si a la derecha hay otro TOCADO.
        // Esta vertical si abajo hay otro TOCADO.
        // (Como buscamos de arriba a abajo y de izquierda a derecha, la casilla
        //  que encontramos es la de mas arriba / mas a la izquierda del barco.)
        boolean horizontal = esTocado(tiros, filaTocada, columnaTocada + 1);
        boolean vertical = esTocado(tiros, filaTocada + 1, columnaTocada);

        if (horizontal) {
            // ---- PASO 3: el barco esta HORIZONTAL -> izquierda o derecha ----

            // Opcion 1: la casilla a la izquierda del barco
            agregarOpcion(tiros, filaTocada, columnaTocada - 1);

            // Opcion 2: caminamos a la derecha mientras haya TOCADO
            // y nos quedamos en la primera casilla que ya no es TOCADO
            int c = columnaTocada;
            while (esTocado(tiros, filaTocada, c)) {
                c = c + 1;
            }
            agregarOpcion(tiros, filaTocada, c);

        } else if (vertical) {
            // ---- PASO 3: el barco esta VERTICAL -> arriba o abajo ----

            // Opcion 1: la casilla de arriba del barco
            agregarOpcion(tiros, filaTocada - 1, columnaTocada);

            // Opcion 2: caminamos hacia abajo mientras haya TOCADO
            int f = filaTocada;
            while (esTocado(tiros, f, columnaTocada)) {
                f = f + 1;
            }
            agregarOpcion(tiros, f, columnaTocada);

        } else {
            // ---- PASO 2: solo un golpe -> las 4 casillas de alrededor ----
            agregarOpcion(tiros, filaTocada - 1, columnaTocada); // arriba
            agregarOpcion(tiros, filaTocada + 1, columnaTocada); // abajo
            agregarOpcion(tiros, filaTocada, columnaTocada - 1); // izquierda
            agregarOpcion(tiros, filaTocada, columnaTocada + 1); // derecha
        }

        // Si no quedo ninguna opcion (todas eran agua o fuera del tablero)
        if (numOpciones == 0) {
            return false;
        }

        // Escogemos una de las opciones al azar
        int elegida = random.nextInt(numOpciones);
        fila = opcionesFila[elegida];
        columna = opcionesColumna[elegida];
        return true;
    }


    // ========================================================================
    // AYUDANTES
    // ========================================================================

    // Agrega (f, c) a la lista de opciones, pero SOLO si esta dentro
    // del tablero y todavia no le hemos disparado.
    static void agregarOpcion(Tablero tiros, int f, int c) {
        if (Tablero.dentroDelTablero(f, c) == false) {
            return; // se sale del tablero
        }
        if (tiros.casilla[f][c] != Tablero.VACIO) {
            return; // ya le habiamos disparado
        }
        opcionesFila[numOpciones] = f;
        opcionesColumna[numOpciones] = c;
        numOpciones = numOpciones + 1;
    }

    // true si (f, c) esta dentro del tablero y es TOCADO
    static boolean esTocado(Tablero tiros, int f, int c) {
        if (Tablero.dentroDelTablero(f, c) == false) {
            return false;
        }
        if (tiros.casilla[f][c] == Tablero.TOCADO) {
            return true;
        } else {
            return false;
        }
    }
}
