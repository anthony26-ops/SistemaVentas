-- =====================================================================
--  Sistema de Ventas - Base de datos MySQL (esquema + usuario admin + datos de ejemplo)
-- =====================================================================
--  NO ES OBLIGATORIO USARLO: el programa crea la base y las tablas solo al arrancar.
--  Este script es la alternativa manual y deja EXACTAMENTE lo mismo.
--
--  Como ejecutarlo (MySQL 8):
--     mysql -uroot -p < database/sistema_ventas.sql        (con contrasena)
--     mysql -uroot    < database/sistema_ventas.sql        (si root no tiene contrasena)
--  o en MySQL Workbench: File > Open SQL Script > ejecutar todo (rayo).
--
--  Es seguro correrlo varias veces: usa CREATE ... IF NOT EXISTS e INSERT IGNORE.
--  Si la base quedo a medias de una version vieja, primero:
--     DROP DATABASE IF EXISTS sistema_ventas;
--
--  Parte 1: base y tablas (en orden de dependencia de las llaves foraneas)
--  Parte 2: usuario administrador (admin / admin123)
--  Parte 3: datos de ejemplo (OPCIONAL: borrela si quiere la base vacia)
-- =====================================================================

CREATE DATABASE IF NOT EXISTS sistema_ventas;
USE sistema_ventas;

-- =====================================================================
--  PARTE 1: TABLAS
-- =====================================================================

