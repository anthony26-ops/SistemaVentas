package controlador;

import dao.ClienteDAO;
import dao.EmpleadoDAO;
import dao.OrdenVentaDAO;
import dao.impl.ClienteDAOImpl;
import dao.impl.EmpleadoDAOImpl;
import dao.impl.OrdenVentaDAOImpl;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import Vista.FrmOrdenVenta;

public class OrdenVentaController implements ActionListener {
    
    private OrdenVentaDAO ordenDAO;
    private ClienteDAO clienteDAO;
    private EmpleadoDAO empleadoDAO;
    public FrmOrdenVenta vistaOrden; 
    
    public OrdenVentaController(FrmOrdenVenta vistaOrden) {
        this.ordenDAO = new OrdenVentaDAOImpl();
        this.clienteDAO = new ClienteDAOImpl();
        this.empleadoDAO = new EmpleadoDAOImpl();
        this.vistaOrden = vistaOrden;
        
        this.vistaOrden.btnGuardar.addActionListener(this);
        this.vistaOrden.btnLimpiar.addActionListener(this);
        this.vistaOrden.btnAgregar.addActionListener(this);
        
        cargarCombos(); // Llena los ComboBox al iniciar la ventana
    }
    
    private void cargarCombos() {
        try {
            vistaOrden.cbxCliente.removeAllItems();
            vistaOrden.cbxEmpleado.removeAllItems();
            
            // Cargar clientes desde la base de datos usando listarTodos()
            for (modelo.Cliente c : clienteDAO.listarTodos()) {
                vistaOrden.cbxCliente.addItem(c.getNombre() + " " + c.getApellido());
            }
            
            // Cargar empleados desde la base de datos usando listarTodos()
            for (modelo.Empleado emp : empleadoDAO.listarTodos()) {
                vistaOrden.cbxEmpleado.addItem(emp.getNombre() + " " + emp.getApellido());
            }
            
        } catch (Exception e) {
            System.err.println("Error al cargar los datos en los combos de orden: " + e.getMessage());
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        // =======================================================
        // LÓGICA DEL BOTÓN GUARDAR
        // =======================================================
        if (e.getSource() == vistaOrden.btnGuardar) {
            
            // 1. Validar que la tabla tenga productos antes de guardar
            javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) vistaOrden.tblDetalles.getModel();
            if (modelo.getRowCount() == 0) {
                javax.swing.JOptionPane.showMessageDialog(vistaOrden, 
                    "Debe agregar al menos un producto a la orden de venta.", 
                    "Advertencia", 
                    javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                // 2. Crear y poblar el objeto OrdenVenta
                modelo.OrdenVenta orden = new modelo.OrdenVenta();
                
                // Asignar fecha actual
                orden.setFecha(new java.sql.Date(System.currentTimeMillis()));
                
                // Extraer el valor numérico del total desde la etiqueta visual
                String textoTotal = vistaOrden.lblTotal.getText().replace("Total: Q", "").trim();
                orden.setTotal(Double.parseDouble(textoTotal));
                
                // 3. Ejecutar el registro en la base de datos usando el DAO
                boolean exito = ordenDAO.registrarOrden(orden);
                
                if (exito) {
                    javax.swing.JOptionPane.showMessageDialog(vistaOrden, 
                        "¡Orden de venta registrada con éxito en PostgreSQL!", 
                        "Éxito", 
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    
                    // Limpiamos el formulario automáticamente tras guardar
                    vistaOrden.btnLimpiar.doClick();
                } else {
                    javax.swing.JOptionPane.showMessageDialog(vistaOrden, 
                        "No se pudo registrar la orden en la base de datos.", 
                        "Error", 
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception ex) {
                System.err.println("Error crítico al procesar el guardado de la orden: " + ex.getMessage());
                javax.swing.JOptionPane.showMessageDialog(vistaOrden, 
                    "Ocurrió un error inesperado al guardar la orden.", 
                    "Error", 
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        } 
        // =======================================================
        // LÓGICA DEL BOTÓN LIMPIAR
        // =======================================================
        else if (e.getSource() == vistaOrden.btnLimpiar) {
            
            if (vistaOrden.cbxCliente.getItemCount() > 0) {
                vistaOrden.cbxCliente.setSelectedIndex(0);
            }
            if (vistaOrden.cbxEmpleado.getItemCount() > 0) {
                vistaOrden.cbxEmpleado.setSelectedIndex(0);
            }
            
            vistaOrden.lblTotal.setText("Total: Q0.00");
            
            javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) vistaOrden.tblDetalles.getModel();
            modelo.setRowCount(0); 
            
            System.out.println("Formulario de orden limpiado correctamente.");
        }
        // =======================================================
        // LÓGICA DEL BOTÓN AGREGAR
        // =======================================================
        else if (e.getSource() == vistaOrden.btnAgregar) {
            
            String producto = vistaOrden.cbxProducto.getSelectedItem().toString();
            int cantidad = (int) vistaOrden.spnCantidad.getValue();
            
            double precioUnitario = 150.00; 
            double subtotal = cantidad * precioUnitario;
            
            javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) vistaOrden.tblDetalles.getModel();
            
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
            
            vistaOrden.lblTotal.setText(String.format("Total: Q%.2f", sumaTotal));
            
            System.out.println("Producto agregado a la orden correctamente.");
        }
    } 
}