# Invernadero con hilos

Práctica tema 4 de PSP 2 DAM — Francisco Javier Saravia Ogazón.

## Qué hace

Simula el invernadero de Pilar y José. Hay tres sensores (temperatura, humedad y luz) y un programador de riego.
Cada sensor toma 5 lecturas con su periodo y avisa con `⚠ ALERTA` si el valor se sale del rango correcto. El riego hace 5 ciclos
(abre, cierra, abre…) y cada vez que abre suma 2,5 litros. Al final se imprime un informe con las estadísticas de cada uno.

| Dispositivo | Cada | Valores | Rango correcto | Implementado como |
|---|---|---|---|---|
| Temperatura | 400 ms | 15 – 35 ºC | 18 – 30 ºC | `Runnable` (hereda de `Sensor`) |
| Humedad | 600 ms | 40 – 90 % | 50 – 80 % | `Runnable` (hereda de `Sensor`) |
| Luz | 500 ms | 0 – 1000 lux | 200 – 900 lux | `Runnable` (hereda de `Sensor`) |
| Riego | 700 ms | abre / cierra | — | `extends Thread` |

Clases:

- `Sensor` → clase abstracta que implementa `Runnable`. Tiene el `run()` con el bucle de lecturas, las estadísticas y `informe()`.
- `Temperatura`, `Humedad`, `Luz` → heredan de `Sensor` y solo implementan `medir()`.
- `ProgramadorRiego` → hereda de `Thread`, su nombre de hilo es `riego` (se lo paso con `super("riego")`).
- `Fase1_Secuencial`, `Fase2_Concurrente`, `Fase3_Experimentos`, `Fase4_Vigilante` → los `main` de cada fase.

## Cómo ejecutarlo

1. Abrir la carpeta del proyecto en IntelliJ con JDK 21.
2. Abrir la clase de la fase que se quiera (`Fase1_Secuencial`, `Fase2_Concurrente`…) y darle al ▶ de su `main`.

Desde terminal también se puede:

```
javac -encoding UTF-8 -d out src/*.java
java -cp out Fase2_Concurrente
```

## Captura

Una ejecución de la Fase 2:

```
=== FASE 2: invernadero CONCURRENTE ===
[09:04:25.094] [riego] Riego ciclo 1: válvula ABIERTA (total 2.5 L)
[09:04:25.092] [sensor-Humedad] Humedad lectura 1: 66.9 %
[09:04:25.092] [sensor-Temperatura] Temperatura lectura 1: 24.4 ºC
[09:04:25.091] [sensor-Luz] Luz lectura 1: 299.1 lux
[09:04:25.561] [sensor-Temperatura] Temperatura lectura 2: 31.2 ºC  ⚠ ALERTA
[09:04:25.662] [sensor-Luz] Luz lectura 2: 410.5 lux
[09:04:25.723] [sensor-Humedad] Humedad lectura 2: 73.0 %
[09:04:25.823] [riego] Riego ciclo 2: válvula cerrada (total 2.5 L)
[09:04:25.965] [sensor-Temperatura] Temperatura lectura 3: 23.6 ºC
[09:04:26.163] [sensor-Luz] Luz lectura 3: 462.9 lux
[09:04:26.324] [sensor-Humedad] Humedad lectura 3: 57.3 %
[09:04:26.366] [sensor-Temperatura] Temperatura lectura 4: 17.3 ºC  ⚠ ALERTA
[09:04:26.524] [riego] Riego ciclo 3: válvula ABIERTA (total 5.0 L)
[09:04:26.663] [sensor-Luz] Luz lectura 4: 943.6 lux  ⚠ ALERTA
[09:04:26.767] [sensor-Temperatura] Temperatura lectura 5: 24.7 ºC
[09:04:26.925] [sensor-Humedad] Humedad lectura 4: 78.4 %
[09:04:27.164] [sensor-Luz] Luz lectura 5: 988.7 lux  ⚠ ALERTA
[09:04:27.225] [riego] Riego ciclo 4: válvula cerrada (total 5.0 L)
[09:04:27.525] [sensor-Humedad] Humedad lectura 5: 84.9 %  ⚠ ALERTA
[09:04:27.928] [riego] Riego ciclo 5: válvula ABIERTA (total 7.5 L)

=== INFORME ===
Temperatura  -> lecturas: 5 | media: 24.2 ºC | min: 17.3 | max: 31.2 | alertas: 2
Humedad      -> lecturas: 5 | media: 72.1 % | min: 57.3 | max: 84.9 | alertas: 1
Luz          -> lecturas: 5 | media: 621.0 lux | min: 299.1 | max: 988.7 | alertas: 2
Riego        -> ciclos: 5 | litros regados: 7.5 L
Tiempo total: 3566 ms
```

