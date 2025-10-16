package ejercicios.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class EjerciciosSwing extends JFrame {

    public EjerciciosSwing() {
        super("Ejercicios Swing - NetBeans/Java");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 420);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("1) Calculadora", new CalculadoraPanel());
        tabs.addTab("2) Tamaño de Fuente", new FuentePanel());
        tabs.addTab("3) Lenguajes", new LenguajesPanel());
        tabs.addTab("4) Validación Edad", new EdadPanel());

        setContentPane(tabs);
    }

    // =========================
    //  EJERCICIO 1: Calculadora
    // =========================
    static class CalculadoraPanel extends JPanel {
        private final JTextField txt;
        private final JPanel centroNumeros;
        private final JPanel inferiorOps;

        private StringBuilder entrada = new StringBuilder(); // buffer de dígitos
        private double acumulado = 0.0;                      // resultado parcial
        private Character operacion = null;                  // '+', '-', '*', '/'
        private boolean mostrarResultado = false;            // para reiniciar entrada luego de operar

        CalculadoraPanel() {
            setLayout(new BorderLayout(8, 8));
            setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

            // Superior: JTextField para mostrar resultado
            txt = new JTextField();
            txt.setEditable(false);
            txt.setHorizontalAlignment(JTextField.RIGHT);
            txt.setFont(new Font("Consolas", Font.PLAIN, 24));
            add(txt, BorderLayout.NORTH);

            // Centro: 10 botones numéricos (0-9)
            centroNumeros = new JPanel(new GridLayout(4, 3, 6, 6));
            for (int i = 1; i <= 9; i++) {
                int n = i;
                JButton b = new JButton(String.valueOf(n));
                b.setFont(b.getFont().deriveFont(18f));
                b.addActionListener(e -> presionarNumero(n));
                centroNumeros.add(b);
            }
            // botón 0 + “.” (opcional) + “CE” (limpia entrada)
            JButton b0 = new JButton("0");
            b0.setFont(b0.getFont().deriveFont(18f));
            b0.addActionListener(e -> presionarNumero(0));
            JButton punto = new JButton(".");
            punto.setFont(punto.getFont().deriveFont(18f));
            punto.addActionListener(e -> presionarPunto());
            JButton ce = new JButton("CE");
            ce.setFont(ce.getFont().deriveFont(16f));
            ce.setToolTipText("Borra solo la entrada actual");
            ce.addActionListener(e -> { entrada.setLength(0); actualizarField("0"); });
            centroNumeros.add(b0);
            centroNumeros.add(punto);
            centroNumeros.add(ce);
            add(centroNumeros, BorderLayout.CENTER);

            // Inferior: botones de operaciones (+, -, *, /)
            inferiorOps = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
            String[] ops = { "+", "-", "*", "/" };
            for (String op : ops) {
                JButton b = new JButton(op);
                b.setFont(b.getFont().deriveFont(18f));
                b.addActionListener(e -> presionarOperacion(op.charAt(0)));
                inferiorOps.add(b);
            }
            // (Opcional, pero útil) “=” para ver el resultado inmediato
            JButton igual = new JButton("=");
            igual.setFont(igual.getFont().deriveFont(18f));
            igual.addActionListener(e -> ejecutarIgual());
            inferiorOps.add(igual);

            // (Opcional) “C” para reiniciar todo
            JButton c = new JButton("C");
            c.setFont(c.getFont().deriveFont(18f));
            c.setToolTipText("Borra todo (acumulado y entrada)");
            c.addActionListener(e -> {
                entrada.setLength(0);
                acumulado = 0.0;
                operacion = null;
                mostrarResultado = false;
                actualizarField("0");
            });
            inferiorOps.add(c);

            add(inferiorOps, BorderLayout.SOUTH);

            // estado inicial
            actualizarField("0");
        }

        private void presionarNumero(int n) {
            if (mostrarResultado) { // si recién mostramos un resultado, empezar nueva entrada
                entrada.setLength(0);
                mostrarResultado = false;
            }
            // evitar 000...
            if (entrada.length() == 1 && entrada.charAt(0) == '0') {
                entrada.setCharAt(0, Character.forDigit(n, 10));
            } else {
                entrada.append(n);
            }
            actualizarField(entrada.toString());
        }

        private void presionarPunto() {
            if (mostrarResultado) {
                entrada.setLength(0);
                mostrarResultado = false;
            }
            if (entrada.indexOf(".") == -1) {
                if (entrada.length() == 0) entrada.append("0");
                entrada.append(".");
                actualizarField(entrada.toString());
            }
        }

        private void presionarOperacion(char op) {
            try {
                double valor = entrada.length() == 0 ? acumulado : Double.parseDouble(entrada.toString());
                if (operacion == null) {
                    // primera operación: guardo el valor y la operación
                    acumulado = valor;
                } else {
                    // ya había una operación pendiente → la resuelvo
                    acumulado = resolver(acumulado, valor, operacion);
                }
                operacion = op;
                entrada.setLength(0);
                mostrarResultado = true;
                actualizarField(trimDouble(acumulado));
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Entrada inválida.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (ArithmeticException ae) {
                JOptionPane.showMessageDialog(this, ae.getMessage(), "Error aritmético", JOptionPane.ERROR_MESSAGE);
                // reset básico ante error
                entrada.setLength(0);
                acumulado = 0;
                operacion = null;
                mostrarResultado = false;
                actualizarField("0");
            }
        }

        private void ejecutarIgual() {
            try {
                if (operacion == null) return;
                double valor = entrada.length() == 0 ? acumulado : Double.parseDouble(entrada.toString());
                acumulado = resolver(acumulado, valor, operacion);
                operacion = null;
                entrada.setLength(0);
                mostrarResultado = true;
                actualizarField(trimDouble(acumulado));
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Entrada inválida.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (ArithmeticException ae) {
                JOptionPane.showMessageDialog(this, ae.getMessage(), "Error aritmético", JOptionPane.ERROR_MESSAGE);
                entrada.setLength(0);
                acumulado = 0;
                operacion = null;
                mostrarResultado = false;
                actualizarField("0");
            }
        }

        private static double resolver(double a, double b, char op) {
            switch (op) {
                case '+': return a + b;
                case '-': return a - b;
                case '*': return a * b;
                case '/':
                    if (b == 0.0) throw new ArithmeticException("No se puede dividir por cero.");
                    return a / b;
                default: throw new IllegalArgumentException("Operación desconocida: " + op);
            }
        }

        private void actualizarField(String s) {
            txt.setText(s);
        }

        private String trimDouble(double d) {
            String s = Double.toString(d);
            if (s.endsWith(".0")) s = s.substring(0, s.length() - 2);
            return s;
        }
    }

    // ==================================
    //  EJERCICIO 2: Tamaño de Fuente
    // ==================================
    static class FuentePanel extends JPanel {
        private final JLabel lbl;
        private final JComboBox<Integer> combo;

        FuentePanel() {
            setLayout(new BorderLayout(8,8));
            setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

            lbl = new JLabel("Texto de ejemplo", SwingConstants.CENTER);
            lbl.setFont(lbl.getFont().deriveFont(16f));

            Integer[] tamanios = {12, 14, 16, 18, 20};
            combo = new JComboBox<>(tamanios);
            combo.setSelectedItem(16);
            combo.addActionListener(e -> {
                Integer size = (Integer) combo.getSelectedItem();
                if (size != null) {
                    lbl.setFont(lbl.getFont().deriveFont(size.floatValue()));
                }
            });

            add(lbl, BorderLayout.CENTER);

            JPanel sur = new JPanel(new FlowLayout(FlowLayout.CENTER));
            sur.add(new JLabel("Tamaño de fuente:"));
            sur.add(combo);
            add(sur, BorderLayout.SOUTH);
        }
    }

    // ==================================
    //  EJERCICIO 3: Selector de Lenguajes
    // ==================================
    static class LenguajesPanel extends JPanel {
        private final JList<String> lista;
        private final Map<String, String> info = new HashMap<>();

        LenguajesPanel() {
            setLayout(new BorderLayout(8,8));
            setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

            String[] langs = {"Java", "Python", "C++", "JavaScript", "C#", "Go", "Ruby", "Kotlin"};
            lista = new JList<>(langs);
            lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            lista.setVisibleRowCount(8);

            // Info básica de ejemplo
            info.put("Java", "Java: lenguaje orientado a objetos, JVM, muy usado en backend y Android.");
            info.put("Python", "Python: sintaxis simple, ciencia de datos, IA, scripting y web.");
            info.put("C++", "C++: alto rendimiento, sistemas, videojuegos, uso de memoria manual.");
            info.put("JavaScript", "JavaScript: web frontend (y backend con Node.js), asincronía.");
            info.put("C#", "C#: ecosistema .NET, desktop y juegos (Unity).");
            info.put("Go", "Go: concurrente, compilado, excelente para servicios y redes.");
            info.put("Ruby", "Ruby: muy usado con Rails para desarrollo web rápido.");
            info.put("Kotlin", "Kotlin: moderno y conciso, oficial para Android, interoperable con Java.");

            // Doble clic → mostrar info
            lista.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        String sel = lista.getSelectedValue();
                        if (sel != null) {
                            String msg = info.getOrDefault(sel, "Sin información disponible.");
                            JOptionPane.showMessageDialog(LenguajesPanel.this, msg, sel, JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            });

            add(new JScrollPane(lista), BorderLayout.CENTER);
            add(new JLabel("Doble clic en un lenguaje para ver info.", SwingConstants.CENTER), BorderLayout.SOUTH);
        }
    }

    // ================================
    //  EJERCICIO 4: Validación de Edad
    // ================================
    static class EdadPanel extends JPanel {
        private final JTextField txtEdad;
        private final JButton btnValidar;
        private final JLabel lblResultado;

        EdadPanel() {
            setLayout(new GridBagLayout());
            setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(8,8,8,8);
            gc.fill = GridBagConstraints.HORIZONTAL;

            JLabel lbl = new JLabel("Ingresá tu edad:");
            txtEdad = new JTextField();
            btnValidar = new JButton("Validar");
            lblResultado = new JLabel(" ", SwingConstants.CENTER);

            btnValidar.addActionListener(e -> validarEdad());

            gc.gridx = 0; gc.gridy = 0; gc.weightx = 0.2;
            add(lbl, gc);
            gc.gridx = 1; gc.gridy = 0; gc.weightx = 0.8;
            add(txtEdad, gc);
            gc.gridx = 0; gc.gridy = 1; gc.gridwidth = 2;
            add(btnValidar, gc);
            gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2;
            add(lblResultado, gc);
        }

        private void validarEdad() {
            try {
                String s = txtEdad.getText().trim();
                if (s.isEmpty()) throw new NumberFormatException("Campo vacío.");
                int edad = Integer.parseInt(s);
                if (edad < 0) throw new NumberFormatException("Edad negativa.");

                if (edad < 18) {
                    lblResultado.setText("Error: sos menor de 18.");
                    JOptionPane.showMessageDialog(this, "Sos menor de 18.", "Acceso restringido", JOptionPane.ERROR_MESSAGE);
                } else {
                    lblResultado.setText("Bienvenido/a. Acceso permitido.");
                    JOptionPane.showMessageDialog(this, "¡Bienvenido/a!", "Acceso permitido", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                lblResultado.setText("Error: ingresá un número válido.");
                JOptionPane.showMessageDialog(this, "Ingresá un número válido para la edad.", "Dato inválido", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EjerciciosSwing().setVisible(true));
    }
}
