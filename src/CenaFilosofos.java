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
    public static Tenedor[] tenedores = new Tenedor[NUM_FILOSOFOS]; // Arreglo de tenedores
    private static MesaPanel mesaPanel = new MesaPanel();
    private static JLabel estadoLabel = new JLabel("Estado: Iniciando...");
    
    public static void main(String[] args) {
        // Configuración de la GUI
        CenaFilosofos frame = new CenaFilosofos();
        frame.setTitle("Cena de los Filósofos");
        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Crear tenedores
        for (int i = 0; i < NUM_FILOSOFOS; i++) {
            tenedores[i] = new Tenedor(i, mesaPanel);
        }

        // Crear y comenzar los hilos de los filósofos
        for (int i = 0; i < NUM_FILOSOFOS; i++) {
            filosofos[i] = new Filosofo(i, tenedores[i], tenedores[(i + 1) % NUM_FILOSOFOS], mesaPanel, estadoLabel);
            Thread t = new Thread(filosofos[i], "Filosofo " + (i + 1));
            t.start();
        }
    }

    // Constructor para la GUI
    public CenaFilosofos() {
        setLayout(new BorderLayout());
        add(mesaPanel, BorderLayout.CENTER);
        add(estadoLabel, BorderLayout.SOUTH); // Agregar una barra de estado al fondo
    }
}

// Panel para dibujar la mesa, los filósofos y los tenedores
class MesaPanel extends JPanel {
    private static final int RADIO_FILOSOFO = 40; // Tamaño de los círculos que representan a los filósofos
    private Color[] estadoFilosofos = new Color[5]; // Colores de los filósofos
    private String[] textoEstadoFilosofos = new String[5]; // Texto para indicar el estado de los filósofos
    private int[] contadorComidas = new int[5]; // Contadores de comidas de los filósofos

    public MesaPanel() {
        // Inicializar los filósofos en estado de "pensar" (color gris)
        for (int i = 0; i < 5; i++) {
            estadoFilosofos[i] = Color.GRAY;
            textoEstadoFilosofos[i] = "Pensando";
            contadorComidas[i] = 0;
        }
    }

    // Cambiar el estado de un filósofo (pensando o comiendo)
    public synchronized void cambiarEstadoFilosofo(int id, boolean comiendo) {
        estadoFilosofos[id] = comiendo ? Color.GREEN : Color.GRAY;
        textoEstadoFilosofos[id] = comiendo ? "Comiendo" : "Pensando";
        repaint();
    }

    // Aumentar el contador de comidas de un filósofo
    public synchronized void incrementarComidas(int id) {
        contadorComidas[id]++;
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

            // Dibujar el estado textual del filósofo
            g2d.setColor(Color.BLACK);
            g2d.drawString(textoEstadoFilosofos[i], x, y - 10); // Estado (Pensando o Comiendo)
            g2d.drawString("Comidas: " + contadorComidas[i], x, y + RADIO_FILOSOFO + 15); // Contador de comidas
        }

        // Dibujar los tenedores como líneas entre los filósofos
        for (int i = 0; i < 5; i++) {
            int x1 = (int) (cx + radio * Math.cos(2 * Math.PI * i / 5));
            int y1 = (int) (cy + radio * Math.sin(2 * Math.PI * i / 5));
            int x2 = (int) (cx + radio * Math.cos(2 * Math.PI * (i + 1) / 5));
            int y2 = (int) (cy + radio * Math.sin(2 * Math.PI * (i + 1) / 5));
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(5));
            g2d.drawLine(x1, y1, x2, y2);
        }
    }
}

class Tenedor {
    private final int id;
    private final Semaphore semaforo = new Semaphore(1); // Semáforo para controlar el acceso al tenedor
    private final MesaPanel mesaPanel;

    private boolean enUso = false;

    public Tenedor(int id, MesaPanel mesaPanel) {
        this.id = id;
        this.mesaPanel = mesaPanel;
    }

    // Tomar el tenedor
    public void tomar() throws InterruptedException {
        semaforo.acquire();
    }

    // Soltar el tenedor
    public void soltar() {
        semaforo.release();
    }

    public int getId() {
        return id;
    }

    public boolean estaEnUso() {
        return enUso;
    }
}

class Filosofo implements Runnable {
    private final int id;
    private final Tenedor tenedorIzquierdo;
    private final Tenedor tenedorDerecho;
    private final MesaPanel mesaPanel;
    private final JLabel estadoLabel;

    public Filosofo(int id, Tenedor tenedorIzquierdo, Tenedor tenedorDerecho, MesaPanel mesaPanel, JLabel estadoLabel) {
        this.id = id;
        this.tenedorIzquierdo = tenedorIzquierdo;
        this.tenedorDerecho = tenedorDerecho;
        this.mesaPanel = mesaPanel;
        this.estadoLabel = estadoLabel;
    }

    @Override
    public void run() {
        try {
            while (true) {
                pensar();
                tomarTenedores(); // Intentar tomar los tenedores
                comer();
                soltarTenedores();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
    }

    // Filósofo está pensando
    private void pensar() throws InterruptedException {
        mesaPanel.cambiarEstadoFilosofo(id, false);
        estadoLabel.setText("Filósofo " + id + " está pensando.");
        Thread.sleep((long) (Math.random() * 3000));
    }

    // Intentar tomar los tenedores
    private void tomarTenedores() throws InterruptedException {
        if (id % 2 == 0) {
            tenedorIzquierdo.tomar();
            tenedorDerecho.tomar();
        } else {
            tenedorDerecho.tomar();
            tenedorIzquierdo.tomar();
        }
        // Actualizar el estado del filósofo
        estadoLabel.setText("Filósofo " + id + " ha tomado ambos tenedores.");
    }

    private void comer() throws InterruptedException {
        mesaPanel.cambiarEstadoFilosofo(id, true);
        mesaPanel.incrementarComidas(id);
        estadoLabel.setText("Filósofo " + id + " está comiendo.");
        Thread.sleep((long) (Math.random() * 3000));
    }

    // Liberar los tenedores
    private void soltarTenedores() {
        tenedorIzquierdo.soltar();
        tenedorDerecho.soltar();
        estadoLabel.setText("Filósofo " + id + " ha soltado ambos tenedores.");
    }
}
