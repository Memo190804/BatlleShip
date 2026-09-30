package modelo;

import java.util.Objects;

/**
 * Representa una coordenada (fila, columna) dentro del tablero de 10x10.
 * Fila: 0-9 (se muestra como A-J en la interfaz)
 * Columna: 0-9 (se muestra como 1-10 en la interfaz)
 */
public class Coordenada {
    private final int fila;
    private final int columna;

    public Coordenada(int fila, int columna) {
        this.fila = fila;
        this.columna = columna;
    }

    public int getFila() { return fila; }
    public int getColumna() { return columna; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coordenada)) return false;
        Coordenada that = (Coordenada) o;
        return fila == that.fila && columna == that.columna;
    }

    @Override
    public int hashCode() { return Objects.hash(fila, columna); }

    @Override
    public String toString() {
        char letraFila = (char) ('A' + fila);
        return "" + letraFila + (columna + 1);
    }
}
