/* ----------------------------------------------------------------------------
    Problema de la cena de los filósofos
-------------------------------------------------------------------------------
- Ambiente grafico (GUI)
- Controles y acciones
- Monitoreo de los estados de los procesos
- Monitoreo de los recursos compartidos
------------------------------------------------------------------------------- */
import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Semaphore;

public class CenaFilosofos extends JFrame {
    private static final int NUM_FILOSOFOS = 5; // Número de filósofos
    private static Filosofo[] filosofos = new Filosofo[NUM_FILOSOFOS]; // Arreglo de filósofos
    private static Tenedor[] tenedores = new Tenedor[NUM_FILOSOFOS]; // Arreglo de tenedores
    private static MesaPanel mesaPanel = new MesaPanel();

    public static void main(String[] args) {
        // Configuración de la GUI
        CenaFilosofos frame = new CenaFilosofos();
        frame.setTitle("Cena de los Filósofos");
        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Crear tenedores
        for (int i = 0; i < NUM_FILOSOFOS; i++) {
            tenedores[i] = new Tenedor(i);
        }

        // Crear y comenzar los hilos de los filósofos
        for (int i = 0; i < NUM_FILOSOFOS; i++) {
            filosofos[i] = new Filosofo(i, tenedores[i], tenedores[(i + 1) % NUM_FILOSOFOS], mesaPanel);
            Thread t = new Thread(filosofos[i], "Filosofo " + (i + 1));
            t.start();
        }
    }

    // Constructor para la GUI
    public CenaFilosofos() {
        add(mesaPanel);
    }
}

// Panel para dibujar la mesa, los filósofos y los tenedores
class MesaPanel extends JPanel {
    private static final int RADIO_FILOSOFO = 40; // Tamaño de los círculos que representan a los filósofos
    private Color[] estadoFilosofos = new Color[5]; // Colores de los filósofos
    private boolean[] estadoTenedores = new boolean[5]; // Estado de los tenedores (ocupado o no)

    public MesaPanel() {
        // Inicializar los filósofos en estado de "pensar" (color gris)
        for (int i = 0; i < 5; i++) {
            estadoFilosofos[i] = Color.GRAY;
        }
    }

    // Cambiar el estado de un filósofo (pensando o comiendo)
    public synchronized void cambiarEstadoFilosofo(int id, boolean comiendo) {
        estadoFilosofos[id] = comiendo ? Color.GREEN : Color.GRAY;
        repaint();
    }

    // Cambiar el estado de un tenedor (ocupado o libre)
    public synchronized void cambiarEstadoTenedor(int id, boolean enUso) {
        estadoTenedores[id] = enUso;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        int radio = 150;

        // Dibujar a los filósofos en círculo
        for (int i = 0; i < 5; i++) {
            int x = (int) (cx + radio * Math.cos(2 * Math.PI * i / 5)) - RADIO_FILOSOFO / 2;
            int y = (int) (cy + radio * Math.sin(2 * Math.PI * i / 5)) - RADIO_FILOSOFO / 2;
            g2d.setColor(estadoFilosofos[i]);
            g2d.fillOval(x, y, RADIO_FILOSOFO, RADIO_FILOSOFO);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(x, y, RADIO_FILOSOFO, RADIO_FILOSOFO);
        }

        // Dibujar los tenedores como líneas entre los filósofos
        for (int i = 0; i < 5; i++) {
            int x1 = (int) (cx + radio * Math.cos(2 * Math.PI * i / 5));
            int y1 = (int) (cy + radio * Math.sin(2 * Math.PI * i / 5));
            int x2 = (int) (cx + radio * Math.cos(2 * Math.PI * (i + 1) / 5));
            int y2 = (int) (cy + radio * Math.sin(2 * Math.PI * (i + 1) / 5));
            g2d.setColor(estadoTenedores[i] ? Color.RED : Color.BLACK);
            g2d.setStroke(new BasicStroke(5));
            g2d.drawLine(x1, y1, x2, y2);
        }
    }
}

class Tenedor {
    private final int id;
    private final Semaphore semaforo = new Semaphore(1); // Semáforo para controlar el acceso al tenedor
    private final MesaPanel mesaPanel;

    public Tenedor(int id) {
        this.id = id;
        this.mesaPanel = null;
    }

    // Tomar el tenedor
    public void tomar() throws InterruptedException {
        semaforo.acquire();
    }

    // Soltar el tenedor
    public void soltar() {
        semaforo.release();
    }
}

class Filosofo implements Runnable {
    private final int id;
    private final Tenedor tenedorIzquierdo;
    private final Tenedor tenedorDerecho;
    private final MesaPanel mesaPanel;

    public Filosofo(int id, Tenedor tenedorIzquierdo, Tenedor tenedorDerecho, MesaPanel mesaPanel) {
        this.id = id;
        this.tenedorIzquierdo = tenedorIzquierdo;
        this.tenedorDerecho = tenedorDerecho;
        this.mesaPanel = mesaPanel;
    }

    @Override
    public void run() {
        try {
            while (true) {
                pensar();
                tomarTenedores();
                comer();
                soltarTenedores();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
    }

    private void pensar() throws InterruptedException {
        System.out.println("Filósofo " + id + " está pensando.");
        mesaPanel.cambiarEstadoFilosofo(id, false);
        Thread.sleep((long) (Math.random() * 1000));
    }

    private void tomarTenedores() throws InterruptedException {
        if (id % 2 == 0) {
            tenedorIzquierdo.tomar();
            mesaPanel.cambiarEstadoTenedor(id, true);
            tenedorDerecho.tomar();
        } else {
            tenedorDerecho.tomar();
            mesaPanel.cambiarEstadoTenedor((id + 1) % 5, true);
            tenedorIzquierdo.tomar();
        }
        System.out.println("Filósofo " + id + " tomó ambos tenedores.");
    }

    private void comer() throws InterruptedException {
        System.out.println("Filósofo " + id + " está comiendo.");
        mesaPanel.cambiarEstadoFilosofo(id, true);
        Thread.sleep((long) (Math.random() * 1000));
    }

    private void soltarTenedores() {
        tenedorIzquierdo.soltar();
        mesaPanel.cambiarEstadoTenedor(id, false);
        tenedorDerecho.soltar();
        mesaPanel.cambiarEstadoTenedor((id + 1) % 5, false);
        System.out.println("Filósofo " + id + " dejó ambos tenedores.");
    }
}
