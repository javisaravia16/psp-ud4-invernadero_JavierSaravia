# Invernadero con hilos

Práctica tema 4 de PSP 2 DAM — Francisco Javier Saravia Ogazón

## Qué hace

El programa simula el invernadero de Pilar y José. Hay tres sensores (temperatura, humedad y luz) y un riego automático. Cada sensor hace 5 lecturas y, si el valor se sale de lo normal, escribe un aviso de ALERTA. El riego abre y cierra la válvula 5 veces y cada vez que la abre echa 2,5 litros. Cuando todo termina, imprime un informe con la media, el mínimo, el máximo y las alertas de cada sensor.

Lo que cuenta la práctica es comparar dos formas de hacerlo: todo seguido, uno detrás de otro, o todo a la vez con hilos.

Las clases son estas:

- **Sensor**: clase abstracta con lo que tienen en común todos los sensores (el bucle de lecturas, las estadísticas y el informe). Implementa Runnable.
- **Temperatura, Humedad y Luz**: heredan de Sensor y solo cambia el rango de valores que generan.
- **ProgramadorRiego**: el riego. Este hereda de Thread, y le pongo el nombre "riego" al hilo.
- **Fase1_Secuencial, Fase2_Concurrente, Fase3_Experimentos y Fase4_Vigilante**: un main por cada fase de la práctica.

Los datos de cada uno:

- Temperatura: mide cada 400 ms, valores entre 15 y 35 ºC, lo normal es de 18 a 30.
- Humedad: cada 600 ms, valores entre 40 y 90 %, lo normal es de 50 a 80.
- Luz: cada 500 ms, valores entre 0 y 1000 lux, lo normal es de 200 a 900.
- Riego: cada 700 ms abre o cierra la válvula.

## Cómo ejecutarlo

Se abre el proyecto en IntelliJ con el JDK 21, se abre la clase de la fase que se quiera probar y se le da al botón verde de play que sale al lado del main.