-- Personas (base comun; hoy solo se usa para el administrador)
CREATE TABLE IF NOT EXISTS `personas` (
  `id_persona` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `apellido` varchar(100) NOT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id_persona`)
) ENGINE=InnoDB;

-- Clientes
CREATE TABLE IF NOT EXISTS `clientes` (
  `id_cliente` int NOT NULL AUTO_INCREMENT,
  `id_persona` int DEFAULT NULL,
  `nombre` varchar(100) NOT NULL,
  `apellido` varchar(100) NOT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `nit` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id_cliente`),
  KEY `fk_cliente_persona` (`id_persona`),
  CONSTRAINT `fk_cliente_persona` FOREIGN KEY (`id_persona`) REFERENCES `personas` (`id_persona`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Empleados y vendedores (tipo_empleado = GENERAL | VENDEDOR)
CREATE TABLE IF NOT EXISTS `empleados` (
  `id_empleado` int NOT NULL AUTO_INCREMENT,
  `id_persona` int DEFAULT NULL,
  `nombre` varchar(100) NOT NULL,
  `apellido` varchar(100) NOT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `puesto` varchar(50) NOT NULL,
  `salario` decimal(10,2) NOT NULL,
  `tipo_empleado` varchar(20) NOT NULL DEFAULT 'GENERAL',
  `zona_venta` varchar(100) DEFAULT NULL,
  `meta_ventas_mensual` decimal(12,2) DEFAULT NULL,
  PRIMARY KEY (`id_empleado`),
  KEY `fk_empleado_persona` (`id_persona`),
  CONSTRAINT `fk_empleado_persona` FOREIGN KEY (`id_persona`) REFERENCES `personas` (`id_persona`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Categorias de producto
CREATE TABLE IF NOT EXISTS `ventas_categorias` (
  `id_categoria` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `descripcion` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id_categoria`)
) ENGINE=InnoDB;

-- Proveedores
CREATE TABLE IF NOT EXISTS `ventas_proveedores` (
  `id_proveedor` int NOT NULL AUTO_INCREMENT,
  `nombre_empresa` varchar(150) NOT NULL,
  `telefono` varchar(50) DEFAULT NULL,
  `direccion` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id_proveedor`)
) ENGINE=InnoDB;

-- Productos (existencia = total cacheado; el detalle por bodega esta en ventas_inventario)
CREATE TABLE IF NOT EXISTS `ventas_productos` (
  `id_producto` int NOT NULL,
  `nombre` varchar(120) NOT NULL,
  `descripcion` varchar(200) DEFAULT NULL,
  `precio` decimal(12,2) NOT NULL,
  `existencia` int NOT NULL,
  `categoria_id` int DEFAULT NULL,
  `proveedor_id` int DEFAULT NULL,
  PRIMARY KEY (`id_producto`),
  KEY `fk_producto_categoria` (`categoria_id`),
  KEY `fk_producto_proveedor` (`proveedor_id`),
  CONSTRAINT `fk_producto_categoria` FOREIGN KEY (`categoria_id`) REFERENCES `ventas_categorias` (`id_categoria`),
  CONSTRAINT `fk_producto_proveedor` FOREIGN KEY (`proveedor_id`) REFERENCES `ventas_proveedores` (`id_proveedor`)
) ENGINE=InnoDB;

-- Bodegas
CREATE TABLE IF NOT EXISTS `ventas_bodegas` (
  `id_bodega` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `direccion` varchar(200) DEFAULT NULL,
  `telefono` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id_bodega`)
) ENGINE=InnoDB;

-- Inventario por bodega (UNIQUE producto + bodega)
CREATE TABLE IF NOT EXISTS `ventas_inventario` (
  `id_inventario` int NOT NULL AUTO_INCREMENT,
  `producto_id` int NOT NULL,
  `bodega_id` int NOT NULL,
  `cantidad_disponible` int NOT NULL,
  PRIMARY KEY (`id_inventario`),
  UNIQUE KEY `uq_inventario_producto_bodega` (`producto_id`,`bodega_id`),
  KEY `fk_inventario_bodega` (`bodega_id`),
  CONSTRAINT `fk_inventario_bodega` FOREIGN KEY (`bodega_id`) REFERENCES `ventas_bodegas` (`id_bodega`),
  CONSTRAINT `fk_inventario_producto` FOREIGN KEY (`producto_id`) REFERENCES `ventas_productos` (`id_producto`)
) ENGINE=InnoDB;

-- Ordenes de venta (estado: Pendiente -> Despachada -> Facturada)
CREATE TABLE IF NOT EXISTS `ordenes_venta` (
  `id` int NOT NULL AUTO_INCREMENT,
  `fecha` date NOT NULL,
  `id_cliente` int NOT NULL,
  `id_empleado` int NOT NULL,
  `total` decimal(12,2) NOT NULL,
  `estado` varchar(30) NOT NULL DEFAULT 'Pendiente',
  PRIMARY KEY (`id`),
  KEY `fk_ordenes_venta_cliente` (`id_cliente`),
  KEY `fk_ordenes_venta_empleado` (`id_empleado`),
  CONSTRAINT `fk_ordenes_venta_cliente` FOREIGN KEY (`id_cliente`) REFERENCES `clientes` (`id_cliente`),
  CONSTRAINT `fk_ordenes_venta_empleado` FOREIGN KEY (`id_empleado`) REFERENCES `empleados` (`id_empleado`)
) ENGINE=InnoDB;

-- Detalle de las ordenes de venta
CREATE TABLE IF NOT EXISTS `orden_venta_detalles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `id_orden_venta` int NOT NULL,
  `id_producto` int NOT NULL,
  `cantidad` int NOT NULL,
  `precio_unitario` decimal(12,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_ov_detalles_orden` (`id_orden_venta`),
  KEY `fk_ov_detalles_producto` (`id_producto`),
  CONSTRAINT `fk_ov_detalles_orden` FOREIGN KEY (`id_orden_venta`) REFERENCES `ordenes_venta` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ov_detalles_producto` FOREIGN KEY (`id_producto`) REFERENCES `ventas_productos` (`id_producto`)
) ENGINE=InnoDB;

-- Facturas (nacen de una orden ya despachada)
CREATE TABLE IF NOT EXISTS `facturas` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_orden_venta` int DEFAULT NULL,
  `fecha` date NOT NULL,
  `id_cliente` int NOT NULL,
  `id_empleado` int NOT NULL,
  `total` decimal(12,2) NOT NULL,
  `estado` varchar(30) NOT NULL DEFAULT 'Pendiente',
  PRIMARY KEY (`id`),
  KEY `fk_facturas_orden` (`id_orden_venta`),
  KEY `fk_facturas_cliente` (`id_cliente`),
  KEY `fk_facturas_empleado` (`id_empleado`),
  CONSTRAINT `fk_facturas_cliente` FOREIGN KEY (`id_cliente`) REFERENCES `clientes` (`id_cliente`),
  CONSTRAINT `fk_facturas_empleado` FOREIGN KEY (`id_empleado`) REFERENCES `empleados` (`id_empleado`),
  CONSTRAINT `fk_facturas_orden` FOREIGN KEY (`id_orden_venta`) REFERENCES `ordenes_venta` (`id`)
) ENGINE=InnoDB;

-- Detalle de las facturas
CREATE TABLE IF NOT EXISTS `factura_detalles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `id_factura` int NOT NULL,
  `id_producto` int NOT NULL,
  `cantidad` int NOT NULL,
  `precio_unitario` decimal(12,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_fact_detalles_factura` (`id_factura`),
  KEY `fk_fact_detalles_producto` (`id_producto`),
  CONSTRAINT `fk_fact_detalles_factura` FOREIGN KEY (`id_factura`) REFERENCES `facturas` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_fact_detalles_producto` FOREIGN KEY (`id_producto`) REFERENCES `ventas_productos` (`id_producto`)
) ENGINE=InnoDB;

-- Pagos (estado: COMPLETADO | ANULADO)
CREATE TABLE IF NOT EXISTS `pagos` (
  `id_pago` int NOT NULL AUTO_INCREMENT,
  `id_factura` int NOT NULL,
  `fecha` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `monto` decimal(10,2) NOT NULL,
  `tipo_pago` varchar(20) NOT NULL,
  `referencia` varchar(100) DEFAULT NULL,
  `estado` varchar(20) NOT NULL DEFAULT 'COMPLETADO',
  PRIMARY KEY (`id_pago`),
  KEY `fk_pagos_factura` (`id_factura`),
  CONSTRAINT `fk_pagos_factura` FOREIGN KEY (`id_factura`) REFERENCES `facturas` (`id`),
  CONSTRAINT `pagos_chk_1` CHECK ((`monto` > 0))
) ENGINE=InnoDB;

-- Ordenes de despacho (salida de inventario de una bodega)
CREATE TABLE IF NOT EXISTS `ordenes_despacho` (
  `id_orden_despacho` int NOT NULL AUTO_INCREMENT,
  `id_orden_venta` int NOT NULL,
  `bodega_id` int DEFAULT NULL,
  `fecha_despacho` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `direccion_entrega` varchar(200) NOT NULL,
  `transportista` varchar(100) DEFAULT NULL,
  `estado` varchar(20) NOT NULL DEFAULT 'PENDIENTE',
  PRIMARY KEY (`id_orden_despacho`),
  KEY `fk_ordenes_despacho_orden` (`id_orden_venta`),
  KEY `fk_ordenes_despacho_bodega` (`bodega_id`),
  CONSTRAINT `fk_ordenes_despacho_bodega` FOREIGN KEY (`bodega_id`) REFERENCES `ventas_bodegas` (`id_bodega`),
  CONSTRAINT `fk_ordenes_despacho_orden` FOREIGN KEY (`id_orden_venta`) REFERENCES `ordenes_venta` (`id`)
) ENGINE=InnoDB;

-- Detalle de las ordenes de despacho
CREATE TABLE IF NOT EXISTS `detalle_orden_despacho` (
  `id_detalle_despacho` int NOT NULL AUTO_INCREMENT,
  `id_orden_despacho` int NOT NULL,
  `id_producto` int NOT NULL,
  `nombre_producto` varchar(100) NOT NULL,
  `cantidad` int NOT NULL,
  PRIMARY KEY (`id_detalle_despacho`),
  KEY `fk_det_despacho_orden` (`id_orden_despacho`),
  CONSTRAINT `fk_det_despacho_orden` FOREIGN KEY (`id_orden_despacho`) REFERENCES `ordenes_despacho` (`id_orden_despacho`) ON DELETE CASCADE,
  CONSTRAINT `detalle_orden_despacho_chk_1` CHECK ((`cantidad` > 0))
) ENGINE=InnoDB;

-- Usuarios de login (solo se usa si REQUIERE_LOGIN = true)
CREATE TABLE IF NOT EXISTS `ventas_usuarios` (
  `id_usuario` int NOT NULL,
  `empleado_id` int NOT NULL,
  `nombre_usuario` varchar(50) NOT NULL,
  `contrasena_hash` varchar(255) NOT NULL,
  `rol` varchar(30) NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `empleado_id` (`empleado_id`),
  UNIQUE KEY `nombre_usuario` (`nombre_usuario`),
  CONSTRAINT `fk_usuario_empleado` FOREIGN KEY (`empleado_id`) REFERENCES `empleados` (`id_empleado`)
) ENGINE=InnoDB;

-- =====================================================================
--  PARTE 2: USUARIO ADMINISTRADOR  (usuario: admin  |  contrasena: admin123)
--  La contrasena se guarda como hash BCrypt, nunca en texto plano.
-- =====================================================================
INSERT IGNORE INTO personas (id_persona, nombre, apellido, telefono, email)
VALUES (1, 'Admin', 'Sistema', '00000000', 'admin@sistemaventas.com');

INSERT IGNORE INTO empleados (id_empleado, id_persona, nombre, apellido, telefono, email, puesto, salario)
VALUES (1, 1, 'Admin', 'Sistema', '00000000', 'admin@sistemaventas.com', 'Administrador', 0);

INSERT IGNORE INTO ventas_usuarios (id_usuario, empleado_id, nombre_usuario, contrasena_hash, rol, activo)
VALUES (1, 1, 'admin', '$2a$10$TPCXyARwvD9jJr9vW4wJ9O6L1Y/1mhkLJcsehR3I16GRZFoeiSbka', 'ADMIN', TRUE);

-- =====================================================================
--  PARTE 3: DATOS DE EJEMPLO (opcional) - los mismos que carga el programa
--  en una instalacion nueva: 1 bodega, 1 categoria, 1 proveedor, 3 productos
--  con stock, 2 clientes y 1 vendedor.
-- =====================================================================
INSERT IGNORE INTO ventas_bodegas (id_bodega, nombre, direccion, telefono)
VALUES (1, 'Bodega Principal', 'Sin direccion registrada', '');

INSERT IGNORE INTO ventas_categorias (id_categoria, nombre, descripcion)
VALUES (1, 'General (ejemplo)', 'Categoria de ejemplo');

INSERT IGNORE INTO ventas_proveedores (id_proveedor, nombre_empresa, telefono, direccion)
VALUES (1, 'Proveedor de ejemplo', '22223333', 'Ciudad de Guatemala');

INSERT IGNORE INTO ventas_productos (id_producto, nombre, descripcion, precio, existencia, categoria_id, proveedor_id) VALUES
 (1001, 'Producto de ejemplo A', 'Producto de ejemplo',  25.00, 50, 1, 1),
 (1002, 'Producto de ejemplo B', 'Producto de ejemplo',  40.00, 30, 1, 1),
 (1003, 'Producto de ejemplo C', 'Producto de ejemplo', 100.00, 20, 1, 1);

INSERT IGNORE INTO ventas_inventario (producto_id, bodega_id, cantidad_disponible) VALUES
 (1001, 1, 50), (1002, 1, 30), (1003, 1, 20);

INSERT IGNORE INTO clientes (id_cliente, nombre, apellido, telefono, email, nit) VALUES
 (1, 'Cliente', 'Ejemplo Uno', '55551111', 'cliente1@ejemplo.com', 'CF'),
 (2, 'Cliente', 'Ejemplo Dos', '55552222', 'cliente2@ejemplo.com', '1234567-8');

INSERT IGNORE INTO empleados (id_empleado, nombre, apellido, telefono, email, puesto, salario, tipo_empleado, zona_venta, meta_ventas_mensual)
VALUES (2, 'Vendedor', 'De ejemplo', '55550000', 'vendedor@ejemplo.com', 'Vendedor', 3000, 'VENDEDOR', 'Zona 1', 10000);
