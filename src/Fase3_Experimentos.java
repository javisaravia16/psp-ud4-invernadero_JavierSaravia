public class Fase3_Experimentos {

    public static void main(String[] args) throws InterruptedException {
        experimentoA();
        experimentoB();
        experimentoC();
    }

    // A: informe sin esperar y luego esperando
    private static void experimentoA() throws InterruptedException {
        System.out.println("\n===== EXPERIMENTO A: informe sin join =====");
        Humedad humedad = new Humedad();
        Thread hilo = new Thread(humedad, "sensor-Humedad");
        hilo.start();

        System.out.println("Informe SIN esperar: " + humedad.informe());

        hilo.join();
        System.out.println("Informe DESPUES de join: " + humedad.informe());
    }

    // B: llamar a run() en vez de start()
    private static void experimentoB() {
        System.out.println("\n===== EXPERIMENTO B: riego.run() en vez de start() =====");
        ProgramadorRiego riego = new ProgramadorRiego(3);

        long inicio = System.currentTimeMillis();
        riego.run();
        long fin = System.currentTimeMillis();

        System.out.println("Ha tardado: " + (fin - inicio) + " ms");
        System.out.println("Estado del objeto riego: " + riego.getState());
    }

    // C: llamar dos veces a start() sobre el mismo objeto
    private static void experimentoC() throws InterruptedException {
        System.out.println("\n===== EXPERIMENTO C: start() dos veces =====");
        ProgramadorRiego riego = new ProgramadorRiego(2);
        riego.start();
        riego.join();
        System.out.println("Primer riego terminado, estado: " + riego.getState());

        try {
            riego.start();
        } catch (IllegalThreadStateException e) {
            System.out.println("Excepcion capturada: " + e);
            System.out.println("Un hilo que ya ha terminado no se puede volver a arrancar.");
        }
    }
}
