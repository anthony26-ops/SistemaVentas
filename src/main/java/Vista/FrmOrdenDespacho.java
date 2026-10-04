package Vista;

import controlador.OrdenDespachoController;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import modelo.DetalleOrdenDespacho;
import modelo.DetalleOrdenVenta;
import modelo.OrdenDespacho;
import modelo.OrdenVenta;

/**
 * Pantalla del modulo de Orden de Despacho. El detalle de productos a
 * despachar se toma automáticamente de la Orden de Venta seleccionada
 * (no se vuelve a teclear a mano), cumpliendo el flujo obligatorio
 * Cliente -&gt; Orden de Venta -&gt; Despacho -&gt; Salida de Inventario -&gt; Facturación.
 */
public class FrmOrdenDespacho extends JInternalFrame {

    private final OrdenDespachoController controller = new OrdenDespachoController();
    private List<DetalleOrdenDespacho> detallesOrdenActual = new ArrayList<>();

    private JComboBox<String> cbxOrdenVenta;
    private JComboBox<modelo.Bodega> cbxBodega;
    private JLabel lblClienteOrden;
    private JTextField txtDireccion;
    private JTextField txtTransportista;
    private JTable tblDetalles;
    private DefaultTableModel modeloDetalles;
    private JTable tblOrdenes;
    private DefaultTableModel modeloOrdenes;

    public FrmOrdenDespacho() {
        initComponents();
        cargarOrdenesPendientes();
        cargarOrdenes();
        cargarBodegas();
    }

    /** Carga las bodegas reales para que el usuario elija de dónde sale el despacho. */
    private void cargarBodegas() {
        cbxBodega.removeAllItems();
        for (modelo.Bodega b : controller.listarBodegas()) {
            cbxBodega.addItem(b);
        }
    }

    private void initComponents() {
        setTitle("Orden de Despacho");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setSize(900, 640);
        setLayout(new BorderLayout(10, 10));

        // ---------- Encabezado de la orden ----------
        JPanel panelEncabezado = new JPanel(new GridBagLayout());
        panelEncabezado.setBorder(BorderFactory.createTitledBorder("Datos de la orden de despacho"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panelEncabezado.add(new JLabel("Orden de venta pendiente:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        cbxOrdenVenta = new JComboBox<>();
        cbxOrdenVenta.addActionListener(e -> cargarDetalleDeOrdenSeleccionada());
        panelEncabezado.add(cbxOrdenVenta, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        panelEncabezado.add(new JLabel("Cliente:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        lblClienteOrden = new JLabel("-");
        panelEncabezado.add(lblClienteOrden, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panelEncabezado.add(new JLabel("Transportista:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtTransportista = new JTextField(15);
        panelEncabezado.add(txtTransportista, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        panelEncabezado.add(new JLabel("Direccion de entrega:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        txtDireccion = new JTextField(25);
        panelEncabezado.add(txtDireccion, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        panelEncabezado.add(new JLabel("Bodega de origen:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        cbxBodega = new JComboBox<>();
        panelEncabezado.add(cbxBodega, gbc);

        add(panelEncabezado, BorderLayout.NORTH);

        // ---------- Panel central: productos de la orden + ordenes ----------
        JPanel panelCentro = new JPanel(new GridLayout(2, 1, 10, 10));

        JPanel panelProductos = new JPanel(new BorderLayout(6, 6));
        panelProductos.setBorder(BorderFactory.createTitledBorder(
                "Productos a despachar (tomados automáticamente de la orden seleccionada)"));

        modeloDetalles = new DefaultTableModel(
                new String[]{"Codigo", "Producto", "Cantidad"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;   // solo lectura: refleja el detalle real de la orden
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
        JButton btnGuardar = new JButton("Confirmar despacho");
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

    /** Carga en el combo las órdenes de venta que todavía no se han despachado. */
    private void cargarOrdenesPendientes() {
        cbxOrdenVenta.removeAllItems();
        for (OrdenVenta orden : controller.listarOrdenesPendientes()) {
            cbxOrdenVenta.addItem(orden.getIdOrdenVenta() + " - Cliente #" + orden.getCliente().getId()
                    + " - Total Q" + String.format("%.2f", orden.getTotal()));
        }
        if (cbxOrdenVenta.getItemCount() == 0) {
            cbxOrdenVenta.addItem("(No hay órdenes de venta pendientes de despachar)");
        }
    }

    /** Al elegir una orden en el combo, trae su detalle real de productos. */
    private void cargarDetalleDeOrdenSeleccionada() {
        modeloDetalles.setRowCount(0);
        detallesOrdenActual = new ArrayList<>();
        lblClienteOrden.setText("-");

        Integer idOrdenVenta = idOrdenSeleccionada();
        if (idOrdenVenta == null) {
            return;
        }

        OrdenVenta orden = controller.obtenerOrdenVenta(idOrdenVenta);
        if (orden == null || orden.getDetalles() == null) {
            return;
        }

        lblClienteOrden.setText("Cliente #" + orden.getCliente().getId());

        for (DetalleOrdenVenta d : orden.getDetalles()) {
            modeloDetalles.addRow(new Object[]{
                d.getProducto().getIdProducto(), d.getProducto().getNombre(), d.getCantidad()
            });

            DetalleOrdenDespacho detalleDespacho = new DetalleOrdenDespacho();
            detalleDespacho.setIdProducto(d.getProducto().getIdProducto());
            detalleDespacho.setNombreProducto(d.getProducto().getNombre());
            detalleDespacho.setCantidad(d.getCantidad());
            detallesOrdenActual.add(detalleDespacho);
        }
    }

    /** Extrae el número de orden del texto seleccionado en el combo ("id - ..."). */
    private Integer idOrdenSeleccionada() {
        Object seleccion = cbxOrdenVenta.getSelectedItem();
        if (seleccion == null) {
            return null;
        }
        String texto = seleccion.toString();
        int guion = texto.indexOf(" - ");
        if (guion <= 0) {
            return null;
        }
        try {
            return Integer.parseInt(texto.substring(0, guion).trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void guardarOrden() {
        Integer idOrdenVenta = idOrdenSeleccionada();
        if (idOrdenVenta == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una orden de venta pendiente para despachar.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        modelo.Bodega bodega = (modelo.Bodega) cbxBodega.getSelectedItem();
        if (bodega == null) {
            JOptionPane.showMessageDialog(this,
                    "No hay ninguna bodega registrada. Cree una en el módulo de Bodega/Inventario "
                    + "antes de despachar.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            OrdenDespacho orden = controller.crearOrdenDespacho(
                    idOrdenVenta,
                    bodega.getIdBodega(),
                    txtDireccion.getText(),
                    txtTransportista.getText(),
                    new ArrayList<>(detallesOrdenActual));

            JOptionPane.showMessageDialog(this,
                    "Orden de despacho No. " + orden.getIdOrdenDespacho() + " guardada correctamente.\n"
                    + "Total de unidades: " + orden.calcularTotalUnidades() + "\n"
                    + "Se descontó el inventario de cada producto despachado.",
                    "Sistema de Ventas", JOptionPane.INFORMATION_MESSAGE);

            limpiarFormulario();
            cargarOrdenesPendientes();
            cargarOrdenes();

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
        txtDireccion.setText("");
        txtTransportista.setText("");
        modeloDetalles.setRowCount(0);
        detallesOrdenActual = new ArrayList<>();
        lblClienteOrden.setText("-");
    }

}
