package Vista;

import controlador.PagoController;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import modelo.IMetodoPago;
import modelo.Pago;
import modelo.PagoEfectivo;

/**
 * Pantalla del modulo de Pagos. Solo se encarga de mostrar datos y de
 * recoger lo que el usuario escribe: toda la logica vive en PagoController.
 */
public class FrmPago extends JInternalFrame {

    private final PagoController controller = new PagoController();

    private JTextField txtIdFactura;
    private JTextField txtMonto;
    private JComboBox<String> cbxMetodo;
    private JLabel lblDatoUno;
    private JLabel lblDatoDos;
    private JTextField txtDatoUno;
    private JTextField txtDatoDos;
    private JTable tblPagos;
    private DefaultTableModel modeloTabla;

    public FrmPago() {
        initComponents();
        cargarPagos();
    }

    private void initComponents() {
        setTitle("Registro de Pagos");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setSize(820, 540);
        setLayout(new BorderLayout(10, 10));

        // ---------- Formulario ----------
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos del pago"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panelFormulario.add(new JLabel("No. de factura:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtIdFactura = new JTextField(12);
        panelFormulario.add(txtIdFactura, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        panelFormulario.add(new JLabel("Monto a pagar (Q):"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        txtMonto = new JTextField(12);
        panelFormulario.add(txtMonto, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panelFormulario.add(new JLabel("Metodo de pago:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        cbxMetodo = new JComboBox<>(new String[]{"EFECTIVO", "TARJETA", "TRANSFERENCIA"});
        cbxMetodo.addActionListener(e -> actualizarCamposMetodo());
        panelFormulario.add(cbxMetodo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        lblDatoUno = new JLabel("Monto recibido (Q):");
        panelFormulario.add(lblDatoUno, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtDatoUno = new JTextField(12);
        panelFormulario.add(txtDatoUno, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        lblDatoDos = new JLabel(" ");
        panelFormulario.add(lblDatoDos, gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        txtDatoDos = new JTextField(12);
        txtDatoDos.setEnabled(false);
        panelFormulario.add(txtDatoDos, gbc);

        add(panelFormulario, BorderLayout.NORTH);

        // ---------- Tabla ----------
        modeloTabla = new DefaultTableModel(
                new String[]{"Codigo", "Factura", "Fecha", "Monto (Q)", "Metodo", "Referencia", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tblPagos = new JTable(modeloTabla);
        tblPagos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(tblPagos);
        scroll.setBorder(BorderFactory.createTitledBorder("Pagos registrados"));
        add(scroll, BorderLayout.CENTER);

        // ---------- Botones ----------
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnRegistrar = new JButton("Registrar pago");
        JButton btnAnular = new JButton("Anular pago");
        JButton btnConsultarSaldo = new JButton("Consultar saldo");
        JButton btnLimpiar = new JButton("Limpiar");
        JButton btnDespacho = new JButton("Orden de despacho");
        JButton btnSalir = new JButton("Salir");

        btnRegistrar.addActionListener(e -> registrarPago());
        btnAnular.addActionListener(e -> anularPago());
        btnConsultarSaldo.addActionListener(e -> consultarSaldo());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnDespacho.addActionListener(e -> abrirOrdenDespacho());
        btnSalir.addActionListener(e -> dispose());

        panelBotones.add(btnRegistrar);
        panelBotones.add(btnAnular);
        panelBotones.add(btnConsultarSaldo);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnDespacho);
        panelBotones.add(btnSalir);
        add(panelBotones, BorderLayout.SOUTH);

        actualizarCamposMetodo();
    }

    /** Cambia las etiquetas segun el metodo de pago seleccionado. */
    private void actualizarCamposMetodo() {
        String metodo = (String) cbxMetodo.getSelectedItem();
        txtDatoUno.setText("");
        txtDatoDos.setText("");
        if ("EFECTIVO".equals(metodo)) {
            lblDatoUno.setText("Monto recibido (Q):");
            lblDatoDos.setText(" ");
            txtDatoDos.setEnabled(false);
        } else if ("TARJETA".equals(metodo)) {
            lblDatoUno.setText("No. de tarjeta:");
            lblDatoDos.setText("Titular:");
            txtDatoDos.setEnabled(true);
        } else {
            lblDatoUno.setText("Banco:");
            lblDatoDos.setText("No. de boleta:");
            txtDatoDos.setEnabled(true);
        }
    }

    private void registrarPago() {
        try {
            int idFactura = Integer.parseInt(txtIdFactura.getText().trim());
            double monto = Double.parseDouble(txtMonto.getText().trim());
            String metodo = (String) cbxMetodo.getSelectedItem();

            IMetodoPago metodoPago = controller.crearMetodoPago(
                    metodo, txtDatoUno.getText().trim(), txtDatoDos.getText().trim());

            Pago pago = controller.registrarPago(idFactura, monto, metodoPago);

            String mensaje = "Pago registrado correctamente.\nReferencia: " + pago.getReferencia();
            if (metodoPago instanceof PagoEfectivo) {
                double cambio = ((PagoEfectivo) metodoPago).calcularCambio(monto);
                mensaje += "\nCambio a devolver: Q" + String.format("%.2f", cambio);
            }
            JOptionPane.showMessageDialog(this, mensaje, "Sistema de Ventas",
                    JOptionPane.INFORMATION_MESSAGE);

            limpiarFormulario();
            cargarPagos();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "El numero de factura y el monto deben ser valores numericos.",
                    "Datos invalidos", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "No se pudo registrar el pago", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void anularPago() {
        int fila = tblPagos.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione en la tabla el pago que desea anular.",
                    "Sistema de Ventas", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirmar = JOptionPane.showConfirmDialog(this,
                "Esta seguro de anular el pago seleccionado?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmar != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            int idPago = (int) modeloTabla.getValueAt(fila, 0);
            if (controller.anularPago(idPago)) {
                JOptionPane.showMessageDialog(this, "El pago fue anulado.");
                cargarPagos();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo anular el pago.",
                        "Sistema de Ventas", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Sistema de Ventas", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void consultarSaldo() {
        try {
            int idFactura = Integer.parseInt(txtIdFactura.getText().trim());
            String totalTexto = JOptionPane.showInputDialog(this,
                    "Total de la factura No. " + idFactura + " (Q):");
            if (totalTexto == null || totalTexto.trim().isEmpty()) {
                return;
            }
            double total = Double.parseDouble(totalTexto.trim());
            double saldo = controller.calcularSaldoPendiente(idFactura, total);

            String estado = controller.facturaEstaSaldada(idFactura, total)
                    ? "La factura ya esta totalmente pagada."
                    : "Saldo pendiente: Q" + String.format("%.2f", saldo);

            JOptionPane.showMessageDialog(this, estado, "Estado de la factura",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese valores numericos validos.",
                    "Datos invalidos", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void cargarPagos() {
        modeloTabla.setRowCount(0);
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        List<Pago> pagos = controller.listarPagos();
        for (Pago pago : pagos) {
            modeloTabla.addRow(new Object[]{
                pago.getIdPago(),
                pago.getIdFactura(),
                pago.getFecha() == null ? "" : formato.format(pago.getFecha()),
                String.format("%.2f", pago.getMonto()),
                pago.getTipoPago(),
                pago.getReferencia(),
                pago.getEstado()
            });
        }
    }

    /** Abre la pantalla de Orden de Despacho del mismo modulo, dentro del mismo escritorio. */
    private void abrirOrdenDespacho() {
        FrmOrdenDespacho frm = new FrmOrdenDespacho();
        JDesktopPane desktop = getDesktopPane();
        if (desktop != null) {
            desktop.add(frm);
            frm.setVisible(true);
            try {
                frm.setSelected(true);
            } catch (java.beans.PropertyVetoException ignored) {
            }
        } else {
            frm.setVisible(true);
        }
    }

    private void limpiarFormulario() {
        txtIdFactura.setText("");
        txtMonto.setText("");
        txtDatoUno.setText("");
        txtDatoDos.setText("");
        cbxMetodo.setSelectedIndex(0);
        actualizarCamposMetodo();
        txtIdFactura.requestFocus();
    }
}