## Resultados

| Versión | Ejecución 1 | Ejecución 2 | Ejecución 3 | Teórico |
|---|---|---|---|---|
| Fase 1 (secuencial) | 11 073 ms | — | — | 2 + 3 + 2,5 + 3,5 = **11 s** (suma) |
| Fase 2 (concurrente) | 3 566 ms | 3 556 ms | 3 566 ms | el más lento = riego, 5 × 700 = **3,5 s** (máximo) |

La hipótesis se cumple: en secuencial el tiempo es la **suma** de todos (11 s) y en concurrente es más o menos lo que tarda **el más lento**,
que es el riego (3,5 s). Va unos 3 veces más rápido. Los milisegundos de más son lo que tarda en crear los hilos y en imprimir.

En la Fase 1 todas las líneas del log ponen `[main]`, porque al llamar a `run()` directamente no se crea ningún hilo:
es una llamada normal a un método y la ejecuta el propio `main`.

**¿Sale siempre en el mismo orden el log de la Fase 2?** No. En las tres ejecuciones el orden de la primera tanda cambia
(unas veces sale antes la humedad, otras la luz o el riego), y cuando dos hilos coinciden en el mismo milisegundo
el orden también varía. Lo decide el planificador del sistema operativo, no el orden en que hago los `start()`.
Incluso la hora impresa puede salir "desordenada" (una línea con 25.094 antes que otra con 25.091) porque un hilo coge la hora y otro imprime antes que él.

## Análisis

### ¿Por qué compensa usar hilos en el invernadero?

Porque los sensores se pasan casi todo el tiempo esperando (`sleep`) y no calculando. En la versión secuencial, mientras se espera a la humedad
nadie vigila la temperatura, y si la temperatura sube a 35 ºC no nos enteramos hasta que acabe todo lo demás. Con hilos cada dispositivo
lleva su ritmo, se vigila todo a la vez y además el programa tarda lo que el más lento en lugar de la suma.

### Experimentos A, B y C

**A — Informe sin esperar.** Lanzo la humedad en un hilo y justo después imprimo `informe()`:

```
Informe SIN esperar: Humedad      -> lecturas: 0 (todavía no ha medido nada)
...
Informe DESPUES de join: Humedad  -> lecturas: 5 | media: 63.3 % | min: 50.4 | max: 80.7 | alertas: 1
```

La primera vez sale vacío porque `start()` solo arranca el hilo y vuelve enseguida: `main` sigue a lo suyo y llega al `informe()`
antes de que el sensor haya hecho ninguna lectura. Es lo mismo que pasaba en el ejemplo 0A con el "main: he terminado" que salía antes de la cuenta atrás.
Con `join()` main se queda esperando a que el hilo termine y entonces el informe ya está completo.

**B — `riego.run()` en vez de `riego.start()`.** Con 3 ciclos:

```
[09:04:39.014] [main] Riego ciclo 1: válvula ABIERTA (total 2.5 L)
[09:04:39.715] [main] Riego ciclo 2: válvula cerrada (total 2.5 L)
[09:04:40.416] [main] Riego ciclo 3: válvula ABIERTA (total 5.0 L)
Ha tardado: 2102 ms
Estado del objeto riego: NEW
```

En el log sale `main` y no `riego`, y tarda 2,1 s (3 × 700) bloqueando a main. Es la "prueba" de los cohetes: llamar a `run()` no crea un hilo,
es un método normal. Por eso el objeto sigue en estado `NEW`: aunque su código se ha ejecutado entero, el hilo como tal nunca se ha arrancado.

**C — `start()` dos veces.**

```
Primer riego terminado, estado: TERMINATED
Excepcion capturada: java.lang.IllegalThreadStateException
```

