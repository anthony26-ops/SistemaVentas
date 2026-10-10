package com.mycompany.sistemaventas;

import Vista.FrmLogin;
import Vista.FrmMenuPrincipal;
import bd.ConexionException;
import bd.ConexionMySQL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import modelo.Empleado;
import modelo.Usuario;

public class SistemaVentas {

    private static final Logger logger = Logger.getLogger(SistemaVentas.class.getName());

    /**
     * Si es false el sistema abre directo en el menú principal como
     * administrador, sin pedir usuario ni contraseña (así nadie queda
     * bloqueado por una base de datos distinta a la de quien escribió el
     * código). Para volver a exigir login basta con ponerlo en true: la
     * pantalla FrmLogin y la tabla ventas_usuarios siguen intactas.
     */
    public static final boolean REQUIERE_LOGIN = false;

    public static void main(String[] args) {
        try {
            ConexionMySQL.inicializar();
        } catch (ConexionException ex) {
            logger.log(Level.SEVERE, "No se pudo inicializar la base de datos", ex);
            avisarErrorDeBaseDeDatos(ex);
        }

        java.awt.EventQueue.invokeLater(() -> {
            if (REQUIERE_LOGIN) {
                new FrmLogin().setVisible(true);
            } else {
                new FrmMenuPrincipal(usuarioAdministrador()).setVisible(true);
            }
        });
    }

    /** Usuario ADMIN en memoria (no se consulta la base de datos). */
    private static Usuario usuarioAdministrador() {
        Empleado empleado = new Empleado();
        empleado.setIdEmpleado(1);
        return new Usuario(1, "admin", null, "ADMIN", true, empleado);
    }

    /**
     * Antes este error solo salía en la consola y el usuario veía un
     * mensaje engañoso ("usuario o contraseña incorrectos"). Ahora se le
     * dice la causa real y qué revisar.
     */
    private static void avisarErrorDeBaseDeDatos(ConexionException ex) {
        Throwable causa = ex;
        while (causa.getCause() != null) {
            causa = causa.getCause();
        }
        JOptionPane.showMessageDialog(null,
                "No se pudo preparar la base de datos MySQL:\n\n" + causa.getMessage() + "\n\n"
                + "Revise que MySQL esté corriendo en 127.0.0.1:3306 y que el usuario/contraseña\n"
                + "de MySQL sean correctos (por defecto: usuario root, sin contraseña).\n"
                + "Si su root tiene contraseña, inicie el programa con la variable de entorno\n"
                + "DB_PASSWORD definida (y DB_USER si el usuario no es root).\n"
                + "Si la base quedó a medias de una versión anterior: DROP DATABASE sistema_ventas;",
                "Sistema de Ventas - Error de base de datos", JOptionPane.ERROR_MESSAGE);
    }
}
