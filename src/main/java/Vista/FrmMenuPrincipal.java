/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package Vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import modelo.Usuario;

/**
 * Menú principal en estilo escritorio (menú + barra de herramientas +
 * área de trabajo tipo JDesktopPane), armado dinámicamente según el rol
 * del Usuario que inició sesión en FrmLogin. Al ser un diseño condicional
 * (las opciones cambian por rol) se construye directamente en código en
 * lugar de con el editor visual de NetBeans.
 *
 * @author anthonyjolon
 */
public class FrmMenuPrincipal extends JFrame {

    private static final Logger logger = Logger.getLogger(FrmMenuPrincipal.class.getName());

    private final Usuario usuarioActual;
    private JDesktopPane desktopPane;

    public FrmMenuPrincipal(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        initComponents();
        construirMenu();
        construirBarraHerramientas();
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Sistema de Ventas");
        setSize(900, 600);
        setLocationRelativeTo(null);

        desktopPane = new JDesktopPane();
        desktopPane.setBackground(new Color(45, 66, 96));

        JLabel lblEstado = new JLabel(
                " Usuario: " + usuarioActual.getNombreUsuario()
                + "   |   Rol: " + usuarioActual.getRol());
        lblEstado.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JLabel lblBienvenida = new JLabel("Sistema de Ventas", SwingConstants.CENTER);
        lblBienvenida.setForeground(Color.WHITE);
        lblBienvenida.setFont(lblBienvenida.getFont().deriveFont(28f));
        lblBienvenida.setBounds(0, 0, 900, 500);
        desktopPane.add(lblBienvenida);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(desktopPane, BorderLayout.CENTER);
        getContentPane().add(lblEstado, BorderLayout.SOUTH);
    }

    private void construirBarraHerramientas() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton btnCerrarSesion = new JButton("Cerrar sesión");
        btnCerrarSesion.addActionListener(e -> cerrarSesion());
        toolBar.add(btnCerrarSesion);

        getContentPane().add(toolBar, BorderLayout.NORTH);
    }

    private void construirMenu() {
        JMenuBar menuBar = new JMenuBar();
        String rol = usuarioActual.getRol();

        JMenu menuModulos = new JMenu("Módulos");
        agregarItemSiPuede(menuModulos, "Clientes", "Vista.FrmClientes", rol, "ADMIN", "CAJA");
        agregarItemSiPuede(menuModulos, "Empleados", "Vista.FrmEmpleados", rol, "ADMIN");
        agregarItemSiPuede(menuModulos, "Productos", "Vista.FrmProductos", rol, "ADMIN", "BODEGA");
        agregarItemSiPuede(menuModulos, "Bodega / Inventario", "Vista.FrmBodegas", rol, "ADMIN", "BODEGA");
        agregarItemSiPuede(menuModulos, "Hacer Pedido", "Vista.FrmOrdenVenta", rol, "ADMIN", "VENDEDOR");
        agregarItemSiPuede(menuModulos, "Despacho", "Vista.FrmOrdenDespacho", rol, "ADMIN", "BODEGA");
        agregarItemSiPuede(menuModulos, "Facturación", "Vista.FrmFacturacion", rol, "ADMIN", "CAJA");
        agregarItemSiPuede(menuModulos, "Pagos", "Vista.FrmPago", rol, "ADMIN", "CAJA");
        if (menuModulos.getItemCount() > 0) {
            menuBar.add(menuModulos);
        }

        JMenu menuSesion = new JMenu("Sesión");
        JMenuItem itemCerrar = new JMenuItem("Cerrar sesión");
        itemCerrar.addActionListener(e -> cerrarSesion());
        JMenuItem itemSalir = new JMenuItem("Salir");
        itemSalir.addActionListener(e -> System.exit(0));
        menuSesion.add(itemCerrar);
        menuSesion.add(itemSalir);
        menuBar.add(menuSesion);

        setJMenuBar(menuBar);
    }

    /**
     * Agrega el item de menú solo si el rol del usuario actual está entre
     * los roles permitidos para ese módulo.
     */
    private void agregarItemSiPuede(JMenu menu, String texto, String claseFQN, String rolActual,
            String... rolesPermitidos) {
        for (String rolPermitido : rolesPermitidos) {
            if (rolPermitido.equalsIgnoreCase(rolActual)) {
                JMenuItem item = new JMenuItem(texto);
                item.addActionListener(e -> abrirVentana(claseFQN, texto));
                menu.add(item);
                return;
            }
        }
    }

    /**
     * Abre una pantalla del sistema por su nombre de clase completo.
     * Se usa reflexión para que este menú funcione aunque la pantalla
     * de un compañero todavía no exista o esté en desarrollo. Soporta tanto
     * pantallas JFrame (la mayoría) como JInternalFrame (Cristofer las hizo
     * así para el módulo de Cliente/Empleado) abriendo estas últimas dentro
     * del JDesktopPane en vez de como ventana aparte.
     */
    private void abrirVentana(String claseFQN, String nombreModulo) {
        try {
            Class<?> clazz = Class.forName(claseFQN);
            Object instancia = clazz.getDeclaredConstructor().newInstance();
            if (instancia instanceof javax.swing.JInternalFrame internalFrame) {
                desktopPane.add(internalFrame);
                internalFrame.setVisible(true);
                try {
                    internalFrame.setSelected(true);
                } catch (java.beans.PropertyVetoException ignored) {
                }
            } else if (instancia instanceof JFrame frame) {
                frame.setLocationRelativeTo(this);
                frame.setVisible(true);
            }
        } catch (ReflectiveOperationException | ClassCastException ex) {
            logger.log(Level.WARNING, "No se pudo abrir el módulo " + nombreModulo, ex);
            JOptionPane.showMessageDialog(this,
                    "El módulo de " + nombreModulo + " aún no está disponible.",
                    "Sistema de Ventas",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void cerrarSesion() {
        dispose();
        java.awt.EventQueue.invokeLater(() -> new FrmLogin().setVisible(true));
    }
}
