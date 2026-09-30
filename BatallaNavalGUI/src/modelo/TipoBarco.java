package modelo;

/**
 * Tipos de barco según el punto 1 del requerimiento:
 * 1 acorazado [4], 2 cruceros [3], 3 destructores [2], 1 submarino [5].
 */
public enum TipoBarco {
    ACORAZADO("Acorazado", 4, 1),
    CRUCERO("Crucero", 3, 2),
    DESTRUCTOR("Destructor", 2, 3),
    SUBMARINO("Submarino", 5, 1);

    private final String nombre;
    private final int longitud;
    private final int cantidad;

    TipoBarco(String nombre, int longitud, int cantidad) {
        this.nombre = nombre;
        this.longitud = longitud;
        this.cantidad = cantidad;
    }

    public String getNombre() { return nombre; }
    public int getLongitud() { return longitud; }
    public int getCantidad() { return cantidad; }
}
