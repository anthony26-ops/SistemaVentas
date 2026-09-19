package Vista;

import controlador.OrdenDespachoController;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import modelo.DetalleOrdenDespacho;
import modelo.OrdenDespacho;

/**
 * Pantalla del modulo de Orden de Despacho. Permite armar la orden con sus
 * productos antes de guardarla y consultar las ordenes ya generadas.
 */
public class FrmOrdenDespacho extends JFrame {

    private final OrdenDespachoController controller = new OrdenDespachoController();
    private final List<DetalleOrdenDespacho> detallesTemporales = new ArrayList<>();

    private JTextField txtIdOrdenVenta;
    private JTextField txtDireccion;
    private JTextField txtTransportista;
    private JTextField txtIdProducto;
    private JTextField txtNombreProducto;
    private JTextField txtCantidad;
    private JTable tblDetalles;
    private DefaultTableModel modeloDetalles;
    private JTable tblOrdenes;
    private DefaultTableModel modeloOrdenes;

    public FrmOrdenDespacho() {
        initComponents();
        cargarOrdenes();
    }

    private void initComponents() {
        setTitle("Sistema de Ventas - Orden de Despacho");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 620);
        setLayout(new BorderLayout(10, 10));

        // ---------- Encabezado de la orden ----------
        JPanel panelEncabezado = new JPanel(new GridBagLayout());
        panelEncabezado.setBorder(BorderFactory.createTitledBorder("Datos de la orden de despacho"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panelEncabezado.add(new JLabel("No. orden de venta:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtIdOrdenVenta = new JTextField(10);
        panelEncabezado.add(txtIdOrdenVenta, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        panelEncabezado.add(new JLabel("Transportista:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        txtTransportista = new JTextField(15);
        panelEncabezado.add(txtTransportista, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panelEncabezado.add(new JLabel("Direccion de entrega:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1;
        txtDireccion = new JTextField(40);
        panelEncabezado.add(txtDireccion, gbc);
        gbc.gridwidth = 1;

        add(panelEncabezado, BorderLayout.NORTH);

        // ---------- Panel central: productos + ordenes ----------
        JPanel panelCentro = new JPanel(new GridLayout(2, 1, 10, 10));

        // Productos de la orden
        JPanel panelProductos = new JPanel(new BorderLayout(6, 6));
        panelProductos.setBorder(BorderFactory.createTitledBorder("Productos a despachar"));

        JPanel panelAgregar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panelAgregar.add(new JLabel("Codigo producto:"));
        txtIdProducto = new JTextField(6);
        panelAgregar.add(txtIdProducto);
        panelAgregar.add(new JLabel("Nombre:"));
        txtNombreProducto = new JTextField(16);
        panelAgregar.add(txtNombreProducto);
        panelAgregar.add(new JLabel("Cantidad:"));
        txtCantidad = new JTextField(5);
        panelAgregar.add(txtCantidad);

        JButton btnAgregar = new JButton("Agregar producto");
        JButton btnQuitar = new JButton("Quitar seleccionado");
        btnAgregar.addActionListener(e -> agregarDetalle());
        btnQuitar.addActionListener(e -> quitarDetalle());
        panelAgregar.add(btnAgregar);
        panelAgregar.add(btnQuitar);

        panelProductos.add(panelAgregar, BorderLayout.NORTH);

        modeloDetalles = new DefaultTableModel(
                new String[]{"Codigo", "Producto", "Cantidad"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tblDetalles = new JTable(modeloDetalles);
        panelProductos.add(new JScrollPane(tblDetalles), BorderLayout.CENTER);
        panelCentro.add(panelProductos);

        // Ordenes generadas
        modeloOrdenes = new DefaultTableModel(
                new String[]{"Codigo", "Orden venta", "Fecha", "Direccion", "Transportista", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tblOrdenes = new JTable(modeloOrdenes);
        tblOrdenes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollOrdenes = new JScrollPane(tblOrdenes);
        scrollOrdenes.setBorder(BorderFactory.createTitledBorder("Ordenes de despacho generadas"));
        panelCentro.add(scrollOrdenes);

        add(panelCentro, BorderLayout.CENTER);

        // ---------- Botones ----------
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnGuardar = new JButton("Guardar orden");
        JButton btnCambiarEstado = new JButton("Cambiar estado");
        JButton btnVerDetalle = new JButton("Ver detalle");
        JButton btnLimpiar = new JButton("Limpiar");
        JButton btnSalir = new JButton("Salir");

        btnGuardar.addActionListener(e -> guardarOrden());
        btnCambiarEstado.addActionListener(e -> cambiarEstado());
        btnVerDetalle.addActionListener(e -> verDetalle());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnSalir.addActionListener(e -> dispose());

        panelBotones.add(btnGuardar);
        panelBotones.add(btnCambiarEstado);
        panelBotones.add(btnVerDetalle);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnSalir);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private void agregarDetalle() {
        try {
            int idProducto = Integer.parseInt(txtIdProducto.getText().trim());
            String nombre = txtNombreProducto.getText().trim();
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());

            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingrese el nombre del producto.",
                        "Datos incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (cantidad <= 0) {
                JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a cero.",
                        "Datos invalidos", JOptionPane.WARNING_MESSAGE);
                return;
            }

            DetalleOrdenDespacho detalle = new DetalleOrdenDespacho();
            detalle.setIdProducto(idProducto);
            detalle.setNombreProducto(nombre);
            detalle.setCantidad(cantidad);
            detallesTemporales.add(detalle);

            modeloDetalles.addRow(new Object[]{idProducto, nombre, cantidad});

            txtIdProducto.setText("");
            txtNombreProducto.setText("");
            txtCantidad.setText("");
            txtIdProducto.requestFocus();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "El codigo de producto y la cantidad deben ser numeros.",
                    "Datos invalidos", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void quitarDetalle() {
        int fila = tblDetalles.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione el producto que desea quitar.");
            return;
        }
        detallesTemporales.remove(fila);
        modeloDetalles.removeRow(fila);
    }

    private void guardarOrden() {
        try {
            int idOrdenVenta = Integer.parseInt(txtIdOrdenVenta.getText().trim());

            OrdenDespacho orden = controller.crearOrdenDespacho(
                    idOrdenVenta,
                    txtDireccion.getText(),
                    txtTransportista.getText(),
                    new ArrayList<>(detallesTemporales));

            JOptionPane.showMessageDialog(this,
                    "Orden de despacho No. " + orden.getIdOrdenDespacho() + " guardada correctamente.\n"
                    + "Total de unidades: " + orden.calcularTotalUnidades(),
                    "Sistema de Ventas", JOptionPane.INFORMATION_MESSAGE);

            limpiarFormulario();
            cargarOrdenes();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El numero de orden de venta debe ser numerico.",
                    "Datos invalidos", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "No se pudo guardar la orden", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void cambiarEstado() {
        int fila = tblOrdenes.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una orden de la tabla.");
            return;
        }
        String nuevoEstado = (String) JOptionPane.showInputDialog(this,
                "Seleccione el nuevo estado:", "Cambiar estado",
                JOptionPane.QUESTION_MESSAGE, null,
                OrdenDespachoController.ESTADOS, OrdenDespachoController.ESTADOS[0]);

        if (nuevoEstado == null) {
            return;
        }
        try {
            int idOrden = (int) modeloOrdenes.getValueAt(fila, 0);
            if (controller.cambiarEstado(idOrden, nuevoEstado)) {
                JOptionPane.showMessageDialog(this, "Estado actualizado.");
                cargarOrdenes();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar el estado.",
                        "Sistema de Ventas", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Sistema de Ventas", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void verDetalle() {
        int fila = tblOrdenes.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una orden de la tabla.");
            return;
        }
        int idOrden = (int) modeloOrdenes.getValueAt(fila, 0);
        List<DetalleOrdenDespacho> detalles = controller.listarDetalles(idOrden);

        if (detalles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La orden no tiene productos registrados.");
            return;
        }
        StringBuilder sb = new StringBuilder("Productos de la orden No. " + idOrden + ":\n\n");
        for (DetalleOrdenDespacho d : detalles) {
            sb.append("- ").append(d.getNombreProducto())
              .append("  (codigo ").append(d.getIdProducto()).append(")")
              .append("  x").append(d.getCantidad()).append("\n");
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "Detalle de la orden",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void cargarOrdenes() {
        modeloOrdenes.setRowCount(0);
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (OrdenDespacho orden : controller.listarOrdenes()) {
            modeloOrdenes.addRow(new Object[]{
                orden.getIdOrdenDespacho(),
                orden.getIdOrdenVenta(),
                orden.getFechaDespacho() == null ? "" : formato.format(orden.getFechaDespacho()),
                orden.getDireccionEntrega(),
                orden.getTransportista(),
                orden.getEstado()
            });
        }
    }

    private void limpiarFormulario() {
        txtIdOrdenVenta.setText("");
        txtDireccion.setText("");
        txtTransportista.setText("");
        txtIdProducto.setText("");
        txtNombreProducto.setText("");
        txtCantidad.setText("");
        detallesTemporales.clear();
        modeloDetalles.setRowCount(0);
        txtIdOrdenVenta.requestFocus();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FrmOrdenDespacho().setVisible(true));
    }
}
