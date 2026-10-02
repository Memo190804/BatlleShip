# BatlleShip — Batalla Naval en Java

Práctica 2 de **Redes 2**: juego de Batalla Naval cliente-servidor en Java,
con interfaz gráfica en Swing y comunicación por sockets UDP (`DatagramSocket`).

## Estructura del repositorio

```
BatlleShip/
├── BatallaNavalGUI/          # Interfaz gráfica (Swing)
│   ├── README.md             # Detalle de clases e integración con la red
│   └── src/
│       ├── ServidorBatalla.java  # Servidor = la PC (UDP, turnos, disparos)
│       ├── ClienteBatalla.java   # Cliente: ventanas + comunicación UDP
│       ├── red/Protocolo.java    # Tipos de mensaje que viajan en los datagramas
│       ├── modelo/           # Lógica del juego: Tablero, Barco, Coordenada...
│       └── vista/            # Ventanas: Conexión, Colocación, Juego
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
| `SOLICITUD` | C → S | nombre del usuario |
| `INICIO` | S → C | mensaje de bienvenida |
| `RECHAZO` | S → otro cliente | motivo (ya hay partida en curso) |
| `LISTO` | C → S | — |
| `TURNO` | S → C | ¿es tu turno?, disparos restantes |
| `DISPARO` | C ↔ S | fila, columna |
| `RESULTADO` | C ↔ S | fila, columna, resultado, ¿fin?, celdas del barco hundido |

## Estado

- [x] Interfaz gráfica (conexión, colocación de barcos, juego)
- [x] Lógica del tablero (colocación, disparos, hundimiento)
- [x] Servidor UDP: un solo cliente, barcos y turno al azar, hasta 3 tiros
- [x] La PC dispara sola (al azar, sin repetir casillas)

## Licencia

MIT — ver [LICENSE](LICENSE).
