import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Clase base de todos los sensores. Es una TAREA (implements Runnable),
 * no un hilo: para ejecutarla en paralelo hay que meterla dentro de un Thread.
 */
public abstract class Sensor implements Runnable {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    protected String nombre;
    protected String unidad;
    protected int periodo;        // ms entre lecturas
    protected int numLecturas;
    protected double umbralMinimo;
    protected double umbralMaximo;

    // Estadisticas propias de cada sensor
    private int lecturasHechas = 0;
    private double suma = 0;
    private double minimo = Double.MAX_VALUE;
    private double maximo = -Double.MAX_VALUE;
    private int alertas = 0;

    public Sensor(String nombre, String unidad, int periodo, int numLecturas,
                  double umbralMinimo, double umbralMaximo) {
        this.nombre = nombre;
        this.unidad = unidad;
        this.periodo = periodo;
        this.numLecturas = numLecturas;
        this.umbralMinimo = umbralMinimo;
        this.umbralMaximo = umbralMaximo;
    }

    // Cada sensor genera su valor de una forma distinta
    public abstract double medir();

    @Override
    public void run() {
        for (int i = 1; i <= numLecturas; i++) {
            double valor = medir();
            // Como no heredamos de Thread no tenemos getName(), hay que preguntar al hilo actual
            String hilo = Thread.currentThread().getName();
            String hora = LocalTime.now().format(FORMATO_HORA);

            String linea = String.format("[%s] [%s] %s lectura %d: %.1f %s",
                    hora, hilo, nombre, i, valor, unidad);

            if (valor < umbralMinimo || valor > umbralMaximo) {
                linea += "  ⚠ ALERTA";
                alertas++;
            }
            System.out.println(linea);

            // Actualizar estadisticas
            lecturasHechas++;
            suma += valor;
            if (valor < minimo) minimo = valor;
            if (valor > maximo) maximo = valor;

            try {
                Thread.sleep(periodo);
            } catch (InterruptedException e) {
                System.out.println(nombre + " interrumpido");
                return;
            }
        }
    }

    public String informe() {
        if (lecturasHechas == 0) {
            return String.format("%-12s -> lecturas: 0 (todavía no ha medido nada)", nombre);
        }
        double media = suma / lecturasHechas;
        return String.format("%-12s -> lecturas: %d | media: %.1f %s | min: %.1f | max: %.1f | alertas: %d",
                nombre, lecturasHechas, media, unidad, minimo, maximo, alertas);
    }
}
