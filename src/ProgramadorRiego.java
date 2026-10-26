import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * El riego SI es un hilo (extends Thread), como el Cohete del ejemplo 0A.
 * El nombre del hilo se le pasa al constructor de Thread con super(...).
 */
public class ProgramadorRiego extends Thread {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final int PERIODO = 700;
    private static final double LITROS_POR_RIEGO = 2.5;

    private int ciclos;
    private int ciclosHechos = 0;
    private double litros = 0;

    public ProgramadorRiego() {
        this(5);
    }

    public ProgramadorRiego(int ciclos) {
        super("riego");
        this.ciclos = ciclos;
    }

    @Override
    public void run() {
        for (int i = 1; i <= ciclos; i++) {
            String hora = LocalTime.now().format(FORMATO_HORA);
            String estado;
            if (i % 2 == 1) {          // ciclos impares abre, pares cierra
                estado = "ABIERTA";
                litros += LITROS_POR_RIEGO;
            } else {
                estado = "cerrada";
            }
            ciclosHechos++;
            // Aqui si podemos usar getName() porque somos un Thread.
            // OJO: si alguien llama a run() directamente, el hilo que ejecuta es otro (main)
            System.out.printf("[%s] [%s] Riego ciclo %d: válvula %s (total %.1f L)%n",
                    hora, Thread.currentThread().getName(), i, estado, litros);
            try {
                Thread.sleep(PERIODO);
            } catch (InterruptedException e) {
                System.out.println("Riego interrumpido");
                return;
            }
        }
    }

    public String informe() {
        return String.format("%-12s -> ciclos: %d | litros regados: %.1f L", "Riego", ciclosHechos, litros);
    }
}
