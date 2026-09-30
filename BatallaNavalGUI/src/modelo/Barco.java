package modelo;

import java.util.ArrayList;
import java.util.List;

public class Barco {
    private final TipoBarco tipo;
    private final List<Coordenada> celdas = new ArrayList<>();
    private final List<Coordenada> celdasTocadas = new ArrayList<>();

    public Barco(TipoBarco tipo, List<Coordenada> celdas) {
        this.tipo = tipo;
        this.celdas.addAll(celdas);
    }

    public TipoBarco getTipo() { return tipo; }
    public List<Coordenada> getCeldas() { return celdas; }

    public boolean ocupa(Coordenada c) { return celdas.contains(c); }

    public void registrarTocado(Coordenada c) {
        if (ocupa(c) && !celdasTocadas.contains(c)) celdasTocadas.add(c);
    }

    public boolean estaHundido() { return celdasTocadas.size() >= celdas.size(); }
}
