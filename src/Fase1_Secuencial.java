/**
 * Fase 1: version lenta. Llamamos a run() directamente, asi que todo
 * lo ejecuta el hilo main, uno detras de otro.
 */
public class Fase1_Secuencial {

    public static void main(String[] args) {
        Temperatura temperatura = new Temperatura();
        Humedad humedad = new Humedad();
        Luz luz = new Luz();
        ProgramadorRiego riego = new ProgramadorRiego();

        System.out.println("=== FASE 1: invernadero SECUENCIAL ===");
        long inicio = System.currentTimeMillis();

        temperatura.run();
        humedad.run();
        luz.run();
        riego.run();

        long fin = System.currentTimeMillis();

        System.out.println("\n=== INFORME ===");
        System.out.println(temperatura.informe());
        System.out.println(humedad.informe());
        System.out.println(luz.informe());
        System.out.println(riego.informe());
        System.out.println("Tiempo total: " + (fin - inicio) + " ms");
    }
}
