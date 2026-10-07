import java.util.Random;

public class Tablero {

    static final int N = 10;

    static final int VACIO = 0;
    static final int BARCO = 1;
    static final int AGUA = 2;
    static final int TOCADO = 3;
    static final int HUNDIDO = 4;

    static final String[] LETRAS = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};

    static final String[] NOMBRES = {"Submarino", "Acorazado", "Crucero", "Crucero", "Destructor", "Destructor", "Destructor"};
    static final int[] TAMANIOS   = {5,           4,           3,         3,         2,            2,            2};

    int[][] casilla = new int[N][N];

    int[][] numBarco = new int[N][N];

    int[] golpes = new int[TAMANIOS.length];

    int colocados = 0;

    public Tablero() {
        reiniciar();
    }

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

    boolean colocarBarco(int fila, int columna, boolean horizontal) {

        if (colocados == TAMANIOS.length) {
            return false;
        }

        int tamanio = TAMANIOS[colocados];

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
            if (casillaLibre(f, c) == false) {
                return false;
            }
        }

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
            numBarco[f][c] = colocados;
        }

        colocados = colocados + 1;
        return true;
    }

    boolean casillaLibre(int fila, int columna) {
        if (dentroDelTablero(fila, columna) == false) {
            return false;
        }

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

    static boolean dentroDelTablero(int fila, int columna) {
        if (fila < 0) return false;
        if (fila >= N) return false;
        if (columna < 0) return false;
        if (columna >= N) return false;
        return true;
    }

    boolean todosColocados() {
        if (colocados == TAMANIOS.length) {
            return true;
        } else {
            return false;
        }
    }

    void colocarAleatorio() {
        Random random = new Random();
        reiniciar();
        int intentos = 0;
        while (todosColocados() == false) {
            int fila = random.nextInt(N);
            int columna = random.nextInt(N);
            boolean horizontal = random.nextBoolean();
            colocarBarco(fila, columna, horizontal);

            intentos = intentos + 1;
            if (intentos > 1000) {

                reiniciar();
                intentos = 0;
            }
        }
    }

    int recibirDisparo(int fila, int columna) {

        if (casilla[fila][columna] == VACIO) {
            casilla[fila][columna] = AGUA;
            return AGUA;
        }

        if (casilla[fila][columna] == BARCO) {
            int barco = numBarco[fila][columna];
            golpes[barco] = golpes[barco] + 1;

            if (golpes[barco] == TAMANIOS[barco]) {

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

        return casilla[fila][columna];
    }

    boolean todosHundidos() {
        for (int i = 0; i < TAMANIOS.length; i++) {
            if (golpes[i] < TAMANIOS[i]) {
                return false;
            }
        }
        return true;
    }

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

    void marcarHundido(int fila, int columna) {
        casilla[fila][columna] = HUNDIDO;

        int f = fila - 1;
        while (f >= 0 && casilla[f][columna] == TOCADO) {
            casilla[f][columna] = HUNDIDO;
            f = f - 1;
        }

        f = fila + 1;
        while (f < N && casilla[f][columna] == TOCADO) {
            casilla[f][columna] = HUNDIDO;
            f = f + 1;
        }

        int c = columna - 1;
        while (c >= 0 && casilla[fila][c] == TOCADO) {
            casilla[fila][c] = HUNDIDO;
            c = c - 1;
        }

        c = columna + 1;
        while (c < N && casilla[fila][c] == TOCADO) {
            casilla[fila][c] = HUNDIDO;
            c = c + 1;
        }
    }

    static String coordenada(int fila, int columna) {
        String letra = LETRAS[fila];
        int numero = columna + 1;
        return letra + numero;
    }

    static String texto(int resultado) {
        if (resultado == AGUA) return "AGUA";
        if (resultado == TOCADO) return "TOCADO";
        if (resultado == HUNDIDO) return "HUNDIDO";
        return "?";
    }
}
