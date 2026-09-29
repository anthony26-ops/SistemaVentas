package Vista;

import modelo.DetalleFactura;
import modelo.Producto;

/**
 *
 * @author Aramis Ramos
 */
public class FrmFacturacion extends javax.swing.JInternalFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmFacturacion.class.getName());
    private final controlador.FacturaController controller = new controlador.FacturaController();
    private final java.util.List<DetalleFactura> detallesActuales = new java.util.ArrayList<>();
    private final java.util.Map<String, Producto> productosPorNombre = new java.util.HashMap<>();

    private javax.swing.JComboBox<String> cbxOrdenDespachada;
    private javax.swing.table.DefaultTableModel modeloFacturasGeneradas;

    /**
     * Creates new form FrmFacturacion
     */
    public FrmFacturacion() {
        initComponents();
        cargarCombos();
        construirTabOrdenesDespachadas();
    }

    /**
     * Llena la pestaña "Consultar Historial" (antes vacía) con el paso 7 del
     * flujo obligatorio: generar la factura a partir de una Orden de Venta
     * que YA fue despachada (el inventario ya se descontó en ese momento).
     */
    private void construirTabOrdenesDespachadas() {
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

        jPanel2.removeAll();
        jPanel2.setLayout(new java.awt.BorderLayout());
        jPanel2.add(panel, java.awt.BorderLayout.CENTER);

        cargarOrdenesDespachadas();
        cargarFacturasGeneradas();
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

    /**
     * Carga Cliente/Empleado/Producto reales en los combos (antes traían
     * "Item 1".."Item 4" de ejemplo y nunca se conectaron a los DAO).
     */
    private void cargarCombos() {
        javax.swing.DefaultComboBoxModel<String> modeloClientes = new javax.swing.DefaultComboBoxModel<>();
        for (modelo.Cliente c : controller.obtenerClientes()) {
            modeloClientes.addElement(c.getIdCliente() + " - " + c.getNombre() + " " + c.getApellido());
        }
        cbxCliente.setModel(modeloClientes);

        javax.swing.DefaultComboBoxModel<String> modeloEmpleados = new javax.swing.DefaultComboBoxModel<>();
        for (modelo.Empleado e : controller.obtenerEmpleados()) {
            modeloEmpleados.addElement(e.getIdEmpleado() + " - " + e.getNombre() + " " + e.getApellido());
        }
        cbxEmpleado.setModel(modeloEmpleados);

        javax.swing.DefaultComboBoxModel<String> modeloProductos = new javax.swing.DefaultComboBoxModel<>();
        productosPorNombre.clear();
        for (modelo.Producto p : controller.obtenerProductos()) {
            modeloProductos.addElement(p.getNombre());
            productosPorNombre.put(p.getNombre(), p);
        }
        cbxProducto.setModel(modeloProductos);
    }

    /** Extrae el id de un ítem con formato "id - nombre" de los combos. */
    private int idDesdeItemCombo(Object item) {
        if (item == null) {
            return 0;
        }
        String texto = item.toString();
        int guion = texto.indexOf(" - ");
        return guion > 0 ? Integer.parseInt(texto.substring(0, guion).trim()) : 0;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jTabbedPane3 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        spnFecha = new javax.swing.JSpinner();
        cbxCliente = new javax.swing.JComboBox<>();
        cbxEmpleado = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        cbxProducto = new javax.swing.JComboBox<>();
        spnCantidad = new javax.swing.JSpinner();
        btnAgregar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDetalles = new javax.swing.JTable();
        lblTotal = new javax.swing.JLabel();
        btnGuardar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Facturación");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 854, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 602, Short.MAX_VALUE)
        );

        jTabbedPane3.addTab("Consultar Historial", jPanel2);

        jLabel1.setText("Fecha:");

        jLabel2.setText("Cliente:");

        jLabel3.setText("Vendedor:");

        spnFecha.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.DAY_OF_MONTH));
        spnFecha.setEditor(new javax.swing.JSpinner.DateEditor(spnFecha, "dd/MM/yyyy"));

        cbxCliente.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbxCliente.addActionListener(this::cbxClienteActionPerformed);

        cbxEmpleado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel4.setText("Producto:");

        jLabel5.setText("Cantidad:");

        cbxProducto.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnAgregar.setText("Agregar a la lista");
        btnAgregar.addActionListener(this::btnAgregarActionPerformed);

        tblDetalles.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "Producto", "Cantidad", "Precio Unitario", "Subtotal"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblDetalles);

        lblTotal.setFont(new java.awt.Font("Segoe UI Black", 0, 18)); // NOI18N
        lblTotal.setText("Total: Q0.00");

        btnGuardar.setText("Guardar Factura");
        btnGuardar.addActionListener(this::btnGuardarActionPerformed);

        btnLimpiar.setText("Limpiar Pantalla");
        btnLimpiar.addActionListener(this::btnLimpiarActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel4)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(spnCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnAgregar))
                    .addComponent(cbxCliente, 0, 254, Short.MAX_VALUE)
                    .addComponent(spnFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbxProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbxEmpleado, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(503, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnGuardar)
                .addGap(27, 27, 27)
                .addComponent(btnLimpiar)
                .addGap(172, 172, 172)
                .addComponent(lblTotal)
                .addGap(101, 101, 101))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(spnFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(cbxCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(cbxEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(cbxProducto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(spnCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAgregar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 329, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTotal)
                    .addComponent(btnGuardar)
                    .addComponent(btnLimpiar))
                .addGap(71, 71, 71))
        );

        jTabbedPane3.addTab("Nuevo Registro", jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 418, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTabbedPane2)
                .addContainerGap())
            .addComponent(jTabbedPane3, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTabbedPane1)
                    .addComponent(jTabbedPane2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jTabbedPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 637, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
    try {
    if (tblDetalles.getRowCount() == 0) {
        javax.swing.JOptionPane.showMessageDialog(this, "La factura debe tener detalles.");
        return;
    }
    
    // Extraer datos de la pantalla
    int idCliente = idDesdeItemCombo(cbxCliente.getSelectedItem());
    int idEmpleado = idDesdeItemCombo(cbxEmpleado.getSelectedItem());
    java.util.Date fecha = (java.util.Date) spnFecha.getValue();

    // El estado inicial de la factura (cumpliendo el Punto 1)
    String estado = "Emitida";

    // Guarda la factura con su detalle real de productos (inserta las
    // líneas y descuenta existencia de cada producto).
    boolean exito = controller.crearFactura(idCliente, idEmpleado, detallesActuales, estado, fecha);

    if (exito) {
        javax.swing.JOptionPane.showMessageDialog(this, "¡Factura guardada con éxito!");
        btnLimpiar.doClick(); // Si tienes botón limpiar
    } else {
        javax.swing.JOptionPane.showMessageDialog(this, "Error al registrar la factura.");
    }
} catch (Exception ex) {
    javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage());
}
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void cbxClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxClienteActionPerformed
        // No hace falta reaccionar a la selección de cliente.
    }//GEN-LAST:event_cbxClienteActionPerformed

    private void btnAgregarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarActionPerformed
    if (cbxProducto.getSelectedItem() == null) {
        javax.swing.JOptionPane.showMessageDialog(this,
                "No hay productos en el catálogo todavía. Pídele a André que registre productos primero.",
                "Sin productos", javax.swing.JOptionPane.WARNING_MESSAGE);
        return;
    }
    try {
        // Agrega una línea de producto a la tabla de detalles (antes este
        // botón, por error, duplicaba la lógica de "Guardar factura").
        String producto = cbxProducto.getSelectedItem().toString();
        int cantidad = (Integer) spnCantidad.getValue();

        double precioUnitario = controller.obtenerPrecioProducto(producto);
        double subtotal = cantidad * precioUnitario;

        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) tblDetalles.getModel();
        modelo.addRow(new Object[]{
            modelo.getRowCount() + 1,
            producto,
            cantidad,
            precioUnitario,
            subtotal
        });

        // Guardar la línea real (con el Producto completo) para poder
        // persistirla junto con la factura.
        Producto productoSeleccionado = productosPorNombre.get(producto);
        if (productoSeleccionado != null) {
            DetalleFactura detalle = new DetalleFactura();
            detalle.setProducto(productoSeleccionado);
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setCantidad(cantidad);
            detallesActuales.add(detalle);
        }

        double sumaTotal = 0;
        for (int i = 0; i < modelo.getRowCount(); i++) {
            sumaTotal += Double.parseDouble(modelo.getValueAt(i, 4).toString());
        }
        lblTotal.setText(String.format("Total: Q%.2f", sumaTotal));
    } catch (Exception ex) {
        javax.swing.JOptionPane.showMessageDialog(this,
                "Error al agregar el producto: " + ex.getMessage(),
                "Error",
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }
    }//GEN-LAST:event_btnAgregarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
    // Reiniciar ComboBox
    cargarCombos();
    spnFecha.setValue(new java.util.Date());
    if (cbxCliente.getItemCount() > 0) {
    cbxCliente.setSelectedIndex(0);
}
    if (cbxEmpleado.getItemCount() > 0) {
    cbxEmpleado.setSelectedIndex(0);
}

    // Reiniciar la etiqueta del total
    lblTotal.setText("Total: Q0.00");

    // Vaciar la tabla
    javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) tblDetalles.getModel();
    modelo.setRowCount(0);
    detallesActuales.clear();
    }//GEN-LAST:event_btnLimpiarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton btnAgregar;
    public javax.swing.JButton btnGuardar;
    public javax.swing.JButton btnLimpiar;
    public javax.swing.JComboBox<String> cbxCliente;
    public javax.swing.JComboBox<String> cbxEmpleado;
    public javax.swing.JComboBox<String> cbxProducto;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JSpinner spnFecha;
    public javax.swing.JLabel lblTotal;
    public javax.swing.JSpinner spnCantidad;
    public javax.swing.JTable tblDetalles;
    // End of variables declaration//GEN-END:variables
}