Si se prefiere desde la terminal:

    javac -encoding UTF-8 -d out src/*.java
    java -cp out Fase2_Concurrente

## Captura

Esto es una ejecución de la Fase 2 (la concurrente):

    === FASE 2: invernadero CONCURRENTE ===
    [09:04:25.094] [riego] Riego ciclo 1: válvula ABIERTA (total 2.5 L)
    [09:04:25.092] [sensor-Humedad] Humedad lectura 1: 66.9 %
    [09:04:25.092] [sensor-Temperatura] Temperatura lectura 1: 24.4 ºC
    [09:04:25.091] [sensor-Luz] Luz lectura 1: 299.1 lux
    [09:04:25.561] [sensor-Temperatura] Temperatura lectura 2: 31.2 ºC  ⚠ ALERTA
    [09:04:25.662] [sensor-Luz] Luz lectura 2: 410.5 lux
    [09:04:25.723] [sensor-Humedad] Humedad lectura 2: 73.0 %
    [09:04:25.823] [riego] Riego ciclo 2: válvula cerrada (total 2.5 L)
    ...
    [09:04:27.928] [riego] Riego ciclo 5: válvula ABIERTA (total 7.5 L)

    === INFORME ===
    Temperatura  -> lecturas: 5 | media: 24.2 ºC | min: 17.3 | max: 31.2 | alertas: 2
    Humedad      -> lecturas: 5 | media: 72.1 % | min: 57.3 | max: 84.9 | alertas: 1
    Luz          -> lecturas: 5 | media: 621.0 lux | min: 299.1 | max: 988.7 | alertas: 2
    Riego        -> ciclos: 5 | litros regados: 7.5 L
    Tiempo total: 3566 ms

## Resultados

Tiempos que me han salido:

- Fase 1 (todo seguido): 11073 ms, o sea, unos 11 segundos.
- Fase 2 (con hilos): 3566 ms, 3556 ms y 3566 ms en tres ejecuciones, unos 3,5 segundos.

La idea de la práctica era comprobar que en secuencial el tiempo es la suma de todo y con hilos es lo que tarda el más lento. Y sale justo así. Seguido tarda 2 + 3 + 2,5 + 3,5 = 11 segundos. Con hilos todo va a la vez y solo hay que esperar al riego, que es el más lento (5 ciclos de 700 ms = 3,5 s). Va más o menos tres veces más rápido.

Otra cosa que se ve en la Fase 1: todas las líneas del log dicen [main]. Es porque ahí llamo a run() directamente, y eso no crea ningún hilo nuevo, es como llamar a cualquier método normal, así que lo hace todo el hilo principal.

Y sobre el orden del log en la Fase 2: no sale siempre igual. En las tres veces que lo ejecuté, el orden de las primeras líneas cambiaba (a veces salía antes la humedad, otras la luz o el riego). Eso lo decide el sistema operativo, no yo, aunque yo arranque los hilos siempre en el mismo orden.

## Análisis

### Por qué compensa usar hilos aquí

Porque los sensores se pasan casi todo el rato esperando entre una lectura y la siguiente. Si lo hago seguido, mientras espero a la humedad nadie mira la temperatura, y si se pasa de 30 grados no me entero hasta mucho después. Con hilos cada cosa va a su ritmo, se vigila todo a la vez y además el programa termina antes.

### Los tres experimentos

**Experimento A: sacar el informe sin esperar.** Lanzo la humedad en un hilo y enseguida imprimo el informe. Sale con 0 lecturas ("todavía no ha medido nada"). Pasa porque start() solo pone el hilo en marcha y el main sigue su camino sin esperar, así que llega al informe antes de que el sensor haya medido nada. Es parecido a lo que pasaba en el ejemplo de los cohetes, donde main decía que había terminado antes de que acabara la cuenta atrás. Cuando uso join(), el main espera a que el hilo acabe y entonces el informe sale completo (5 lecturas, media 63,3 %).

**Experimento B: llamar a run() en vez de start().** Hago un riego de 3 ciclos con riego.run(). El log dice [main] en vez de [riego], tarda unos 2,1 segundos bloqueando el programa, y el estado del objeto al final es NEW. Es decir, el código del riego se ejecuta, pero no se ha creado ningún hilo de verdad. Esa es la gracia: run() es un método normal y el que crea el hilo es start().

**Experimento C: llamar a start() dos veces.** Arranco un riego, espero a que termine (queda en TERMINATED) y vuelvo a llamar a start() con el mismo objeto. Salta la excepción IllegalThreadStateException. Quiere decir que un hilo solo se puede arrancar una vez, y cuando acaba ya no se puede reutilizar. Si quiero regar otra vez tengo que crear un objeto riego nuevo.

### Estados que he visto

- **NEW**: antes de arrancar los hilos, cuando solo he hecho el new. También sale en el experimento B.
- **RUNNABLE**: justo al arrancar, cuando el hilo está haciendo la lectura o escribiendo. Se ve poco porque dura muy poquito.
- **TIMED_WAITING**: es el que más sale, casi todo el rato. Es cuando el hilo está dormido en el Thread.sleep() esperando a la siguiente lectura.
- **BLOCKED**: este no me lo esperaba. Pensaba que no iba a salir porque se usa con synchronized, que se ve en una unidad posterior. Pero en una ejecución, en la primera foto del vigilante, tres de los hilos salieron BLOCKED. Creo que es porque System.out.println por dentro está sincronizado, y como los cuatro hilos querían escribir en la consola a la vez, uno escribía y los otros esperaban su turno.
- **TERMINATED**: cuando cada hilo acaba su run(). Al final salen todos así.
- **WAITING**: no me ha salido en ninguno de los cuatro hilos que vigilo. Lo tendría el main mientras espera con join(), pero a ese no lo vigilo.

### Por qué termina el programa aunque el vigilante no para

El vigilante tiene un while(true) y nunca acaba por sí solo, pero el programa termina igualmente porque lo he puesto como hilo daemon con setDaemon(true) (hay que hacerlo antes del start(), si no da error). Java cierra el programa cuando se acaban todos los hilos normales, y a los daemon no los espera. Así que cuando acaban los sensores, el riego y el main, el programa se cierra y se lleva al vigilante por delante. Si no fuera daemon, el programa se quedaría colgado para siempre.

### Thread frente a Runnable

Con Thread (el riego), la clase es directamente el hilo. Se arranca con riego.start() y el nombre se le pone en el constructor con super("riego"). Con Runnable (los sensores), la clase es solo la tarea, el trabajo que hay que hacer, y hay que meterla dentro de un Thread para ejecutarla: new Thread(sensor, "sensor-Luz").start(). Como no es un Thread, para saber el nombre del hilo que la ejecuta hay que preguntar con Thread.currentThread().getName().

Otras diferencias que he notado:

- Con extends Thread gastas la única herencia que tiene Java. Con Runnable la clase todavía puede heredar de otra.
- Un objeto Thread solo se puede arrancar una vez (lo del experimento C). La tarea Runnable se puede pasar a un hilo nuevo cuando quieras.
- Thread es más corto de escribir. Runnable es más flexible.

**Por qué los sensores solo pueden ser Runnable en mi diseño:** porque todos heredan de la clase Sensor, para no repetir el bucle de lecturas, las estadísticas y el informe en cada uno. Java no deja heredar de dos clases a la vez, así que no pueden heredar también de Thread. La única forma de que se puedan ejecutar en un hilo es que Sensor implemente Runnable.

**Qué haría yo con el riego:** lo haría también con Runnable. Funciona bien con Thread porque no hereda de nada más, pero así todo el invernadero se lanza igual, y si en el futuro el riego tiene que heredar de otra clase (por ejemplo una clase Dispositivo) no habría que cambiarlo. Para algo tan sencillo Thread vale, pero Runnable me parece más ordenado.
