package controlador;

import dao.PagoDAO;
import dao.impl.PagoDAOImpl;
import java.util.Date;
import java.util.List;
import modelo.IMetodoPago;
import modelo.Pago;
import modelo.PagoEfectivo;
import modelo.PagoTarjeta;
import modelo.PagoTransferencia;

/**
 * Controlador del modulo de Pagos. Contiene unicamente logica de negocio:
 * no importa ninguna clase de Swing, por lo que puede reutilizarse desde
 * otra interfaz (web o movil) sin modificarlo.
 */
public class PagoController {

    private final PagoDAO pagoDAO;

    public PagoController() {
        this.pagoDAO = new PagoDAOImpl();
    }

    /**
     * Fabrica el metodo de pago correspondiente segun el tipo elegido.
     * Aqui se aplica el polimorfismo: todos se devuelven como IMetodoPago.
     */
    public IMetodoPago crearMetodoPago(String tipo, String datoUno, String datoDos) {
        if (tipo == null) {
            return null;
        }
        switch (tipo.toUpperCase()) {
            case "EFECTIVO":
                double recibido;
                try {
                    recibido = Double.parseDouble(datoUno);
                } catch (NumberFormatException e) {
                    recibido = 0;
                }
                return new PagoEfectivo(recibido);
            case "TARJETA":
                return new PagoTarjeta(datoUno, datoDos);
            case "TRANSFERENCIA":
                return new PagoTransferencia(datoUno, datoDos);
            default:
                return null;
        }
    }

    /**
     * Registra un pago para una factura. Valida el monto y delega la
     * validacion propia de cada metodo en la implementacion de IMetodoPago.
     *
     * @return el objeto Pago ya guardado.
     * @throws Exception si algun dato no es valido.
     */
    public Pago registrarPago(int idFactura, double monto, IMetodoPago metodoPago) throws Exception {
        if (idFactura <= 0) {
            throw new Exception("Debe indicar la factura a la que se aplica el pago.");
        }
        if (monto <= 0) {
            throw new Exception("El monto del pago debe ser mayor a cero.");
        }
        if (metodoPago == null) {
            throw new Exception("Debe seleccionar un metodo de pago valido.");
        }
        if (!metodoPago.validar(monto)) {
            throw new Exception(metodoPago.getMensajeValidacion());
        }

        Pago pago = new Pago();
        pago.setIdFactura(idFactura);
        pago.setFecha(new Date());
        pago.setMonto(monto);
        pago.setTipoPago(metodoPago.getTipo());
        pago.setReferencia(metodoPago.generarReferencia());
        pago.setEstado("COMPLETADO");

        if (!pagoDAO.insertar(pago)) {
            throw new Exception("No se pudo guardar el pago en la base de datos.");
        }
        return pago;
    }

    /** Anula un pago sin borrarlo, para conservar el historial. */
    public boolean anularPago(int idPago) throws Exception {
        Pago pago = pagoDAO.obtenerPorId(idPago);
        if (pago == null) {
            throw new Exception("No existe un pago con el codigo indicado.");
        }
        pago.setEstado("ANULADO");
        return pagoDAO.actualizar(pago);
    }

    public boolean eliminarPago(int idPago) {
        return pagoDAO.eliminar(idPago);
    }

    public List<Pago> listarPagos() {
        return pagoDAO.listarTodos();
    }

    public List<Pago> listarPagosDeFactura(int idFactura) {
        return pagoDAO.listarPorFactura(idFactura);
    }

    /** Calcula cuanto falta por pagar de una factura. */
    public double calcularSaldoPendiente(int idFactura, double totalFactura) {
        double pagado = pagoDAO.totalPagadoPorFactura(idFactura);
        double saldo = totalFactura - pagado;
        return saldo < 0 ? 0 : saldo;
    }

    /** Indica si la factura ya quedo totalmente cancelada. */
    public boolean facturaEstaSaldada(int idFactura, double totalFactura) {
        return calcularSaldoPendiente(idFactura, totalFactura) <= 0;
    }
}