Salta `IllegalThreadStateException`. Un hilo solo se puede arrancar una vez; cuando termina se queda en `TERMINATED` para siempre
y no se puede "reiniciar". Si quiero volver a regar tengo que crear un objeto `ProgramadorRiego` nuevo.

### Estados observados

| Estado | Cuándo | Qué línea lo provoca |
|---|---|---|
| `NEW` | Antes de arrancar los hilos (y en el experimento B) | `new Thread(...)` / `new ProgramadorRiego()`, todavía sin `start()` |
| `RUNNABLE` | Justo al arrancar, mientras el hilo está haciendo la lectura o imprimiendo | `start()` |
| `TIMED_WAITING` | Casi todo el rato: es lo que más se ve en el vigilante | `Thread.sleep(periodo)` dentro de `run()` |
| `BLOCKED` | Me salió en la primera foto del vigilante, justo al arrancar, en 3 de los 4 hilos | `System.out.println` (ver abajo) |
| `TERMINATED` | Cuando cada hilo acaba su bucle; al final todos | fin de `run()` |
| `WAITING` | No apareció en los 4 hilos vigilados | lo tendría `main` mientras hace `join()`, pero no lo vigilo |

Lo que esperaba ver era NEW, RUNNABLE, TIMED_WAITING y TERMINATED. Pensaba que `BLOCKED` no iba a salir porque es de `synchronized`
y eso no lo hemos usado (es de la UD6). Pero en una ejecución salió:

```
[vigilante] sensor-Temperatura=RUNNABLE  sensor-Humedad=BLOCKED  sensor-Luz=BLOCKED  riego=BLOCKED
```

Buscando el porqué: `System.out.println` por dentro es `synchronized`, así que cuando los cuatro hilos quieren imprimir
la primera línea a la vez, uno escribe y los otros tres esperan el cerrojo de la consola. Es muy rápido, por eso solo se ve a veces.
`RUNNABLE` también se ve poco por lo mismo: el hilo trabaja unos milisegundos y se pasa el resto dormido.

### ¿Por qué termina el programa si el vigilante sigue en su bucle?

Porque el vigilante es un hilo **daemon** (`vigilante.setDaemon(true)`, llamado **antes** de `start()`, si no salta excepción).
La JVM termina cuando acaban todos los hilos que no son daemon. Cuando terminan los sensores, el riego y `main`, la JVM cierra
y se lleva por delante al vigilante aunque esté en su `while (true)`. Al final se ve que el vigilante sigue `TIMED_WAITING` y `daemon = true`
justo antes de que el programa acabe. Sin el `setDaemon(true)` el programa no terminaría nunca.

### Thread frente a Runnable

| Aspecto | `extends Thread` (riego) | `implements Runnable` (sensores) |
|---|---|---|
| Qué representa el objeto | Es el hilo en sí | Es una tarea, el trabajo a hacer |
| Cómo se arranca | `riego.start()` directamente | Hay que meterlo en un hilo: `new Thread(sensor, "nombre").start()` |
| Nombre del hilo | `super("riego")` en el constructor y `getName()` | Se da en el `new Thread(..., "sensor-Luz")` y se lee con `Thread.currentThread().getName()` |
| Herencia | Gasta la única herencia que tiene Java | Deja libre la herencia para otra clase |
| Reutilizar | Un objeto = una sola ejecución (ver experimento C) | El mismo objeto tarea se puede pasar a otro `Thread` nuevo |

**¿Por qué los sensores SOLO pueden ser Runnable en mi diseño?** Porque ya heredan de `Sensor` (para reutilizar el `run()`, las estadísticas
y el `informe()`), y Java no tiene herencia múltiple. Si `Temperatura` extends `Sensor`, ya no puede extender también de `Thread`.
La única forma de que sean ejecutables en un hilo es que `Sensor` implemente `Runnable`.

**¿Qué forma elegiría para el riego?** También `Runnable`. Con `extends Thread` funciona porque el riego no hereda de nada más,
pero me parece más limpio separar la tarea del hilo: así todos los dispositivos se lanzan igual, el riego podría heredar de una
clase `Dispositivo` en el futuro, y si quiero volver a regar no tengo que crear un objeto nuevo entero. Para algo tan simple
`extends Thread` es más rápido de escribir, pero `Runnable` es más flexible.
