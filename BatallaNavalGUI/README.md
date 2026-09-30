# Batalla Naval — Interfaz Gráfica (Swing)

Esta es la interfaz gráfica completa para el proyecto de Batalla Naval, lista
para pegarse dentro del mismo proyecto de NetBeans donde tu compañero está
trabajando la parte de sockets cliente-servidor.

## Estructura

```
src/
  modelo/
    Coordenada.java      -> (fila, columna) de una celda
    TipoBarco.java        -> enum con los 4 tipos de barco y sus cantidades/longitudes
    EstadoCelda.java       -> VACIO, BARCO, AGUA, TOCADO, HUNDIDO
    ResultadoDisparo.java  -> AGUA, TOCADO, HUNDIDO, YA_DISPARADO, COORDENADA_INVALIDA
    Barco.java             -> un barco individual y sus celdas tocadas
    Tablero.java           -> matriz 10x10, colocación de barcos, lógica de disparo
  vista/
    TableroPanel.java      -> JPanel que dibuja un tablero de 10x10 y captura clicks
    VentanaConexion.java   -> JFrame: nombre de usuario + IP + puerto
    VentanaColocacion.java -> JFrame: colocar los 7 barcos (arrastrar no, click + orientación)
    VentanaJuego.java      -> JFrame: tablero propio + tablero de tiro, turnos, registro
  Main.java                -> demo que encadena las 3 ventanas (aquí van los TODO de red)
```

## Cómo importarlo en NetBeans

**Opción A (más simple): agregar los archivos a tu proyecto existente**
1. En NetBeans, en tu proyecto actual, click derecho sobre `Source Packages` →
   `New` → `Java Package`, crea los paquetes `modelo` y `vista`.
2. Copia cada archivo `.java` de esta carpeta dentro del paquete correspondiente
   (puedes arrastrarlos desde el explorador de archivos de tu SO directo al
   paquete en NetBeans, o copiar/pegar el contenido en clases nuevas).
3. Listo, ya tienes las 3 ventanas disponibles para usarlas desde el código
   de red de tu compañero.

**Opción B: abrir esta carpeta como proyecto nuevo**
1. `File` → `New Project` → `Java with Existing Sources`.
2. En "Source Package Folders" selecciona la carpeta `src` de este ZIP.
3. NetBeans detectará `Main.java` como clase principal.

> Nota: estas clases NO usan el editor visual (`.form`) de NetBeans, están
> escritas a mano con `GridBagLayout`/`BorderLayout`/`BoxLayout`. Funcionan
> igual, simplemente no vas a ver los componentes en el diseñador "Matisse" de
> NetBeans si abres las clases con doble click — para editarlas usa la vista
> de código (`Source`), no la de diseño (`Design`). Si prefieres editarlas
> luego con el diseñador visual, puedo adaptarlas a partir de `.form`s vacíos
> generados por NetBeans; dímelo y te preparo esa versión.

## Cómo se integra con el código de sockets de tu compañero

Diseñé las 3 ventanas para que **no dependan en nada del código de red**. Cada
una expone una interfaz (`listener`) que tu compañero debe implementar, y
métodos públicos que la capa de red debe llamar cuando llegan mensajes del
otro extremo. Todo el flujo está resumido en `Main.java` con comentarios
`// TODO (red): ...` exactamente en los puntos donde va su código.

Resumen del flujo (según los 9 puntos del requerimiento):

1. **VentanaConexion** → botón "Conectar" dispara
   `ConexionListener.onConectar(nombreUsuario, ip, puerto)`. Ahí tu compañero
   abre el `DatagramSocket` y envía la solicitud de juego (punto 2).
2. Cuando el servidor confirme el inicio de juego (punto 3), se cierra esa
   ventana y se abre `VentanaColocacion`.
3. **VentanaColocacion** → el usuario coloca sus 7 barcos (1 acorazado[4],
   2 cruceros[3], 3 destructores[2], 1 submarino[5]) haciendo click en el
   tablero, eligiendo orientación H/V, o con el botón "Colocar
   aleatoriamente". El botón "Listo" se habilita solo cuando ya se colocaron
   todos, y dispara `ColocacionListener.onListo(tableroPropio)` (punto 4).
4. Servidor coloca sus barcos aleatoriamente y decide quién empieza (punto
   5) — esto no requiere GUI del lado cliente, solo se recibe el aviso de
   turno.
5. **VentanaJuego** → tiene el tablero propio (izquierda, no interactivo) y
   el tablero de tiro (derecha, donde se hace click para disparar). Métodos
   clave:
   - `setTurno(boolean esMiTurno)` — habilita/deshabilita el disparo.
   - `setDisparosRestantes(int)` — refleja el máximo de 3 tiros seguidos
     (punto 6).
   - Al hacer click en el tablero de tiro se dispara
     `JuegoListener.onDisparo(fila, columna)` — aquí se envía la coordenada
     al otro extremo (punto 7).
   - Cuando llega el resultado de un disparo propio:
     `ventanaJuego.mostrarResultadoPropio(fila, columna, resultado)`.
   - Cuando llega una coordenada disparada por el rival:
     `ResultadoDisparo r = ventanaJuego.recibirDisparoRival(fila, columna);`
     y ese `r` se debe enviar de vuelta al otro extremo (esto ya calcula
     internamente si fue AGUA/TOCADO/HUNDIDO, punto 8).
   - `ventanaJuego.mostrarFinDeJuego(boolean gano)` cuando el servidor avisa
     que se hundió toda una flota (punto 9).

Las coordenadas siempre se manejan como `fila` (0-9, mostrada como A-J) y
`columna` (0-9, mostrada como 1-10), para que coincidan fácilmente con lo que
tu compañero mande por el socket (puede convertir a texto tipo "A1" o mandar
dos enteros, como prefieran para su protocolo).

## Notas

- El tablero valida que los barcos no queden pegados entre sí (regla clásica
  de Batalla Naval). Si no la quieren, es una sola condición que se puede
  quitar en `Tablero.colocarBarco(...)`.
- `Main.java` es solo una **demostración de flujo** (sin red real, para que
  puedas correr y ver las 3 pantallas). Tu compañero debe reemplazar/ampliar
  esos bloques `TODO` con su código de sockets real.
- Si necesitan un modo consola además del gráfico (la nota final del punto 9
  lo permite como alternativa), avísame y preparo esa versión también.
