package com.mycompany.sistemaventas;

import Vista.FrmMenuPrincipal;
import bd.ConexionException;
import bd.ConexionPostgreSQL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SistemaVentas {

    private static final Logger logger = Logger.getLogger(SistemaVentas.class.getName());

    public static void main(String[] args) {
        try {
            ConexionPostgreSQL.inicializar();
        } catch (ConexionException ex) {
            logger.log(Level.SEVERE, "No se pudo inicializar la base de datos", ex);
        }

        java.awt.EventQueue.invokeLater(() -> new FrmMenuPrincipal().setVisible(true));
    }
}
