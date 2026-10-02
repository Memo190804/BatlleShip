package modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Representa un tablero de 10x10. Se usa TANTO para el tablero propio
 * (con los barcos reales) COMO para el tablero de tiro (donde solo se
 * van marcando los resultados de los disparos propios sobre el rival).
 */
public class Tablero {
    public static final int TAMANIO = 10;

    private final EstadoCelda[][] celdas = new EstadoCelda[TAMANIO][TAMANIO];
    private final List<Barco> barcos = new ArrayList<>();

    public Tablero() {
        for (int f = 0; f < TAMANIO; f++)
            for (int c = 0; c < TAMANIO; c++)
                celdas[f][c] = EstadoCelda.VACIO;
    }

    public EstadoCelda getEstado(int fila, int columna) { return celdas[fila][columna]; }

    public boolean esPosicionValida(int fila, int columna) {
        return fila >= 0 && fila < TAMANIO && columna >= 0 && columna < TAMANIO;
    }

    /** Intenta colocar un barco (respetando que no toque a otro). Devuelve true si se pudo colocar. */
    public boolean colocarBarco(TipoBarco tipo, int filaInicial, int columnaInicial, boolean horizontal) {
        List<Coordenada> nuevasCeldas = new ArrayList<>();
        for (int i = 0; i < tipo.getLongitud(); i++) {
            int f = horizontal ? filaInicial : filaInicial + i;
            int c = horizontal ? columnaInicial + i : columnaInicial;
            if (!esPosicionValida(f, c)) return false;
            if (celdas[f][c] != EstadoCelda.VACIO) return false;
            nuevasCeldas.add(new Coordenada(f, c));
        }
        // No permitir que un barco quede pegado a otro (regla clásica de Batalla Naval)
        for (Coordenada coord : nuevasCeldas) {
            for (int df = -1; df <= 1; df++) {
                for (int dc = -1; dc <= 1; dc++) {
                    int nf = coord.getFila() + df;
                    int nc = coord.getColumna() + dc;
                    if (esPosicionValida(nf, nc) && celdas[nf][nc] == EstadoCelda.BARCO
                            && !nuevasCeldas.contains(new Coordenada(nf, nc))) {
                        return false;
                    }
                }
            }
        }
        for (Coordenada coord : nuevasCeldas) {
            celdas[coord.getFila()][coord.getColumna()] = EstadoCelda.BARCO;
        }
        barcos.add(new Barco(tipo, nuevasCeldas));
        return true;
    }

    public int cantidadColocada(TipoBarco tipo) {
        int cont = 0;
        for (Barco b : barcos) if (b.getTipo() == tipo) cont++;
        return cont;
    }

    public boolean todosLosBarcosColocados() {
        for (TipoBarco t : TipoBarco.values()) {
            if (cantidadColocada(t) < t.getCantidad()) return false;
        }
        return true;
    }

    public void colocarBarcosAleatoriamente() {
        Random rnd = new Random();
        for (TipoBarco tipo : TipoBarco.values()) {
            int colocados = cantidadColocada(tipo);
            int intentos = 0;
            while (colocados < tipo.getCantidad() && intentos < 5000) {
                boolean horizontal = rnd.nextBoolean();
                int f = rnd.nextInt(TAMANIO);
                int c = rnd.nextInt(TAMANIO);
                if (colocarBarco(tipo, f, c, horizontal)) colocados++;
                intentos++;
            }
        }
    }

    public void reiniciar() {
        for (int f = 0; f < TAMANIO; f++)
            for (int c = 0; c < TAMANIO; c++)
                celdas[f][c] = EstadoCelda.VACIO;
        barcos.clear();
    }

    /** Procesa un disparo recibido en ESTE tablero (el que tiene los barcos reales). */
    public ResultadoDisparo recibirDisparo(int fila, int columna) {
        if (!esPosicionValida(fila, columna)) return ResultadoDisparo.COORDENADA_INVALIDA;
        EstadoCelda estado = celdas[fila][columna];
        if (estado == EstadoCelda.AGUA || estado == EstadoCelda.TOCADO || estado == EstadoCelda.HUNDIDO) {
            return ResultadoDisparo.YA_DISPARADO;
        }
        Coordenada coord = new Coordenada(fila, columna);
        if (estado == EstadoCelda.VACIO) {
            celdas[fila][columna] = EstadoCelda.AGUA;
            return ResultadoDisparo.AGUA;
        }
        // estado == BARCO
        Barco barcoImpactado = null;
        for (Barco b : barcos) {
            if (b.ocupa(coord)) { barcoImpactado = b; break; }
        }
        if (barcoImpactado == null) {
            celdas[fila][columna] = EstadoCelda.AGUA;
            return ResultadoDisparo.AGUA;
        }
        barcoImpactado.registrarTocado(coord);
        if (barcoImpactado.estaHundido()) {
            for (Coordenada cc : barcoImpactado.getCeldas()) {
                celdas[cc.getFila()][cc.getColumna()] = EstadoCelda.HUNDIDO;
            }
            return ResultadoDisparo.HUNDIDO;
        } else {
            celdas[fila][columna] = EstadoCelda.TOCADO;
            return ResultadoDisparo.TOCADO;
        }
    }

    /** Celdas del barco que ocupa (fila, columna), o lista vacía si ahí no hay barco. */
    public List<Coordenada> getCeldasBarcoEn(int fila, int columna) {
        Coordenada coord = new Coordenada(fila, columna);
        for (Barco b : barcos) {
            if (b.ocupa(coord)) return b.getCeldas();
        }
        return new ArrayList<>();
    }

    public boolean todosHundidos() {
        if (barcos.isEmpty()) return false;
        for (Barco b : barcos) if (!b.estaHundido()) return false;
        return true;
    }

    /** Usado en el TABLERO DE TIRO para reflejar el resultado de un disparo propio. */
    public void marcarResultadoPropio(int fila, int columna, ResultadoDisparo resultado) {
        switch (resultado) {
            case AGUA: celdas[fila][columna] = EstadoCelda.AGUA; break;
            case TOCADO: celdas[fila][columna] = EstadoCelda.TOCADO; break;
            case HUNDIDO: celdas[fila][columna] = EstadoCelda.HUNDIDO; break;
            default: break;
        }
    }
}
