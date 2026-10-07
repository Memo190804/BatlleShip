import java.util.Random;

public class BotPC {

    static final int FACIL = 0;
    static final int DIFICIL = 1;

    static Random random = new Random();

    static int fila;
    static int columna;

    static int[] opcionesFila = new int[4];
    static int[] opcionesColumna = new int[4];
    static int numOpciones = 0;

    static void elegirTiro(Tablero tiros, int dificultad) {
        if (dificultad == DIFICIL) {
            boolean encontro = tiroCercaDeUnTocado(tiros);
            if (encontro) {
                return;
            }
        }

        tiroAleatorio(tiros);
    }

    static void tiroAleatorio(Tablero tiros) {
        fila = random.nextInt(Tablero.N);
        columna = random.nextInt(Tablero.N);

        while (tiros.casilla[fila][columna] != Tablero.VACIO) {
            fila = random.nextInt(Tablero.N);
            columna = random.nextInt(Tablero.N);
        }
    }

    static boolean tiroCercaDeUnTocado(Tablero tiros) {

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
            return false;
        }

        numOpciones = 0;

        boolean horizontal = esTocado(tiros, filaTocada, columnaTocada + 1);
        boolean vertical = esTocado(tiros, filaTocada + 1, columnaTocada);

        if (horizontal) {

            agregarOpcion(tiros, filaTocada, columnaTocada - 1);

            int c = columnaTocada;
            while (esTocado(tiros, filaTocada, c)) {
                c = c + 1;
            }
            agregarOpcion(tiros, filaTocada, c);

        } else if (vertical) {

            agregarOpcion(tiros, filaTocada - 1, columnaTocada);

            int f = filaTocada;
            while (esTocado(tiros, f, columnaTocada)) {
                f = f + 1;
            }
            agregarOpcion(tiros, f, columnaTocada);

        } else {

            agregarOpcion(tiros, filaTocada - 1, columnaTocada);
            agregarOpcion(tiros, filaTocada + 1, columnaTocada);
            agregarOpcion(tiros, filaTocada, columnaTocada - 1);
            agregarOpcion(tiros, filaTocada, columnaTocada + 1);
        }

        if (numOpciones == 0) {
            return false;
        }

        int elegida = random.nextInt(numOpciones);
        fila = opcionesFila[elegida];
        columna = opcionesColumna[elegida];
        return true;
    }

    static void agregarOpcion(Tablero tiros, int f, int c) {
        if (Tablero.dentroDelTablero(f, c) == false) {
            return;
        }
        if (tiros.casilla[f][c] != Tablero.VACIO) {
            return;
        }
        opcionesFila[numOpciones] = f;
        opcionesColumna[numOpciones] = c;
        numOpciones = numOpciones + 1;
    }

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
