/**
 * Fase 2: cada sensor en su propio hilo + el riego (que ya es un hilo).
 * main espera con join() a que acaben los cuatro antes del informe.
 */
public class Fase2_Concurrente {

    public static void main(String[] args) throws InterruptedException {
        Temperatura temperatura = new Temperatura();
        Humedad humedad = new Humedad();
        Luz luz = new Luz();
        ProgramadorRiego riego = new ProgramadorRiego();   // ya es un hilo, no hace falta new Thread

        Thread hiloTemp = new Thread(temperatura, "sensor-Temperatura");
        Thread hiloHum = new Thread(humedad, "sensor-Humedad");
        Thread hiloLuz = new Thread(luz, "sensor-Luz");

        System.out.println("=== FASE 2: invernadero CONCURRENTE ===");
        long inicio = System.currentTimeMillis();

        hiloTemp.start();
        hiloHum.start();
        hiloLuz.start();
        riego.start();

        // Esperamos a los cuatro
        hiloTemp.join();
        hiloHum.join();
        hiloLuz.join();
        riego.join();

        long fin = System.currentTimeMillis();

        System.out.println("\n=== INFORME ===");
        System.out.println(temperatura.informe());
        System.out.println(humedad.informe());
        System.out.println(luz.informe());
        System.out.println(riego.informe());
        System.out.println("Tiempo total: " + (fin - inicio) + " ms");
    }
}
