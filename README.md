# BatlleShip — Batalla Naval en Java

Práctica 2 de **Redes 2**: juego de Batalla Naval cliente-servidor en Java,
con interfaz gráfica en Swing y comunicación por sockets UDP (`DatagramSocket`).

## Estructura del repositorio

```
BatlleShip/
├── BatallaNavalGUI/
│   ├── README.md                 # Guía para entender y modificar el código
│   └── src/                      # Todo sin paquetes, como los ejemplos de clase
│       ├── ServidorBatalla.java  # Servidor = la PC (UDP, turnos, disparos)
│       ├── ClienteBatalla.java   # Cliente: ventanas + comunicación UDP
│       ├── Protocolo.java        # Tipos de mensaje y regla de los tiros
│       ├── Tablero.java          # Matriz 10x10, barcos y disparos
│       ├── BotPC.java            # Dificultad de la PC (Fácil / Difícil)
│       ├── TableroPanel.java     # Cuadrícula de botones
│       └── Ventana*.java         # Conexión, Colocación, Juego
├── docs/
│   └── Practica2_Batalla_Naval.docx   # Enunciado de la práctica
├── LICENSE
└── README.md
```

## Cómo ejecutarlo

Primero el **servidor** y después el **cliente** (puerto 1234, igual que en clase).

**NetBeans:** `File → New Project → Java with Existing Sources`, selecciona
`BatallaNavalGUI/src`. Ejecuta `ServidorBatalla` (clic derecho → *Run File*)
y luego `ClienteBatalla`.

**Terminal:**

```bash
cd BatallaNavalGUI
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out ServidorBatalla     # terminal 1
java -cp out ClienteBatalla      # terminal 2
```

## Protocolo (sockets de datagrama)

Cada datagrama lleva un `int` con el tipo de mensaje y después sus datos
escritos con `DataOutputStream` (como en los ejemplos `C1`/`S1` de clase):

| Mensaje | Dirección | Datos |
|---|---|---|
| `SOLICITUD` | C → S | nombre, dificultad |
| `INICIO` | S → C | mensaje de bienvenida |
| `RECHAZO` | S → otro cliente | motivo (ya hay partida en curso) |
| `LISTO` | C → S | — |
| `TURNO` | S → C | ¿es tu turno?, tiros restantes |
| `DISPARO` | C ↔ S | fila, columna |
| `RESULTADO` | C ↔ S | fila, columna, resultado (AGUA/TOCADO/HUNDIDO), ¿fin? |

## Reglas

- Como pide el Word (punto 6): hasta **3 tiros seguidos**, o hasta que falles (AGUA).
- Dificultad **Fácil**: la PC tira al azar. **Difícil**: cuando le da a un barco
  prueba las 4 casillas de alrededor y, al saber la orientación, solo las 2 puntas.

## Estado

- [x] Interfaz gráfica (conexión, colocación de barcos, juego)
- [x] Lógica del tablero (colocación, disparos, hundimiento)
- [x] Servidor UDP: un solo cliente, barcos y turno al azar
- [x] La PC dispara sola, con dificultad Fácil o Difícil

## Licencia

MIT — ver [LICENSE](LICENSE).
