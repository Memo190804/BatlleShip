# Batalla Naval — Guía para entender el código

Todo está en `src/`, sin paquetes (igual que los ejemplos `C`, `S`, `C1`, `S1` de clase).

| Archivo | Qué hace |
|---|---|
| `ServidorBatalla.java` | El servidor **es la PC**. Igual que `S1`: `for(;;)` → `receive` → lee el tipo de mensaje → responde. |
| `ClienteBatalla.java` | El jugador. Manda mensajes cuando presionas botones y tiene un hilo (`run()`) que escucha al servidor. |
| `Protocolo.java` | Números de cada tipo de mensaje, el puerto y el máximo de tiros seguidos (3). |
| `Tablero.java` | Matriz de 10x10: colocar barcos, recibir disparos, saber si ya se hundió todo. |
| `BotPC.java` | Decide a dónde dispara la PC (**dificultad**). |
| `VentanaConexion.java` | Ventana 1: nombre, IP, puerto y dificultad. |
| `VentanaColocacion.java` | Ventana 2: colocar los 7 barcos. |
| `VentanaJuego.java` | Ventana 3: tu tablero, el tablero del rival y el registro. |
| `TableroPanel.java` | La cuadrícula de 10x10 botones que se usa en las ventanas 2 y 3. |

## Cómo viaja un mensaje (lo mismo que `C1`/`S1`)

Para **mandar**:

```java
ByteArrayOutputStream baos = new ByteArrayOutputStream();
DataOutputStream dos = new DataOutputStream(baos);
dos.writeInt(Protocolo.DISPARO);   // 1) siempre primero el tipo
dos.writeInt(fila);                // 2) luego los datos
dos.writeInt(columna);
dos.flush();
enviar(baos.toByteArray());        // 3) se mete en un DatagramPacket y se manda
```

Para **recibir**, se lee **en el mismo orden** en que se escribió:

```java
int tipo = dis.readInt();
int fila = dis.readInt();
int columna = dis.readInt();
```

> Regla de oro: si agregas un dato al mensaje, agrégalo **en los dos lados**
> (el que escribe y el que lee) y en la misma posición. Si no, se leen datos
> revueltos.

## Cómo funciona la dificultad (`BotPC.java`)

- **Fácil:** dispara al azar en todo el tablero.
- **Difícil:**
  1. Mientras no le haya dado a nada, dispara al azar (100 casillas posibles).
  2. Le dio **una vez** a un barco: no sabe si está horizontal o vertical,
     así que solo prueba las **4** casillas de alrededor (arriba, abajo, izquierda, derecha).
  3. Le dio **dos o más veces en línea**: ya sabe la orientación, así que solo
     quedan **2** opciones, las dos puntas de la línea.
  4. Cuando lo hunde, vuelve al paso 1.

El bot solo mira el tablero de tiro de la PC (sus propios disparos), nunca ve
tus barcos.

En 2000 partidas simuladas, la PC necesitó en promedio **~96 tiros** para ganar
en Fácil y **~72** en Difícil.

## Si te piden cambiar algo, ¿qué muevo?

| Quiero… | Dónde |
|---|---|
| Cambiar cuántos tiros seguidos (el Word dice 3) | `Protocolo.java`: `MAX_DISPAROS` |
| Cambiar el puerto | `Protocolo.java`: `PUERTO` (y el texto `"1234"` en `VentanaConexion.java`) |
| Cambiar los barcos (cantidad o tamaño) | `Tablero.java`: los arreglos `NOMBRES` y `TAMANIOS` (deben tener el mismo número de elementos) |
| Permitir barcos pegados | `Tablero.java`, método `casillaLibre`: revisar solo `casilla[f][c]` en vez de las 8 de alrededor. **Ojo:** el bot y `marcarHundido` suponen que los barcos no se pegan |
| Que la PC tire más rápido o más lento | `ServidorBatalla.java`, `dispararPC()`: `Thread.sleep(1000)` (milisegundos) |
| Cambiar colores del tablero | `TableroPanel.java`, método `pintar()` |
| Agregar otra dificultad | `BotPC.java`: nueva constante (ej. `MEDIO = 2`) y su `if` en `elegirTiro`; en `VentanaConexion.java` agregar `"Medio"` a la lista del `JComboBox` y un `if` más donde se revisa `getSelectedItem()` |
