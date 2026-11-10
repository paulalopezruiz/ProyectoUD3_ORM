# Memoria del proyecto: Gestión de proyectos

## 1. Introducción

Esta aplicación permite gestionar proveedores, piezas, proyectos y los suministros
realizados entre ellos. Se ha desarrollado en Java usando Hibernate/JPA y una
base de datos MariaDB. La aplicación se puede utilizar desde la consola y
también incluye una pequeña interfaz web local.

## 2. Requisitos y puesta en marcha

### Requisitos

- Java 17.
- Maven.
- MariaDB accesible desde el ordenador.
- IntelliJ IDEA (recomendado, aunque no es obligatorio).

### Configuración de la base de datos

La aplicación utiliza por defecto:

- URL: `jdbc:mariadb://localhost:33061/proyecto_ud3`
- Usuario: `root`

La contraseña no está escrita en el código. Hay que configurar la variable de
entorno `DB_PASSWORD`. También se pueden cambiar la URL y el usuario mediante
`DB_URL` y `DB_USERNAME`.

Hibernate utiliza `hibernate.hbm2ddl.auto=update`, por lo que crea o actualiza
las tablas a partir de las entidades. No es necesario ejecutar un script SQL
adicional.

### Ejecución

Desde IntelliJ se puede ejecutar la clase `org.example.App`. Al iniciar por
primera vez se solicita crear un usuario. En las siguientes ejecuciones se
solicitan sus credenciales. La contraseña se guarda mediante un hash, no en
texto plano.

Para ejecutar las pruebas se puede usar la configuración de tests de IntelliJ
o el comando:

```text
mvn test
```

Las pruebas DAO usan H2 en memoria para no depender de una base de datos
externa.

## 3. Funcionamiento y funcionalidades

Después de iniciar sesión se muestra el menú principal:

1. **Proveedores**: alta, baja, modificación, listado y búsquedas por código,
   nombre, apellidos, dirección y pieza suministrada.
2. **Piezas**: alta, baja, modificación, listado y búsquedas por código,
   nombre, precio, proveedor y proyecto.
3. **Proyectos**: alta, baja, modificación, listado y búsquedas por código,
   nombre, ciudad, proveedor y pieza.
4. **Suministros**: alta, baja, modificación, listado y búsquedas por
   proveedor, pieza o proyecto.
5. **Estadísticas**: históricos de proveedores, análisis de proyectos y
   estadísticas generales.
6. **Exportaciones**: exportación de datos e informes a JSON y XML.
7. **Ayuda**: explicación breve de las opciones.

Los menús permiten volver al menú anterior o salir. Las entradas numéricas,
fechas, precios, cantidades y códigos se validan antes de acceder a la base
de datos. Los errores se muestran con mensajes comprensibles y no deberían
cerrar la aplicación de forma inesperada.

También se inicia una interfaz web local para consultar y gestionar parte de
la aplicación. Si no pudiera iniciarse, la aplicación continúa disponible
desde la consola.

## 4. Modelo entidad-relación

Las entidades principales son:

```text
PROVEEDOR
- codigo (PK)
- nombre
- apellidos
- direccionPostal

PIEZA
- codigo (PK)
- nombre
- precio
- descripcion

PROYECTO
- codigo (PK)
- nombre
- ciudad
- estado

GESTION
- id (PK, autogenerado)
- fecha
- cantidad
- proveedor_codigo (FK)
- pieza_codigo (FK)
- proyecto_codigo (FK)
```

Relaciones:

```text
PROVEEDOR 1 -------- N GESTION N -------- 1 PIEZA
                         |
                         N
                         |
                         1
                      PROYECTO
```

Una gestión representa un suministro concreto. Tiene una fecha, una cantidad
y referencia exactamente a un proveedor, una pieza y un proyecto. Además,
existen las entidades `Usuario`, `RegistroAcceso` y `RegistroAuditoria` para
la seguridad y el control de operaciones.

## 5. Uso de Hibernate

Las clases del modelo están anotadas con JPA:

- `@Entity` define las entidades.
- `@Id` marca las claves primarias.
- `@GeneratedValue` genera automáticamente el identificador de `Gestion`.
- `@ManyToOne` relaciona cada gestión con su proveedor, pieza y proyecto.
- `@OneToMany(mappedBy=...)` representa la colección de gestiones de cada
  proveedor, pieza y proyecto.
- `@JoinColumn` define las claves externas.

Los DAO abren un `EntityManager` y realizan las operaciones dentro de
transacciones. Las operaciones de alta, modificación y baja se centralizan
en la clase `Dao`, que también registra la auditoría.

## 6. Consultas JPQL principales

Algunos ejemplos utilizados son:

```sql
SELECT p FROM Proveedor p
WHERE LOWER(p.nombre) LIKE LOWER(:nombre)
```

```sql
SELECT DISTINCT p FROM Pieza p
JOIN p.gestiones g
WHERE g.proveedor.codigo = :codigo
```

```sql
SELECT g FROM Gestion g
WHERE g.proyecto.codigo = :codigo
ORDER BY g.proveedor.codigo, g.pieza.codigo
```

```sql
SELECT SUM(g.cantidad * g.pieza.precio)
FROM Gestion g
WHERE g.proyecto.codigo = :codigo
```

También se utilizan consultas con `COUNT`, `SUM`, `DISTINCT`, agrupaciones y
filtros por fechas para los históricos y las estadísticas.

## 7. Integridad referencial y errores

Las relaciones de `Gestion` son obligatorias (`optional=false`) y sus columnas
no aceptan valores nulos. Antes de borrar un proveedor, una pieza o un
proyecto se comprueba que no tenga suministros asociados. De esta forma no se
rompen las relaciones existentes.

La entrada por consola se valida con la clase `Validaciones`. Se controlan
opciones inexistentes, números incorrectos, fechas no válidas, precios
negativos, cantidades no positivas y códigos con formato incorrecto.

## 8. Mejoras implementadas

- Pruebas automatizadas con JUnit para validaciones, seguridad, DAO y
  estadísticas.
- Registro de eventos y errores mediante SLF4J en `logs/aplicacion.log`.
- Login con un máximo de tres intentos.
- Contraseñas protegidas mediante hash y salt aleatorio.
- Registro de accesos y cierres de sesión.
- Registro de altas, modificaciones y bajas.
- Búsquedas avanzadas.
- Estadísticas de proveedores, piezas, proyectos y suministros.
- Exportación a JSON y XML.
- Interfaz web local complementaria a los menús de consola.

## 9. Decisiones tomadas

Se ha utilizado un DAO genérico para evitar repetir la lógica de
transacciones y auditoría. Las entidades mantienen las relaciones necesarias
para poder hacer consultas JPQL sobre los suministros. Se ha elegido
`LocalDate` para las fechas de suministro y `LocalDateTime` para los accesos
y la auditoría.

Para las pruebas se utiliza H2 en memoria. Así se pueden ejecutar sin
modificar los datos de MariaDB ni depender de que el servidor esté iniciado.
