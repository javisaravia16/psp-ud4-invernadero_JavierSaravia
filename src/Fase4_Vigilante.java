/**
 * Fase 4: un quinto hilo (vigilante) que cada 300 ms muestra el estado
 * de los otros cuatro. Tiene un while(true), asi que lo hacemos DAEMON
 * para que la JVM no le espere y el programa termine solo.
 */
public class Fase4_Vigilante {

    public static void main(String[] args) throws InterruptedException {
        Thread hiloTemp = new Thread(new Temperatura(), "sensor-Temperatura");
        Thread hiloHum = new Thread(new Humedad(), "sensor-Humedad");
        Thread hiloLuz = new Thread(new Luz(), "sensor-Luz");
        ProgramadorRiego riego = new ProgramadorRiego();

        Thread[] hilos = {hiloTemp, hiloHum, hiloLuz, riego};

        Thread vigilante = new Thread(new Vigilante(hilos), "vigilante");
        vigilante.setDaemon(true);   // hay que hacerlo ANTES de start()

        System.out.println("=== FASE 4: vigilante de estados ===");
        System.out.println("Estados ANTES de arrancar:");
        mostrarEstados(hilos);

        for (Thread h : hilos) {
            h.start();
        }
        vigilante.start();

        for (Thread h : hilos) {
            h.join();
        }

        System.out.println("\nEstados DESPUES de esperarlos:");
        mostrarEstados(hilos);
        System.out.println("Vigilante: " + vigilante.getState() + " (daemon = " + vigilante.isDaemon() + ")");
        System.out.println("main termina -> la JVM no espera al vigilante porque es daemon");
    }

    static void mostrarEstados(Thread[] hilos) {
        for (Thread h : hilos) {
            System.out.println("   " + h.getName() + ": " + h.getState());
        }
    }
}

class Vigilante implements Runnable {

    private Thread[] hilos;

    public Vigilante(Thread[] hilos) {
        this.hilos = hilos;
    }

    @Override
    public void run() {
        while (true) {
            StringBuilder sb = new StringBuilder("   [vigilante] ");
            for (Thread h : hilos) {
                sb.append(h.getName()).append("=").append(h.getState()).append("  ");
            }
            System.out.println(sb);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
