# Proyecto UD3 ORM

Aplicación Java para gestionar proveedores, piezas, proyectos y suministros
utilizando Hibernate/JPA y MariaDB.

## Requisitos

- Java 17.
- Maven.
- MariaDB.
- IntelliJ IDEA (opcional, pero recomendado).

## Preparar MariaDB

Ejecuta el script `sql/01_crear_base_datos.sql` o crea una base de datos
llamada `proyecto_ud3`. El script solo crea la base de datos; Hibernate crea
las tablas automáticamente al arrancar.

La configuración por defecto es:

```text
URL:      jdbc:mariadb://localhost:33061/proyecto_ud3
Usuario:  root
```

Si tu MariaDB utiliza otro puerto, usuario o base de datos, configura
`DB_URL` y `DB_USERNAME`.

## Configurar la contraseña

No se incluye ninguna contraseña en el repositorio. Antes de ejecutar la
aplicación, configura la variable de entorno `DB_PASSWORD`.

En IntelliJ:

1. Abre `Run > Edit Configurations`.
2. Selecciona la configuración de `App`.
3. En `Environment variables`, añade `DB_PASSWORD=tu_contraseña`.
4. Guarda y ejecuta la aplicación.

También se puede hacer temporalmente desde PowerShell:

```powershell
$env:DB_PASSWORD="tu_contraseña"
```

## Ejecutar desde IntelliJ

Abre el archivo `pom.xml` como proyecto Maven y espera a que se descarguen
las dependencias. Después ejecuta la clase:

```text
src/main/java/org/example/App.java
```

La primera vez se crea el usuario de acceso. En las siguientes ejecuciones se
solicitan sus credenciales.

La aplicación ofrece menús de consola y una interfaz web local. Si la interfaz
web no puede iniciarse, la aplicación continúa funcionando mediante la
consola.

## Ejecutar desde terminal

```text
mvn clean compile
mvn exec:java -Dexec.mainClass=org.example.App
```

Si no está disponible el plugin `exec`, se puede ejecutar desde IntelliJ.

## Ejecutar las pruebas

```text
mvn test
```

Las pruebas DAO usan H2 en memoria, por lo que no necesitan MariaDB ni
`DB_PASSWORD`.

## Dependencias

Las dependencias están declaradas en `pom.xml`. Entre ellas se encuentran:

- Hibernate ORM y Jakarta Persistence.
- MariaDB JDBC.
- Jackson para JSON y XML.
- SLF4J para logs.
- JUnit 5 y H2 para pruebas.

No se debe entregar la carpeta `target`, porque Maven la genera al compilar.
