import java.util.Random;

/*
 * ============================================================================
 *  TABLERO de 10x10
 * ============================================================================
 *
 * Se usa para dos cosas (punto 1 de la practica):
 *   - Tablero propio: donde estan MIS barcos.
 *   - Tablero de tiro: donde anoto a donde dispare yo y que paso.
 *
 * Cada casilla guarda un numero que dice que hay en ella:
 *   0 = VACIO, 1 = BARCO, 2 = AGUA (disparo fallado), 3 = TOCADO, 4 = HUNDIDO
 */
public class Tablero {

    static final int N = 10; // el tablero es de N x N (10 x 10)

    // Lo que puede haber en una casilla
    static final int VACIO = 0;
    static final int BARCO = 1;
    static final int AGUA = 2;
    static final int TOCADO = 3;
    static final int HUNDIDO = 4;

    // Letras de las filas: la fila 0 es la "A", la fila 1 es la "B", etc.
    static final String[] LETRAS = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};

    // Los 7 barcos del punto 1, en el orden en que se colocan.
    // NOMBRES[0] tiene el tamanio TAMANIOS[0], NOMBRES[1] tiene TAMANIOS[1], etc.
    static final String[] NOMBRES = {"Submarino", "Acorazado", "Crucero", "Crucero", "Destructor", "Destructor", "Destructor"};
    static final int[] TAMANIOS   = {5,           4,           3,         3,         2,            2,            2};

    // casilla[fila][columna] dice que hay en esa casilla (VACIO, BARCO, AGUA...)
    int[][] casilla = new int[N][N];

    // numBarco[fila][columna] dice QUE barco esta ahi (0 = el submarino, 1 = el acorazado...)
    // Si no hay barco vale -1.
    int[][] numBarco = new int[N][N];

    // golpes[0] = cuantos golpes lleva el barco 0, golpes[1] = el barco 1, etc.
    int[] golpes = new int[TAMANIOS.length];

    // Cuantos barcos llevamos colocados (de 0 a 7)
    int colocados = 0;


    // Constructor: se ejecuta al hacer "new Tablero()". Deja todo vacio.
    public Tablero() {
        reiniciar();
    }

    // Deja el tablero vacio, sin barcos
    void reiniciar() {
        for (int f = 0; f < N; f++) {
            for (int c = 0; c < N; c++) {
                casilla[f][c] = VACIO;
                numBarco[f][c] = -1;
            }
        }
        for (int i = 0; i < golpes.length; i++) {
            golpes[i] = 0;
        }
        colocados = 0;
    }


    // ========================================================================
    // COLOCAR BARCOS
    // ========================================================================

    // Coloca el SIGUIENTE barco que falta, empezando en (fila, columna).
    // Si es horizontal crece hacia la derecha, si es vertical crece hacia abajo.
    // Regresa true si se pudo colocar y false si no.
    boolean colocarBarco(int fila, int columna, boolean horizontal) {
        // Si ya estan los 7 barcos, no hay nada que colocar
        if (colocados == TAMANIOS.length) {
            return false;
        }

        // El tamanio del barco que toca colocar
        int tamanio = TAMANIOS[colocados];

        // PASO 1: revisamos que TODAS las casillas del barco esten libres.
        // Si una sola no esta libre, no se puede colocar.
        for (int i = 0; i < tamanio; i++) {
            int f;
            int c;
            if (horizontal) {
                f = fila;         // la fila no cambia
                c = columna + i;  // avanzamos a la derecha
            } else {
                f = fila + i;     // avanzamos hacia abajo
                c = columna;      // la columna no cambia
            }
            if (casillaLibre(f, c) == false) {
                return false;
            }
        }

        // PASO 2: si todas estaban libres, ahora si ponemos el barco
        for (int i = 0; i < tamanio; i++) {
            int f;
            int c;
            if (horizontal) {
                f = fila;
                c = columna + i;
            } else {
                f = fila + i;
                c = columna;
            }
            casilla[f][c] = BARCO;
            numBarco[f][c] = colocados; // anotamos que numero de barco es
        }

        colocados = colocados + 1;
        return true;
    }

    // Una casilla esta libre si:
    //   1. esta dentro del tablero, y
    //   2. ni ella ni las 8 casillas de alrededor tienen barco
    //      (en Batalla Naval los barcos no se pueden pegar).
    boolean casillaLibre(int fila, int columna) {
        if (dentroDelTablero(fila, columna) == false) {
            return false;
        }
        // Revisamos el cuadrito de 3x3 que rodea a la casilla
        for (int f = fila - 1; f <= fila + 1; f++) {
            for (int c = columna - 1; c <= columna + 1; c++) {
                if (dentroDelTablero(f, c)) {
                    if (casilla[f][c] == BARCO) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // true si la casilla existe (fila y columna entre 0 y 9)
    static boolean dentroDelTablero(int fila, int columna) {
        if (fila < 0) return false;
        if (fila >= N) return false;
        if (columna < 0) return false;
        if (columna >= N) return false;
        return true;
    }

    // true si ya se colocaron los 7 barcos
    boolean todosColocados() {
        if (colocados == TAMANIOS.length) {
            return true;
        } else {
            return false;
        }
    }

    // Coloca los 7 barcos al azar (lo usa la PC y el boton "Colocar aleatoriamente")
    void colocarAleatorio() {
        Random random = new Random();
        reiniciar();
        int intentos = 0;
        while (todosColocados() == false) {
            int fila = random.nextInt(N);               // numero al azar del 0 al 9
            int columna = random.nextInt(N);            // numero al azar del 0 al 9
            boolean horizontal = random.nextBoolean();  // true o false al azar
            colocarBarco(fila, columna, horizontal);    // si no cabe, no pasa nada y se intenta otra vez

            intentos = intentos + 1;
            if (intentos > 1000) {
                // Casi nunca pasa: el barco que sigue ya no cabe en ningun lado.
                // Borramos todo y empezamos de nuevo.
                reiniciar();
                intentos = 0;
            }
        }
    }


    // ========================================================================
    // DISPAROS
    // ========================================================================

    // Me dispararon en (fila, columna). Reviso que habia y regreso
    // AGUA, TOCADO o HUNDIDO.
    int recibirDisparo(int fila, int columna) {

        // Caso 1: no habia nada -> AGUA
        if (casilla[fila][columna] == VACIO) {
            casilla[fila][columna] = AGUA;
            return AGUA;
        }

        // Caso 2: habia un barco -> TOCADO o HUNDIDO
        if (casilla[fila][columna] == BARCO) {
            int barco = numBarco[fila][columna];   // que barco es
            golpes[barco] = golpes[barco] + 1;     // le sumamos un golpe

            if (golpes[barco] == TAMANIOS[barco]) {
                // Ya le dieron en TODAS sus casillas: se hundio.
                // Buscamos todas las casillas de ese barco y las marcamos HUNDIDO.
                for (int f = 0; f < N; f++) {
                    for (int c = 0; c < N; c++) {
                        if (numBarco[f][c] == barco) {
                            casilla[f][c] = HUNDIDO;
                        }
                    }
                }
                return HUNDIDO;
            } else {
                casilla[fila][columna] = TOCADO;
                return TOCADO;
            }
        }

        // Caso 3: ya se habia disparado ahi antes. Regresamos lo que ya habia.
        return casilla[fila][columna];
    }

    // true si ya no queda ningun barco a flote
    boolean todosHundidos() {
        for (int i = 0; i < TAMANIOS.length; i++) {
            if (golpes[i] < TAMANIOS[i]) {
                return false; // este barco todavia no se hunde
            }
        }
        return true;
    }

    // Se usa en el TABLERO DE TIRO: anoto lo que paso con MI disparo
    void anotarTiro(int fila, int columna, int resultado) {
        if (resultado == AGUA) {
            casilla[fila][columna] = AGUA;
        }
        if (resultado == TOCADO) {
            casilla[fila][columna] = TOCADO;
        }
        if (resultado == HUNDIDO) {
            marcarHundido(fila, columna);
        }
    }

    // En el tablero de tiro no sabemos donde estan los barcos del rival,
    // solo sabemos que casillas estan TOCADO. Como los barcos no se pueden pegar,
    // las casillas TOCADO que estan en linea con (fila, columna) son del mismo
    // barco, asi que todas pasan a HUNDIDO.
    void marcarHundido(int fila, int columna) {
        casilla[fila][columna] = HUNDIDO;

        // Hacia arriba
        int f = fila - 1;
        while (f >= 0 && casilla[f][columna] == TOCADO) {
            casilla[f][columna] = HUNDIDO;
            f = f - 1;
        }

        // Hacia abajo
        f = fila + 1;
        while (f < N && casilla[f][columna] == TOCADO) {
            casilla[f][columna] = HUNDIDO;
            f = f + 1;
        }

        // Hacia la izquierda
        int c = columna - 1;
        while (c >= 0 && casilla[fila][c] == TOCADO) {
            casilla[fila][c] = HUNDIDO;
            c = c - 1;
        }

        // Hacia la derecha
        c = columna + 1;
        while (c < N && casilla[fila][c] == TOCADO) {
            casilla[fila][c] = HUNDIDO;
            c = c + 1;
        }
    }


    // ========================================================================
    // TEXTOS (para mostrar en consola y en la ventana)
    // ========================================================================

    // Convierte (fila, columna) a texto:  (0, 0) -> "A1",  (2, 9) -> "C10"
    static String coordenada(int fila, int columna) {
        String letra = LETRAS[fila];
        int numero = columna + 1; // las columnas se muestran del 1 al 10
        return letra + numero;
    }

    // Convierte el resultado a texto para mostrarlo
    static String texto(int resultado) {
        if (resultado == AGUA) return "AGUA";
        if (resultado == TOCADO) return "TOCADO";
        if (resultado == HUNDIDO) return "HUNDIDO";
        return "?";
    }
}
