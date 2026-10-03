package controlador;

import dao.FacturaDAO;
import dao.PagoDAO;
import dao.impl.FacturaDAOImpl;
import dao.impl.PagoDAOImpl;
import java.util.Date;
import java.util.List;
import modelo.Factura;
import modelo.IMetodoPago;
import modelo.Pago;
import modelo.PagoEfectivo;

/**
 * Controlador del modulo de Pagos. Contiene unicamente logica de negocio:
 * no importa ninguna clase de Swing, por lo que puede reutilizarse desde
 * otra interfaz (web o movil) sin modificarlo.
 *
 * Por ahora solo se acepta EFECTIVO (se quitaron Tarjeta y Transferencia
 * a pedido): son los únicos métodos que de verdad se verificaban contra
 * algo (el monto recibido en efectivo); los otros dos solo pedían un
 * número de tarjeta/boleta sin validar nada real.
 */
public class PagoController {

    private final PagoDAO pagoDAO;
    private final FacturaDAO facturaDAO;

    public PagoController() {
        this.pagoDAO = new PagoDAOImpl();
        this.facturaDAO = new FacturaDAOImpl();
    }

    /**
     * Fabrica el metodo de pago correspondiente segun el tipo elegido.
     * Aqui se aplica el polimorfismo: todos se devuelven como IMetodoPago.
     */
    public IMetodoPago crearMetodoPago(String tipo, String datoUno, String datoDos) {
        if (tipo == null) {
            return null;
        }
        if ("EFECTIVO".equalsIgnoreCase(tipo)) {
            double recibido;
            try {
                recibido = Double.parseDouble(datoUno);
            } catch (NumberFormatException e) {
                recibido = 0;
            }
            return new PagoEfectivo(recibido);
        }
        return null;
    }

    /**
     * Registra un pago para una factura. Valida el monto, que no exceda el
     * saldo pendiente REAL de la factura (consultado aquí mismo, no
     * recibido como parámetro: antes se podía registrar cualquier monto
     * porque nadie comparaba contra el total real de la factura), y delega
     * la validacion propia de cada metodo en la implementacion de
     * IMetodoPago.
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

        Factura factura = facturaDAO.obtenerFacturaPorId(idFactura);
        if (factura == null) {
            throw new Exception("No existe ninguna factura con el número " + idFactura + ".");
        }
        double saldoPendiente = calcularSaldoPendiente(idFactura);
        if (saldoPendiente <= 0) {
            throw new Exception("La factura No. " + idFactura + " ya está totalmente pagada.");
        }
        if (monto > saldoPendiente + 0.01) {
            throw new Exception("El monto a pagar (Q" + String.format("%.2f", monto)
                    + ") no puede ser mayor al saldo pendiente de la factura (Q"
                    + String.format("%.2f", saldoPendiente) + ").");
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

    /**
     * Calcula cuanto falta por pagar de una factura, consultando su total
     * real en la base de datos (antes había que escribirlo a mano en un
     * cuadro de diálogo, lo cual era propenso a errores y desconectado del
     * total verdadero).
     */
    public double calcularSaldoPendiente(int idFactura) {
        Factura factura = facturaDAO.obtenerFacturaPorId(idFactura);
        if (factura == null) {
            return 0;
        }
        double pagado = pagoDAO.totalPagadoPorFactura(idFactura);
        double saldo = factura.getTotal() - pagado;
        return saldo < 0 ? 0 : saldo;
    }

    /** Indica si la factura ya quedo totalmente cancelada. */
    public boolean facturaEstaSaldada(int idFactura) {
        return calcularSaldoPendiente(idFactura) <= 0;
    }
}
