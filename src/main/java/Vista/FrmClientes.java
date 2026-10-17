/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JInternalFrame.java to edit this template
 */
package Vista;

import controlador.ClienteController;
import modelo.Cliente;
import modelo.Telefono;
import modelo.TipoTelefono;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

public class FrmClientes extends JInternalFrame {

    private ClienteController controller;
    private DefaultTableModel modeloTabla;

    // Teléfonos del cliente que se está registrando o editando (uno o varios).
    // Viven en memoria hasta que se pulsa Guardar/Actualizar.
    private final List<Telefono> telefonosEnEdicion = new ArrayList<>();
    private DefaultTableModel modeloTelefonos;
    private JTable tblTelefonos;
    private JComboBox<TipoTelefono> cbxTipoTelefono;
    private JTextField txtNumeroTelefono;

    public FrmClientes() {
        initComponents();
        construirPanelTelefonos();
        controller = new ClienteController();
        configurarTabla();
        cargarTabla();
    }

    private void configurarTabla() {
        modeloTabla = new DefaultTableModel(new String[]{"ID", "Nombre", "Apellido", "Teléfonos", "Email", "NIT"}, 0);
        tablaClientes.setModel(modeloTabla);
        // La columna de teléfonos muestra varios ("55551111 (Personal), 22223333 (Trabajo)"): más ancha.
        tablaClientes.getColumnModel().getColumn(0).setPreferredWidth(40);
        tablaClientes.getColumnModel().getColumn(3).setPreferredWidth(300);
        // Sin este listener, hacer clic en una fila no cargaba nada en el
        // formulario: como txtId está deshabilitado, no había forma de
        // indicar qué cliente actualizar o eliminar.
        tablaClientes.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablaClientesMouseClicked(evt);
            }
        });
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        List<Cliente> clientes = controller.listarClientes();
        if (clientes != null) {
            for (Cliente c : clientes) {
                modeloTabla.addRow(new Object[]{
                    c.getIdCliente(), c.getNombre(), c.getApellido(), c.getTelefonosResumen(), c.getEmail(), c.getNit()
                });
            }
        }
    }

    private void limpiarCampos() {
        txtId.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtEmail.setText("");
        txtNit.setText("");
        telefonosEnEdicion.clear();
        refrescarTablaTelefonos();
        limpiarEntradaTelefono();
    }

    /**
     * Reemplaza, dentro del layout generado por el Diseñador, el campo de texto del
     * teléfono único (txtTelefono) por un panel para trabajar con VARIOS teléfonos:
     * tipo + número + botones Agregar/Modificar/Quitar + una tabla con la lista.
     * Se hace por código, después de initComponents(), para no tocar el bloque
     * "Generated Code" ni el archivo .form (así el Diseñador sigue funcionando).
     */
    private void construirPanelTelefonos() {
        cbxTipoTelefono = new JComboBox<>(TipoTelefono.values());
        txtNumeroTelefono = new JTextField(14);

        modeloTelefonos = new DefaultTableModel(new String[]{"Número", "Tipo"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tblTelefonos = new JTable(modeloTelefonos);
        tblTelefonos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(tblTelefonos);
        scroll.setPreferredSize(new java.awt.Dimension(260, 72));
        // Al seleccionar un teléfono de la lista, sus datos pasan a los campos para poder modificarlo.
        tblTelefonos.getSelectionModel().addListSelectionListener(e -> {
            int fila = tblTelefonos.getSelectedRow();
            if (!e.getValueIsAdjusting() && fila >= 0 && fila < telefonosEnEdicion.size()) {
                Telefono t = telefonosEnEdicion.get(fila);
                cbxTipoTelefono.setSelectedItem(t.getTipo());
                txtNumeroTelefono.setText(t.getNumero());
            }
        });

        JButton btnAgregarTel = new JButton("Agregar");
        JButton btnModificarTel = new JButton("Modificar");
        JButton btnQuitarTel = new JButton("Quitar");
        btnAgregarTel.addActionListener(e -> agregarTelefono());
        btnModificarTel.addActionListener(e -> modificarTelefonoSeleccionado());
        btnQuitarTel.addActionListener(e -> quitarTelefonoSeleccionado());

        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        fila.add(cbxTipoTelefono);
        fila.add(txtNumeroTelefono);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        botones.add(btnAgregarTel);
        botones.add(btnModificarTel);
        botones.add(btnQuitarTel);

        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.add(fila, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);

        GroupLayout layout = (GroupLayout) getContentPane().getLayout();
        layout.replace(txtTelefono, panel);
        jLabel5.setText("Teléfonos :");
        jLabel5.setVerticalAlignment(SwingConstants.TOP);
        pack();
    }

    private void agregarTelefono() {
        try {
            Telefono t = controller.crearTelefonoValidado(txtNumeroTelefono.getText(),
                    (TipoTelefono) cbxTipoTelefono.getSelectedItem(), telefonosEnEdicion);
            telefonosEnEdicion.add(t);
            refrescarTablaTelefonos();
            limpiarEntradaTelefono();
            txtNumeroTelefono.requestFocusInWindow();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Teléfono no válido", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void modificarTelefonoSeleccionado() {
        int fila = tblTelefonos.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione en la lista el teléfono que desea modificar.");
            return;
        }
        try {
            // Los repetidos se buscan entre los DEMÁS teléfonos (no contra sí mismo).
            List<Telefono> otros = new ArrayList<>(telefonosEnEdicion);
            otros.remove(fila);
            Telefono t = controller.crearTelefonoValidado(txtNumeroTelefono.getText(),
                    (TipoTelefono) cbxTipoTelefono.getSelectedItem(), otros);
            telefonosEnEdicion.set(fila, t);
            refrescarTablaTelefonos();
            limpiarEntradaTelefono();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Teléfono no válido", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void quitarTelefonoSeleccionado() {
        int fila = tblTelefonos.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione en la lista el teléfono que desea quitar.");
            return;
        }
        telefonosEnEdicion.remove(fila);
        refrescarTablaTelefonos();
        limpiarEntradaTelefono();
    }

    private void refrescarTablaTelefonos() {
        if (modeloTelefonos == null) {
            return;
        }
        modeloTelefonos.setRowCount(0);
        for (Telefono t : telefonosEnEdicion) {
            modeloTelefonos.addRow(new Object[]{t.getNumero(), t.getTipo()});
        }
    }

    private void limpiarEntradaTelefono() {
        if (txtNumeroTelefono == null) {
            return;
        }
        txtNumeroTelefono.setText("");
        cbxTipoTelefono.setSelectedIndex(0);
        tblTelefonos.clearSelection();
    }

    /**
     * Si el usuario escribió un número y pulsó Guardar sin pulsar "Agregar", ese número
     * se agrega solo (para que no se pierda en silencio). Si hay un teléfono seleccionado
     * con cambios sin aplicar, se avisa en vez de descartarlos.
     *
     * @return true si se puede continuar con el guardado.
     */
    private boolean agregarTelefonoPendiente() {
        String numero = txtNumeroTelefono.getText().trim();
        if (numero.isEmpty()) {
            return true;
        }
        int sel = tblTelefonos.getSelectedRow();
        if (sel >= 0) {
            Telefono t = telefonosEnEdicion.get(sel);
            boolean sinCambios = t.getNumero().equals(numero) && t.getTipo() == cbxTipoTelefono.getSelectedItem();
            if (sinCambios) {
                return true;
            }
            JOptionPane.showMessageDialog(this,
                    "Tiene cambios sin aplicar en el teléfono seleccionado.\nPulse \"Modificar\" para aplicarlos "
                    + "o quite la selección antes de guardar.", "Teléfono pendiente", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            telefonosEnEdicion.add(controller.crearTelefonoValidado(numero,
                    (TipoTelefono) cbxTipoTelefono.getSelectedItem(), telefonosEnEdicion));
            refrescarTablaTelefonos();
            limpiarEntradaTelefono();
            return true;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Teléfono no válido", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jDesktopPane1 = new javax.swing.JDesktopPane();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaClientes = new javax.swing.JTable();
        btnGuardar = new javax.swing.JButton();
        btnActualizar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        txtId = new javax.swing.JTextField();
        txtNombre = new javax.swing.JTextField();
        txtApellido = new javax.swing.JTextField();
        txtTelefono = new javax.swing.JTextField();
        txtEmail = new javax.swing.JTextField();
        txtNit = new javax.swing.JTextField();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Clientes");

        javax.swing.GroupLayout jDesktopPane1Layout = new javax.swing.GroupLayout(jDesktopPane1);
        jDesktopPane1.setLayout(jDesktopPane1Layout);
        jDesktopPane1Layout.setHorizontalGroup(
            jDesktopPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jDesktopPane1Layout.setVerticalGroup(
            jDesktopPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("CLIENTES");

        jLabel2.setText("ID :");

        jLabel3.setText("Nombre :");

        jLabel4.setText("Apellido:");

        jLabel5.setText("Telefono :");

        jLabel6.setText("Email :");

        jLabel7.setText("NIT :");

        tablaClientes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Nombre", "Apellido", "Telefono", "Email", "Nit"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tablaClientes);

        btnGuardar.setText("Guardar");
        btnGuardar.addActionListener(this::btnGuardarActionPerformed);

        btnActualizar.setText("Actualizar");
        btnActualizar.addActionListener(this::btnActualizarActionPerformed);

        btnEliminar.setText("Eliminar");
        btnEliminar.addActionListener(this::btnEliminarActionPerformed);

        txtId.setEnabled(false);
        txtId.addActionListener(this::txtIdActionPerformed);

        txtEmail.addActionListener(this::txtEmailActionPerformed);

        txtNit.addActionListener(this::txtNitActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5))
                        .addGap(22, 22, 22)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtId, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                            .addComponent(txtNombre)
                            .addComponent(txtApellido)
                            .addComponent(txtTelefono))
                        .addGap(44, 44, 44)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel6)
                                .addComponent(jLabel7))
                            .addGap(18, 18, 18)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtEmail, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                                .addComponent(txtNit)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 116, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnEliminar)
                            .addComponent(btnGuardar)
                            .addComponent(btnActualizar))
                        .addGap(56, 56, 56))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jDesktopPane1))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jDesktopPane1)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel6)
                    .addComponent(btnGuardar)
                    .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel7)
                    .addComponent(btnActualizar)
                    .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(btnEliminar)
                    .addComponent(txtApellido, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 254, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIdActionPerformed

    private void txtEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtEmailActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtEmailActionPerformed

    private void txtNitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNitActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNitActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
       if (txtNombre.getText().trim().isEmpty() || txtApellido.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete al menos nombre y apellido.");
            return;
       }
       if (!agregarTelefonoPendiente()) {
            return;
       }
       try {
            if (controller.guardarCliente(txtNombre.getText(), txtApellido.getText(), txtEmail.getText(), txtNit.getText(),
                    new ArrayList<>(telefonosEnEdicion))) {
                JOptionPane.showMessageDialog(this, "Cliente guardado exitosamente.");
                cargarTabla();
                limpiarCampos();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo guardar el cliente.", "Error", JOptionPane.ERROR_MESSAGE);
            }
       } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo guardar el cliente", JOptionPane.WARNING_MESSAGE);
       }
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnActualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarActionPerformed
        if (txtId.getText().isEmpty()) return;
        if (!agregarTelefonoPendiente()) {
            return;
        }
        try {
            int id = Integer.parseInt(txtId.getText().trim());
            if (controller.modificarCliente(id, txtNombre.getText(), txtApellido.getText(), txtEmail.getText(), txtNit.getText(),
                    new ArrayList<>(telefonosEnEdicion))) {
                JOptionPane.showMessageDialog(this, "Cliente actualizado.");
                cargarTabla();
                limpiarCampos();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar el cliente.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El ID debe ser numérico.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo actualizar el cliente", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_btnActualizarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        if (txtId.getText().isEmpty()) return;
        try {
            int id = Integer.parseInt(txtId.getText().trim());
            if (controller.borrarCliente(id)) {
                JOptionPane.showMessageDialog(this, "Cliente eliminado.");
                cargarTabla();
                limpiarCampos();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se pudo eliminar: el cliente ya tiene pedidos/facturas registrados.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El ID debe ser numérico.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void tablaClientesMouseClicked(java.awt.event.MouseEvent evt) {                                           
        int fila = tablaClientes.getSelectedRow();
        if (fila >= 0) {
            int id = Integer.parseInt(textoDeCelda(fila, 0));
            // Se vuelve a consultar el cliente en la base de datos: trae también todos
            // sus teléfonos (esto es lo que demuestra que quedaron persistidos).
            Cliente c = controller.obtenerCliente(id);
            if (c == null) {
                return;
            }
            txtId.setText(String.valueOf(c.getIdCliente()));
            txtNombre.setText(valorONada(c.getNombre()));
            txtApellido.setText(valorONada(c.getApellido()));
            txtEmail.setText(valorONada(c.getEmail()));
            txtNit.setText(valorONada(c.getNit()));
            telefonosEnEdicion.clear();
            telefonosEnEdicion.addAll(c.getTelefonos());
            refrescarTablaTelefonos();
            limpiarEntradaTelefono();
        }
    }

    /** Texto de una celda; una celda NULL en la base (ej. email viejo) se muestra vacía en vez de lanzar NullPointerException. */
    private String textoDeCelda(int fila, int columna) {
        Object valor = modeloTabla.getValueAt(fila, columna);
        return valor == null ? "" : valor.toString();
    }

    private String valorONada(String valor) {
        return valor == null ? "" : valor;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JDesktopPane jDesktopPane1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tablaClientes;
    private javax.swing.JTextField txtApellido;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtNit;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtTelefono;
    // End of variables declaration//GEN-END:variables
}
