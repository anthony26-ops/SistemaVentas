package controlador;

import dao.ClienteDAO;
import dao.EmpleadoDAO;
import dao.FacturaDAO;
import dao.impl.ClienteDAOImpl;
import dao.impl.EmpleadoDAOImpl;
import dao.impl.FacturaDAOImpl;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import Vista.FrmFacturacion;

public class FacturaController implements ActionListener {
    
    private FacturaDAO facturaDAO;
    private ClienteDAO clienteDAO;
    private EmpleadoDAO empleadoDAO;
    public FrmFacturacion vistaFactura; 
    
    public FacturaController(FrmFacturacion vistaFactura) {
        this.facturaDAO = new FacturaDAOImpl();
        this.clienteDAO = new ClienteDAOImpl();
        this.empleadoDAO = new EmpleadoDAOImpl();
        this.vistaFactura = vistaFactura;
        
        this.vistaFactura.btnGuardar.addActionListener(this);
        this.vistaFactura.btnLimpiar.addActionListener(this);
        this.vistaFactura.btnAgregar.addActionListener(this);
        
        cargarCombos(); // Llena los ComboBox al iniciar la ventana
    }
    
    private void cargarCombos() {
        try {
            vistaFactura.cbxCliente.removeAllItems();
            vistaFactura.cbxEmpleado.removeAllItems();
            
            // Cargar clientes desde la base de datos
            for (modelo.Cliente c : clienteDAO.listarTodos()) {
                vistaFactura.cbxCliente.addItem(c.getNombre() + " " + c.getApellido());
            }
            
            // Cargar empleados desde la base de datos
            for (modelo.Empleado emp : empleadoDAO.listarTodos()) {
                vistaFactura.cbxEmpleado.addItem(emp.getNombre() + " " + emp.getApellido());
            }
            
        } catch (Exception e) {
            System.err.println("Error al cargar los datos en los combos: " + e.getMessage());
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        // =======================================================
        // LÓGICA DEL BOTÓN GUARDAR (FACTURA)
        // =======================================================
        if (e.getSource() == vistaFactura.btnGuardar) {
            
            // 1. Validar que la tabla tenga productos antes de guardar
            javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) vistaFactura.tblDetalles.getModel();
            if (modelo.getRowCount() == 0) {
                javax.swing.JOptionPane.showMessageDialog(vistaFactura, 
                    "Debe agregar al menos un producto a la factura.", 
                    "Advertencia", 
                    javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                // 2. Crear y poblar el objeto Factura
                modelo.Factura factura = new modelo.Factura();
                
                // Asignar fecha actual
                factura.setFecha(new java.sql.Date(System.currentTimeMillis()));
                
                // Extraer el valor numérico del total desde la etiqueta visual
                String textoTotal = vistaFactura.lblTotal.getText().replace("Total: Q", "").trim();
                factura.setTotal(Double.parseDouble(textoTotal));
                
                // 3. Ejecutar el registro en la base de datos usando el DAO
                boolean exito = facturaDAO.registrarFactura(factura);
                
                if (exito) {
                    javax.swing.JOptionPane.showMessageDialog(vistaFactura, 
                        "¡Factura registrada con éxito en PostgreSQL!", 
                        "Éxito", 
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    
                    // Limpiamos el formulario automáticamente tras guardar
                    vistaFactura.btnLimpiar.doClick();
                } else {
                    javax.swing.JOptionPane.showMessageDialog(vistaFactura, 
                        "No se pudo registrar la factura en la base de datos.", 
                        "Error", 
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception ex) {
                System.err.println("Error crítico al procesar el guardado: " + ex.getMessage());
                javax.swing.JOptionPane.showMessageDialog(vistaFactura, 
                    "Ocurrió un error inesperado al guardar.", 
                    "Error", 
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        } 
        // =======================================================
        // LÓGICA DEL BOTÓN LIMPIAR (FACTURA)
        // =======================================================
        else if (e.getSource() == vistaFactura.btnLimpiar) {
            
            if (vistaFactura.cbxCliente.getItemCount() > 0) {
                vistaFactura.cbxCliente.setSelectedIndex(0);
            }
            if (vistaFactura.cbxEmpleado.getItemCount() > 0) {
                vistaFactura.cbxEmpleado.setSelectedIndex(0);
            }
            
            vistaFactura.lblTotal.setText("Total: Q0.00");
            
            javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) vistaFactura.tblDetalles.getModel();
            modelo.setRowCount(0); 
            
            System.out.println("Formulario de factura limpiado correctamente.");
        }
        // =======================================================
        // LÓGICA DEL BOTÓN AGREGAR
        // =======================================================
        else if (e.getSource() == vistaFactura.btnAgregar) {
            
            String producto = vistaFactura.cbxProducto.getSelectedItem().toString();
            int cantidad = (int) vistaFactura.spnCantidad.getValue();
            
            double precioUnitario = 150.00; 
            double subtotal = cantidad * precioUnitario;
            
            javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) vistaFactura.tblDetalles.getModel();
            
            modelo.addRow(new Object[]{
                modelo.getRowCount() + 1, 
                producto,
                cantidad,
                precioUnitario,
                subtotal
            });
            
            double sumaTotal = 0;
            for (int i = 0; i < modelo.getRowCount(); i++) {
                sumaTotal += Double.parseDouble(modelo.getValueAt(i, 4).toString()); 
            }
            
            vistaFactura.lblTotal.setText(String.format("Total: Q%.2f", sumaTotal));
            
            System.out.println("Producto agregado a la tabla correctamente.");
        }
    }
}