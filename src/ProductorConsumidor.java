/* ----------------------------------------------------------------------------
    Problema del productor-consumidor
-------------------------------------------------------------------------------
- Ambiente grafico (GUI)
- Controles y acciones
- Monitoreo de los estados de los procesos
- Monitoreo de los recursos compartidos
------------------------------------------------------------------------------- */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ProductorConsumidor extends JFrame implements ActionListener {
    private Productor productor = new Productor();
    private Consumidor consumidor = new Consumidor();
    public static JTextArea txtArea = new JTextArea(10, 20);
    private JButton btnProductor = new JButton("Productor");
    private JButton btnConsumidor = new JButton("Consumidor");

    private boolean estadoProductor = false;
    private boolean estadoConsumidor = false;

    public static int TAM_BUFFER = 7;
    public static int buffer[] = new int[TAM_BUFFER];
    public static int in = 0, out = 0, count = 0;

    private BufferPanel bufferPanel = new BufferPanel(); // Panel que representará el buffer

    public ProductorConsumidor() {
        super("Productor-Consumidor con Gráficos");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.add(btnProductor);
        topPanel.add(btnConsumidor);

        JScrollPane scroll = new JScrollPane(txtArea);

        add(topPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bufferPanel, BorderLayout.SOUTH);

        btnProductor.setBackground(estadoProductor ? Color.GREEN : Color.RED);
        btnProductor.setForeground(Color.WHITE);
        btnProductor.setOpaque(true);
        btnProductor.addActionListener(this);
        btnConsumidor.setBackground(estadoConsumidor ? Color.GREEN : Color.RED);
        btnConsumidor.setForeground(Color.WHITE);
        btnConsumidor.setOpaque(true);
        btnConsumidor.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnProductor) {
            if (estadoProductor) {
                productor.interrupt();
                estadoProductor = false;
                btnProductor.setBackground(Color.RED);
                btnProductor.setText("Productor");
            } else {
                productor = new Productor(); // Crear nuevo hilo si se reinicia
                productor.start();
                estadoProductor = true;
                btnProductor.setBackground(Color.GREEN);
                btnProductor.setText("Detener");
            }
        } else if (e.getSource() == btnConsumidor) {
            if (consumidor.isAlive()) {
                consumidor.interrupt();
                btnConsumidor.setBackground(Color.RED);
                btnConsumidor.setText("Consumidor");
            } else {
                consumidor = new Consumidor(); // Crear nuevo hilo si se reinicia
                consumidor.start();
                btnConsumidor.setBackground(Color.GREEN);
                btnConsumidor.setText("Detener");
            }
        }
    }

    public static void main(String args[]) {
        ProductorConsumidor pc = new ProductorConsumidor();
        pc.setVisible(true);
    }

    // Clase que representa el buffer como una serie de rectángulos
    class BufferPanel extends JPanel {
        public BufferPanel() {
            setPreferredSize(new Dimension(400, 100));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int width = 50;
            int height = 50;
            int spacing = 10;

            // Representar los elementos del buffer
            for (int i = 0; i < TAM_BUFFER; i++) {
                if (i < count) {
                    g.setColor(Color.GREEN); // Buffer lleno
                } else {
                    g.setColor(Color.GRAY); // Buffer vacío
                }
                g.fillRect(i * (width + spacing), 20, width, height);
                g.setColor(Color.BLACK);
                g.drawRect(i * (width + spacing), 20, width, height);
            }
        }

        public void actualizarBuffer() {
            repaint();
        }
    }

    // Clase que representa el productor
    class Productor extends Thread {
        public void run() {
            int item;
            while (!isInterrupted()) {
                synchronized (buffer) {
                    while (count == TAM_BUFFER) {
                        ProductorConsumidor.txtArea.append("Esperando a que el consumidor consuma...\n");
                        try {
                            buffer.wait(); // El productor espera si el buffer está lleno
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    item = (int) (Math.random() * 100);
                    buffer[in] = item;
                    in = (in + 1) % TAM_BUFFER;
                    count++;
                    ProductorConsumidor.txtArea.append("Productor produce: " + item + "\n");
                    buffer.notifyAll(); // Notificar al consumidor
                    bufferPanel.actualizarBuffer(); // Actualizar la visualización
                }
                try {
                    Thread.sleep((int) (Math.random() * 1000));
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    // Clase que representa el consumidor
    class Consumidor extends Thread {
        public void run() {
            int item;
            while (!isInterrupted()) {
                synchronized (buffer) {
                    while (count == 0) {
                        ProductorConsumidor.txtArea.append("Esperando a que el productor produzca...\n");
                        try {
                            buffer.wait(); // El consumidor espera si el buffer está vacío
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    item = buffer[out];
                    out = (out + 1) % TAM_BUFFER;
                    count--;
                    ProductorConsumidor.txtArea.append("Consumidor consume: " + item + "\n");
                    buffer.notifyAll(); // Notificar al productor
                    bufferPanel.actualizarBuffer(); // Actualizar la visualización
                }
                try {
                    Thread.sleep((int) (Math.random() * 1000));
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }
}
