package Vista;

/**
 * Pantalla de Facturación. Solo permite generar una factura a partir de una
 * Orden de Venta que YA fue despachada (paso 7 del flujo obligatorio
 * Cliente -&gt; Orden de Venta -&gt; Despacho -&gt; Salida de Inventario ->
 * Facturación). El inventario ya se descontó al confirmar el despacho, así
 * que aquí no se vuelve a tocar.
 *
 * Antes existía además una pestaña "Nuevo Registro" que dejaba crear una
 * factura escribiendo cliente/empleado/productos a mano, sin pasar por
 * ninguna Orden de Venta ni Despacho, y esa misma pantalla descontaba
 * inventario por su cuenta. Eso permitía saltarse por completo el flujo
 * obligatorio del proyecto, así que se eliminó: ahora facturar solo es
 * posible desde una orden ya despachada.
 */
public class FrmFacturacion extends javax.swing.JInternalFrame {

    private final controlador.FacturaController controller = new controlador.FacturaController();

    private javax.swing.JComboBox<String> cbxOrdenDespachada;
    private javax.swing.table.DefaultTableModel modeloFacturasGeneradas;

    public FrmFacturacion() {
        initComponents();
        cargarOrdenesDespachadas();
        cargarFacturasGeneradas();
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Facturación");
        setSize(820, 540);
        setLayout(new java.awt.BorderLayout(10, 10));

        javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 10));
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        javax.swing.JPanel panelSuperior = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 8));
        panelSuperior.add(new javax.swing.JLabel("Orden de venta ya despachada:"));
        cbxOrdenDespachada = new javax.swing.JComboBox<>();
        cbxOrdenDespachada.setPreferredSize(new java.awt.Dimension(260, 25));
        panelSuperior.add(cbxOrdenDespachada);
        javax.swing.JButton btnGenerarFactura = new javax.swing.JButton("Generar factura de esta orden");
        btnGenerarFactura.addActionListener(e -> generarFacturaDesdeOrdenDespachada());
        panelSuperior.add(btnGenerarFactura);
        panel.add(panelSuperior, java.awt.BorderLayout.NORTH);

        modeloFacturasGeneradas = new javax.swing.table.DefaultTableModel(
                new String[]{"Factura", "Orden venta", "Cliente", "Total", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        javax.swing.JTable tblFacturas = new javax.swing.JTable(modeloFacturasGeneradas);
        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(tblFacturas);
        scroll.setBorder(javax.swing.BorderFactory.createTitledBorder("Facturas generadas"));
        panel.add(scroll, java.awt.BorderLayout.CENTER);

        add(panel, java.awt.BorderLayout.CENTER);
    }

    private void cargarOrdenesDespachadas() {
        cbxOrdenDespachada.removeAllItems();
        for (modelo.OrdenVenta orden : controller.listarOrdenesListasParaFacturar()) {
            cbxOrdenDespachada.addItem(orden.getIdOrdenVenta() + " - Cliente #" + orden.getCliente().getId()
                    + " - Total Q" + String.format("%.2f", orden.getTotal()));
        }
        if (cbxOrdenDespachada.getItemCount() == 0) {
            cbxOrdenDespachada.addItem("(No hay órdenes despachadas pendientes de facturar)");
        }
    }

    private void cargarFacturasGeneradas() {
        modeloFacturasGeneradas.setRowCount(0);
        for (modelo.Factura factura : controller.listarFacturas()) {
            modeloFacturasGeneradas.addRow(new Object[]{
                factura.getIdFactura(),
                factura.getIdOrdenVenta() == null ? "-" : factura.getIdOrdenVenta(),
                factura.getCliente() != null ? factura.getCliente().getId() : "-",
                String.format("Q%.2f", factura.getTotal()),
                factura.getEstado()
            });
        }
    }

    private void generarFacturaDesdeOrdenDespachada() {
        Object seleccion = cbxOrdenDespachada.getSelectedItem();
        if (seleccion == null) {
            return;
        }
        String texto = seleccion.toString();
        int guion = texto.indexOf(" - ");
        if (guion <= 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Seleccione una orden despachada válida.");
            return;
        }
        int idOrdenVenta = Integer.parseInt(texto.substring(0, guion).trim());

        modelo.Factura factura = controller.generarFacturaDesdeOrden(idOrdenVenta);
        if (factura == null) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "No se pudo generar la factura. Verifique que la orden ya esté despachada.",
                    "Sistema de Ventas", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        javax.swing.JOptionPane.showMessageDialog(this,
                "Factura No. " + factura.getIdFactura() + " generada correctamente.\n"
                + "Total: Q" + String.format("%.2f", factura.getTotal()),
                "Sistema de Ventas", javax.swing.JOptionPane.INFORMATION_MESSAGE);

        cargarOrdenesDespachadas();
        cargarFacturasGeneradas();
    }
}
