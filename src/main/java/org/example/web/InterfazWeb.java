package org.example.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.example.dao.GestionDao;
import org.example.dao.PiezaDao;
import org.example.dao.ProveedorDao;
import org.example.dao.ProyectoDao;
import org.example.models.*;
import org.example.utils.*;

import java.awt.Desktop;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public final class InterfazWeb implements AutoCloseable {

    private static final String COOKIE = "PROYECTO_SESSION";
    private static final long SESSION_LIFETIME_MILLIS = 8 * 60 * 60 * 1000L;
    private static final long LOCKOUT_MILLIS = 60 * 1000L;
    private static final int MAX_LOGIN_FAILURES = 5;

    private final Map<String, SesionWeb> sesiones = new ConcurrentHashMap<>();
    private final Map<String, IntentosAcceso> intentos = new ConcurrentHashMap<>();

    private final ServicioAutenticacion autenticacion =
            new ServicioAutenticacion(EntradaConsola.scanner());

    private HttpServer server;

    public void iniciar() throws IOException {

        int puerto = Integer.parseInt(
                System.getenv().getOrDefault("APP_WEB_PORT", "8080")
        );

        server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", puerto),
                0
        );

        server.createContext("/", this::manejar);

        server.setExecutor(
                Executors.newCachedThreadPool(runnable -> {

                    Thread thread =
                            new Thread(runnable, "interfaz-web");

                    thread.setDaemon(true);

                    return thread;
                })
        );

        server.start();

        String url =
                "http://127.0.0.1:"
                        + server.getAddress().getPort()
                        + "/";

        System.out.println(
                "Interfaz web disponible en " + url
        );

        AppLogger.info(
                "Interfaz web iniciada en " + url
        );

        if (Desktop.isDesktopSupported()) {

            try {

                Desktop.getDesktop().browse(
                        URI.create(url)
                );

            } catch (
                    IOException |
                    UnsupportedOperationException e
            ) {

                AppLogger.warn(
                        "No se pudo abrir el navegador automáticamente: "
                                + e.getMessage()
                );
            }
        }
    }

    @Override
    public void close() {

        if (server != null) {
            server.stop(1);
        }

        for (SesionWeb sesion : sesiones.values()) {
            cerrarRegistro(sesion);
        }

        sesiones.clear();
    }

    private void manejar(
            HttpExchange exchange
    ) throws IOException {

        try {

            limpiarSesionesCaducadas();

            String ruta =
                    exchange.getRequestURI().getPath();

            if (
                    "/style.css".equals(ruta)
                            &&
                            "GET".equals(
                                    exchange.getRequestMethod()
                            )
            ) {

                responderRecurso(
                        exchange,
                        "/web/style.css",
                        "text/css; charset=utf-8"
                );

                return;
            }

            if (
                    "/app.js".equals(ruta)
                            &&
                            "GET".equals(
                                    exchange.getRequestMethod()
                            )
            ) {

                responderRecurso(
                        exchange,
                        "/web/app.js",
                        "text/javascript; charset=utf-8"
                );

                return;
            }

            String metodo =
                    exchange.getRequestMethod();

            if ("POST".equals(metodo)) {

                manejarPost(
                        exchange,
                        ruta
                );

                return;
            }

            if (!"GET".equals(metodo)) {

                responder(
                        exchange,
                        405,
                        pagina(
                                "Método no permitido",
                                "<section class='panel'>"
                                        + "<h1>Método no permitido</h1>"
                                        + "</section>"
                        )
                );

                return;
            }

            manejarGet(
                    exchange,
                    ruta
            );

        } catch (IllegalArgumentException e) {

            AppLogger.warn(
                    "Datos no válidos en una petición web: "
                            + e.getMessage()
            );

            SesionWeb sesion =
                    obtenerSesion(exchange);

            String contenido =
                    "<section class='panel'>"
                            + "<h1>No se pudo completar la operación</h1>"
                            + "<p>"
                            + esc(e.getMessage())
                            + "</p>"
                            + "<a class='button' href='/dashboard'>"
                            + "Volver"
                            + "</a>"
                            + "</section>";

            String paginaError =
                    sesion == null
                            ? pagina(
                            "Datos no válidos",
                            contenido
                    )
                            : pagina(
                            "Datos no válidos",
                            contenido,
                            sesion.usuario,
                            sesion.token
                    );

            responder(
                    exchange,
                    400,
                    paginaError
            );

        } catch (Exception e) {

            AppLogger.error(
                    "Error procesando una petición de la interfaz web.",
                    e
            );

            responder(
                    exchange,
                    500,
                    pagina(
                            "Error",
                            "<section class='panel'>"
                                    + "<h1>No se pudo completar la operación</h1>"
                                    + "<p>Revisa los datos e inténtalo de nuevo.</p>"
                                    + "<a class='button' href='/dashboard'>"
                                    + "Volver"
                                    + "</a>"
                                    + "</section>"
                    )
            );

        } finally {

            ContextoSeguridad.cerrar();

            exchange.close();
        }
    }

    private void manejarGet(
            HttpExchange exchange,
            String ruta
    ) throws IOException {

        SesionWeb sesion =
                obtenerSesion(exchange);

        if (
                "/".equals(ruta)
                        ||
                        "/login".equals(ruta)
        ) {

            responder(
                    exchange,
                    200,
                    sesion == null
                            ? paginaLogin("")
                            : redireccion()
            );

            return;
        }

        if (sesion == null) {

            responder(
                    exchange,
                    401,
                    paginaLogin(
                            "Inicia sesión para continuar."
                    )
            );

            return;
        }

        ContextoSeguridad.iniciar(
                sesion.usuario
        );

        String contenido =
                switch (ruta) {

                    case "/dashboard" ->
                            panelPrincipal();

                    case "/proveedores" ->
                            paginaProveedores();

                    case "/piezas" ->
                            paginaPiezas();

                    case "/proyectos" ->
                            paginaProyectos();

                    case "/suministros" ->
                            paginaSuministros();

                    default ->
                            "<section class='panel'>"
                                    + "<h1>Página no encontrada</h1>"
                                    + "<a href='/dashboard'>Volver</a>"
                                    + "</section>";
                };

        responder(
                exchange,
                "/dashboard".equals(ruta)
                        ||
                        Set.of(
                                "/proveedores",
                                "/piezas",
                                "/proyectos",
                                "/suministros"
                        ).contains(ruta)
                        ? 200
                        : 404,
                pagina(
                        "Gestión de Proyectos",
                        contenido,
                        sesion.usuario,
                        sesion.token
                )
        );
    }

    private void manejarPost(
            HttpExchange exchange,
            String ruta
    ) throws IOException {

        Map<String, String> parametros =
                leerParametros(exchange);

        if ("/login".equals(ruta)) {

            iniciarSesion(
                    exchange,
                    parametros
            );

            return;
        }

        SesionWeb sesion =
                obtenerSesion(exchange);

        if (sesion == null) {

            responder(
                    exchange,
                    401,
                    paginaLogin(
                            "La sesión ha caducado. Vuelve a iniciar sesión."
                    )
            );

            return;
        }

        if (
                !Objects.equals(
                        sesion.token,
                        parametros.get("csrf")
                )
        ) {

            responder(
                    exchange,
                    403,
                    pagina(
                            "Solicitud rechazada",
                            "<section class='panel'>"
                                    + "<h1>Solicitud no válida</h1>"
                                    + "<p>Actualiza la página e inténtalo de nuevo.</p>"
                                    + "<a class='button' href='/dashboard'>"
                                    + "Volver"
                                    + "</a>"
                                    + "</section>"
                    )
            );

            return;
        }

        ContextoSeguridad.iniciar(
                sesion.usuario
        );

        if ("/logout".equals(ruta)) {

            sesiones.remove(
                    sesion.token
            );

            cerrarRegistro(
                    sesion
            );

            exchange.getResponseHeaders()
                    .add(
                            "Set-Cookie",
                            COOKIE
                                    + "=; Path=/; HttpOnly; SameSite=Strict; Max-Age=0"
                    );

            redirigir(
                    exchange,
                    "/"
            );

            return;
        }

        ejecutarAccion(
                ruta,
                parametros
        );

        redirigir(
                exchange,
                rutaBase(ruta)
        );
    }

    private void iniciarSesion(
            HttpExchange exchange,
            Map<String, String> parametros
    ) throws IOException {

        String origen =
                exchange
                        .getRemoteAddress()
                        .getAddress()
                        .getHostAddress();

        IntentosAcceso intentosAcceso =
                intentos.computeIfAbsent(
                        origen,
                        ignorado ->
                                new IntentosAcceso()
                );

        long ahora =
                System.currentTimeMillis();

        if (
                intentosAcceso.bloqueadoHasta
                        >
                        ahora
        ) {

            responder(
                    exchange,
                    429,
                    paginaLogin(
                            "Demasiados intentos. Espera un minuto antes de volver a probar."
                    )
            );

            return;
        }

        String usuario =
                parametros
                        .getOrDefault(
                                "usuario",
                                ""
                        )
                        .trim();

        char[] contrasena =
                parametros
                        .getOrDefault(
                                "contrasena",
                                ""
                        )
                        .toCharArray();

        SesionAutenticada autenticada =
                autenticacion.autenticar(
                        usuario,
                        contrasena
                );

        if (autenticada == null) {

            intentosAcceso.fallos++;

            if (
                    intentosAcceso.fallos
                            >=
                            MAX_LOGIN_FAILURES
            ) {

                intentosAcceso.fallos = 0;

                intentosAcceso.bloqueadoHasta =
                        ahora
                                +
                                LOCKOUT_MILLIS;
            }

            responder(
                    exchange,
                    401,
                    paginaLogin(
                            "Usuario o contraseña incorrectos."
                    )
            );

            return;
        }

        intentos.remove(
                origen
        );

        String token =
                UUID.randomUUID().toString();

        sesiones.put(
                token,
                new SesionWeb(
                        token,
                        autenticada.nombreUsuario(),
                        autenticada.idRegistroAcceso(),
                        ahora
                )
        );

        exchange
                .getResponseHeaders()
                .add(
                        "Set-Cookie",
                        COOKIE
                                + "="
                                + token
                                + "; Path=/; HttpOnly; SameSite=Strict; Max-Age=28800"
                );

        redirigir(
                exchange,
                "/dashboard"
        );
    }

    private void ejecutarAccion(
            String ruta,
            Map<String, String> p
    ) {

        switch (ruta) {

            case "/proveedores/crear" -> {

                try (
                        ProveedorDao dao =
                                new ProveedorDao()
                ) {

                    dao.create(
                            new Proveedor(
                                    codigo(
                                            p,
                                            "codigo"
                                    ),
                                    obligatorio(
                                            p,
                                            "nombre"
                                    ),
                                    obligatorio(
                                            p,
                                            "apellidos"
                                    ),
                                    obligatorio(
                                            p,
                                            "direccion"
                                    )
                            )
                    );
                }
            }

            case "/proveedores/editar" -> {

                try (
                        ProveedorDao dao =
                                new ProveedorDao()
                ) {

                    Proveedor item =
                            requerido(
                                    dao.find(
                                            codigo(
                                                    p,
                                                    "codigo"
                                            )
                                    ),
                                    "Proveedor no encontrado."
                            );

                    String anterior =
                            item.toString();

                    item.setNombre(
                            obligatorio(
                                    p,
                                    "nombre"
                            )
                    );

                    item.setApellidos(
                            obligatorio(
                                    p,
                                    "apellidos"
                            )
                    );

                    item.setDireccionPostal(
                            obligatorio(
                                    p,
                                    "direccion"
                            )
                    );

                    dao.update(
                            item,
                            anterior
                    );
                }
            }

            case "/proveedores/eliminar" -> {

                try (
                        ProveedorDao dao =
                                new ProveedorDao()
                ) {

                    dao.delete(
                            requerido(
                                    dao.find(
                                            codigo(
                                                    p,
                                                    "codigo"
                                            )
                                    ),
                                    "Proveedor no encontrado."
                            )
                    );
                }
            }

            case "/piezas/crear" -> {

                try (
                        PiezaDao dao =
                                new PiezaDao()
                ) {

                    dao.create(
                            new Pieza(
                                    codigo(
                                            p,
                                            "codigo"
                                    ),
                                    obligatorio(
                                            p,
                                            "nombre"
                                    ),
                                    precio(
                                            p.get(
                                                    "precio"
                                            )
                                    ),
                                    p.getOrDefault(
                                            "descripcion",
                                            ""
                                    ).trim()
                            )
                    );
                }
            }

            case "/piezas/editar" -> {

                try (
                        PiezaDao dao =
                                new PiezaDao()
                ) {

                    Pieza item =
                            requerido(
                                    dao.find(
                                            codigo(
                                                    p,
                                                    "codigo"
                                            )
                                    ),
                                    "Pieza no encontrada."
                            );

                    String anterior =
                            item.toString();

                    item.setNombre(
                            obligatorio(
                                    p,
                                    "nombre"
                            )
                    );

                    item.setPrecio(
                            precio(
                                    p.get(
                                            "precio"
                                    )
                            )
                    );

                    item.setDescripcion(
                            p.getOrDefault(
                                    "descripcion",
                                    ""
                            ).trim()
                    );

                    dao.update(
                            item,
                            anterior
                    );
                }
            }

            case "/piezas/eliminar" -> {

                try (
                        PiezaDao dao =
                                new PiezaDao()
                ) {

                    dao.delete(
                            requerido(
                                    dao.find(
                                            codigo(
                                                    p,
                                                    "codigo"
                                            )
                                    ),
                                    "Pieza no encontrada."
                            )
                    );
                }
            }

            case "/proyectos/crear" -> {

                try (
                        ProyectoDao dao =
                                new ProyectoDao()
                ) {

                    Proyecto item =
                            new Proyecto(
                                    codigo(
                                            p,
                                            "codigo"
                                    ),
                                    obligatorio(
                                            p,
                                            "nombre"
                                    ),
                                    obligatorio(
                                            p,
                                            "ciudad"
                                    )
                            );

                    item.setEstado(
                            estado(
                                    p.get(
                                            "estado"
                                    )
                            )
                    );

                    dao.create(
                            item
                    );
                }
            }

            case "/proyectos/editar" -> {

                try (
                        ProyectoDao dao =
                                new ProyectoDao()
                ) {

                    Proyecto item =
                            requerido(
                                    dao.find(
                                            codigo(
                                                    p,
                                                    "codigo"
                                            )
                                    ),
                                    "Proyecto no encontrado."
                            );

                    String anterior =
                            item.toString();

                    item.setNombre(
                            obligatorio(
                                    p,
                                    "nombre"
                            )
                    );

                    item.setCiudad(
                            obligatorio(
                                    p,
                                    "ciudad"
                            )
                    );

                    item.setEstado(
                            estado(
                                    p.get(
                                            "estado"
                                    )
                            )
                    );

                    dao.update(
                            item,
                            anterior
                    );
                }
            }

            case "/proyectos/eliminar" -> {

                try (
                        ProyectoDao dao =
                                new ProyectoDao()
                ) {

                    dao.delete(
                            requerido(
                                    dao.find(
                                            codigo(
                                                    p,
                                                    "codigo"
                                            )
                                    ),
                                    "Proyecto no encontrado."
                            )
                    );
                }
            }

            case "/suministros/crear" ->
                    crearSuministro(p);

            case "/suministros/editar" ->
                    editarSuministro(p);

            case "/suministros/eliminar" -> {

                try (
                        GestionDao dao =
                                new GestionDao()
                ) {

                    dao.delete(
                            requerido(
                                    dao.find(
                                            entero(
                                                    p.get(
                                                            "id"
                                                    ),
                                                    "ID"
                                            )
                                    ),
                                    "Suministro no encontrado."
                            )
                    );
                }
            }

            default ->
                    throw new IllegalArgumentException(
                            "Acción no permitida."
                    );
        }
    }

    private void crearSuministro(
            Map<String, String> p
    ) {

        String proveedorCodigo =
                codigo(
                        p,
                        "proveedor"
                );

        String piezaCodigo =
                codigo(
                        p,
                        "pieza"
                );

        String proyectoCodigo =
                codigo(
                        p,
                        "proyecto"
                );

        try (
                ProveedorDao proveedores =
                        new ProveedorDao();

                PiezaDao piezas =
                        new PiezaDao();

                ProyectoDao proyectos =
                        new ProyectoDao();

                GestionDao gestiones =
                        new GestionDao()
        ) {

            Proveedor proveedor =
                    requerido(
                            proveedores.find(
                                    proveedorCodigo
                            ),
                            "Proveedor no encontrado."
                    );

            Pieza pieza =
                    requerido(
                            piezas.find(
                                    piezaCodigo
                            ),
                            "Pieza no encontrada."
                    );

            Proyecto proyecto =
                    requerido(
                            proyectos.find(
                                    proyectoCodigo
                            ),
                            "Proyecto no encontrado."
                    );

            if (
                    proyecto.getEstado()
                            ==
                            EstadoProyecto.FINALIZADO
            ) {

                throw new IllegalArgumentException(
                        "No se pueden registrar suministros en un proyecto finalizado."
                );
            }

            gestiones.create(
                    new Gestion(
                            fecha(
                                    p.get(
                                            "fecha"
                                    )
                            ),
                            entero(
                                    p.get(
                                            "cantidad"
                                    ),
                                    "Cantidad"
                            ),
                            proveedor,
                            pieza,
                            proyecto
                    )
            );
        }
    }

    private void editarSuministro(
            Map<String, String> p
    ) {

        String proyectoCodigo =
                codigo(
                        p,
                        "proyecto"
                );

        try (
                ProveedorDao proveedores =
                        new ProveedorDao();

                PiezaDao piezas =
                        new PiezaDao();

                ProyectoDao proyectos =
                        new ProyectoDao();

                GestionDao gestiones =
                        new GestionDao()
        ) {

            Gestion item =
                    requerido(
                            gestiones.find(
                                    entero(
                                            p.get(
                                                    "id"
                                            ),
                                            "ID"
                                    )
                            ),
                            "Suministro no encontrado."
                    );

            Proveedor proveedor =
                    requerido(
                            proveedores.find(
                                    codigo(
                                            p,
                                            "proveedor"
                                    )
                            ),
                            "Proveedor no encontrado."
                    );

            Pieza pieza =
                    requerido(
                            piezas.find(
                                    codigo(
                                            p,
                                            "pieza"
                                    )
                            ),
                            "Pieza no encontrada."
                    );

            Proyecto proyecto =
                    requerido(
                            proyectos.find(
                                    proyectoCodigo
                            ),
                            "Proyecto no encontrado."
                    );

            if (
                    proyecto.getEstado()
                            ==
                            EstadoProyecto.FINALIZADO
            ) {

                throw new IllegalArgumentException(
                        "No se pueden asignar suministros a un proyecto finalizado."
                );
            }

            String anterior =
                    item.toString();

            item.setProveedor(
                    proveedor
            );

            item.setPieza(
                    pieza
            );

            item.setProyecto(
                    proyecto
            );

            item.setFecha(
                    fecha(
                            p.get(
                                    "fecha"
                            )
                    )
            );

            item.setCantidad(
                    entero(
                            p.get(
                                    "cantidad"
                            ),
                            "Cantidad"
                    )
            );

            gestiones.update(
                    item,
                    anterior
            );
        }
    }

    private String panelPrincipal() {

        long proveedores;
        long piezas;
        long proyectos;
        long suministros;

        try (
                ProveedorDao p =
                        new ProveedorDao();

                PiezaDao z =
                        new PiezaDao();

                ProyectoDao r =
                        new ProyectoDao();

                GestionDao g =
                        new GestionDao()
        ) {

            proveedores =
                    p.listarTodos().size();

            piezas =
                    z.listarTodos().size();

            proyectos =
                    r.listarTodos().size();

            suministros =
                    g.listarTodos().size();
        }

        return "<section class='hero'>"
                + "<h1>Gestión de Grupo de Proyectos</h1>"
                + "<p class='lead'>Gestión de proveedores, piezas, proyectos y suministros.</p>"
                + "</section>"

                + "<section class='metrics'>"

                + "<article>"
                + "<span>PROVEEDORES</span>"
                + "<strong>"
                + proveedores
                + "</strong>"
                + "<small>Registrados</small>"
                + "</article>"

                + "<article>"
                + "<span>PIEZAS</span>"
                + "<strong>"
                + piezas
                + "</strong>"
                + "<small>Registradas</small>"
                + "</article>"

                + "<article>"
                + "<span>PROYECTOS</span>"
                + "<strong>"
                + proyectos
                + "</strong>"
                + "<small>Registrados</small>"
                + "</article>"

                + "<article>"
                + "<span>SUMINISTROS</span>"
                + "<strong>"
                + suministros
                + "</strong>"
                + "<small>Registrados</small>"
                + "</article>"

                + "</section>";
    }

    private String paginaProveedores() {

        List<Proveedor> items;

        try (
                ProveedorDao dao =
                        new ProveedorDao()
        ) {

            items =
                    dao.listarTodos();
        }

        StringBuilder contenido =
                inicioSeccion(
                        "Proveedores",
                        "Gestión de proveedores registrados."
                )
                        .append(
                                "<div class='panel'>"
                        )
                        .append(
                                "<h2>Nuevo proveedor</h2>"
                        )
                        .append(
                                "<form method='post' "
                                        + "action='/proveedores/crear' "
                                        + "class='form-grid'>"
                        )
                        .append(
                                input(
                                        "Código",
                                        "codigo",
                                        "",
                                        true
                                )
                        )
                        .append(
                                input(
                                        "Nombre",
                                        "nombre",
                                        "",
                                        true
                                )
                        )
                        .append(
                                input(
                                        "Apellidos",
                                        "apellidos",
                                        "",
                                        true
                                )
                        )
                        .append(
                                input(
                                        "Dirección",
                                        "direccion",
                                        "",
                                        true
                                )
                        )
                        .append(
                                "<button class='button' "
                                        + "type='submit'>"
                                        + "Crear proveedor"
                                        + "</button>"
                                        + "</form>"
                                        + "</div>"
                                        + "<div class='records'>"
                        );

        for (Proveedor item : items) {

            contenido
                    .append(
                            "<article class='record'>"
                    )
                    .append(
                            "<div class='record-title'>"
                    )
                    .append(
                            "<span class='eyebrow'>"
                    )
                    .append(
                            esc(
                                    item.getCodigo()
                            )
                    )
                    .append(
                            "</span>"
                                    + "<h2>"
                    )
                    .append(
                            esc(
                                    item.getNombre()
                            )
                    )
                    .append(
                            " "
                    )
                    .append(
                            esc(
                                    item.getApellidos()
                            )
                    )
                    .append(
                            "</h2>"
                                    + "<p>"
                    )
                    .append(
                            esc(
                                    item.getDireccionPostal()
                            )
                    )
                    .append(
                            "</p>"
                                    + "</div>"
                                    + "<details>"
                                    + "<summary>Editar</summary>"
                                    + "<form method='post' "
                                    + "action='/proveedores/editar' "
                                    + "class='form-grid'>"
                    )
                    .append(
                            hidden(
                                    "codigo",
                                    item.getCodigo()
                            )
                    )
                    .append(
                            input(
                                    "Nombre",
                                    "nombre",
                                    item.getNombre(),
                                    true
                            )
                    )
                    .append(
                            input(
                                    "Apellidos",
                                    "apellidos",
                                    item.getApellidos(),
                                    true
                            )
                    )
                    .append(
                            input(
                                    "Dirección",
                                    "direccion",
                                    item.getDireccionPostal(),
                                    true
                            )
                    )
                    .append(
                            "<button class='button' "
                                    + "type='submit'>"
                                    + "Guardar cambios"
                                    + "</button>"
                                    + "</form>"
                                    + "</details>"
                    )
                    .append(
                            formEliminar(
                                    "proveedores",
                                    "codigo",
                                    item.getCodigo()
                            )
                    )
                    .append(
                            "</article>"
                    );
        }

        return contenido
                .append(
                        "</div>"
                )
                .toString();
    }

    private String paginaPiezas() {

        List<Pieza> items;

        try (
                PiezaDao dao =
                        new PiezaDao()
        ) {

            items =
                    dao.listarTodos();
        }

        StringBuilder contenido =
                inicioSeccion(
                        "Piezas",
                        "Gestión de piezas registradas."
                )
                        .append(
                                "<div class='panel'>"
                                        + "<h2>Nueva pieza</h2>"
                                        + "<form method='post' "
                                        + "action='/piezas/crear' "
                                        + "class='form-grid'>"
                        )
                        .append(
                                input(
                                        "Código",
                                        "codigo",
                                        "",
                                        true
                                )
                        )
                        .append(
                                input(
                                        "Nombre",
                                        "nombre",
                                        "",
                                        true
                                )
                        )
                        .append(
                                input(
                                        "Precio",
                                        "precio",
                                        "",
                                        true,
                                        "number",
                                        "0.01"
                                )
                        )
                        .append(
                                input(
                                        "Descripción",
                                        "descripcion",
                                        "",
                                        false
                                )
                        )
                        .append(
                                "<button class='button' "
                                        + "type='submit'>"
                                        + "Crear pieza"
                                        + "</button>"
                                        + "</form>"
                                        + "</div>"
                                        + "<div class='records'>"
                        );

        for (Pieza item : items) {

            contenido
                    .append(
                            "<article class='record'>"
                                    + "<div class='record-title'>"
                                    + "<span class='eyebrow'>"
                    )
                    .append(
                            esc(
                                    item.getCodigo()
                            )
                    )
                    .append(
                            "</span>"
                                    + "<h2>"
                    )
                    .append(
                            esc(
                                    item.getNombre()
                            )
                    )
                    .append(
                            "</h2>"
                                    + "<p>"
                    )
                    .append(
                            esc(
                                    item.getDescripcion()
                            )
                    )
                    .append(
                            "</p>"
                                    + "<strong class='price'>"
                    )
                    .append(
                            String.format(
                                    Locale.ROOT,
                                    "%.2f €",
                                    item.getPrecio()
                            )
                    )
                    .append(
                            "</strong>"
                                    + "</div>"
                                    + "<details>"
                                    + "<summary>Editar</summary>"
                                    + "<form method='post' "
                                    + "action='/piezas/editar' "
                                    + "class='form-grid'>"
                    )
                    .append(
                            hidden(
                                    "codigo",
                                    item.getCodigo()
                            )
                    )
                    .append(
                            input(
                                    "Nombre",
                                    "nombre",
                                    item.getNombre(),
                                    true
                            )
                    )
                    .append(
                            input(
                                    "Precio",
                                    "precio",
                                    Double.toString(
                                            item.getPrecio()
                                    ),
                                    true,
                                    "number",
                                    "0.01"
                            )
                    )
                    .append(
                            input(
                                    "Descripción",
                                    "descripcion",
                                    item.getDescripcion(),
                                    false
                            )
                    )
                    .append(
                            "<button class='button' "
                                    + "type='submit'>"
                                    + "Guardar cambios"
                                    + "</button>"
                                    + "</form>"
                                    + "</details>"
                    )
                    .append(
                            formEliminar(
                                    "piezas",
                                    "codigo",
                                    item.getCodigo()
                            )
                    )
                    .append(
                            "</article>"
                    );
        }

        return contenido
                .append(
                        "</div>"
                )
                .toString();
    }

    private String paginaProyectos() {

        List<Proyecto> items;

        try (
                ProyectoDao dao =
                        new ProyectoDao()
        ) {

            items =
                    dao.listarTodos();
        }

        StringBuilder contenido =
                inicioSeccion(
                        "Proyectos",
                        "Gestión de proyectos registrados."
                )
                        .append(
                                "<div class='panel'>"
                                        + "<h2>Nuevo proyecto</h2>"
                                        + "<form method='post' "
                                        + "action='/proyectos/crear' "
                                        + "class='form-grid'>"
                        )
                        .append(
                                input(
                                        "Código",
                                        "codigo",
                                        "",
                                        true
                                )
                        )
                        .append(
                                input(
                                        "Nombre",
                                        "nombre",
                                        "",
                                        true
                                )
                        )
                        .append(
                                input(
                                        "Ciudad",
                                        "ciudad",
                                        "",
                                        true
                                )
                        )
                        .append(
                                selectEstado(
                                        "PENDIENTE"
                                )
                        )
                        .append(
                                "<button class='button' "
                                        + "type='submit'>"
                                        + "Crear proyecto"
                                        + "</button>"
                                        + "</form>"
                                        + "</div>"
                                        + "<div class='records'>"
                        );

        for (Proyecto item : items) {

            contenido
                    .append(
                            "<article class='record'>"
                                    + "<div class='record-title'>"
                                    + "<span class='eyebrow'>"
                    )
                    .append(
                            esc(
                                    item.getCodigo()
                            )
                    )
                    .append(
                            "</span>"
                                    + "<h2>"
                    )
                    .append(
                            esc(
                                    item.getNombre()
                            )
                    )
                    .append(
                            "</h2>"
                                    + "<p>"
                    )
                    .append(
                            esc(
                                    item.getCiudad()
                            )
                    )
                    .append(
                            "</p>"
                                    + "<span class='status'>"
                    )
                    .append(
                            esc(
                                    item.getEstado().toString()
                            )
                    )
                    .append(
                            "</span>"
                                    + "</div>"
                                    + "<details>"
                                    + "<summary>Editar</summary>"
                                    + "<form method='post' "
                                    + "action='/proyectos/editar' "
                                    + "class='form-grid'>"
                    )
                    .append(
                            hidden(
                                    "codigo",
                                    item.getCodigo()
                            )
                    )
                    .append(
                            input(
                                    "Nombre",
                                    "nombre",
                                    item.getNombre(),
                                    true
                            )
                    )
                    .append(
                            input(
                                    "Ciudad",
                                    "ciudad",
                                    item.getCiudad(),
                                    true
                            )
                    )
                    .append(
                            selectEstado(
                                    item.getEstado().name()
                            )
                    )
                    .append(
                            "<button class='button' "
                                    + "type='submit'>"
                                    + "Guardar cambios"
                                    + "</button>"
                                    + "</form>"
                                    + "</details>"
                    )
                    .append(
                            formEliminar(
                                    "proyectos",
                                    "codigo",
                                    item.getCodigo()
                            )
                    )
                    .append(
                            "</article>"
                    );
        }

        return contenido
                .append(
                        "</div>"
                )
                .toString();
    }

    private String paginaSuministros() {

        List<Gestion> items;
        List<Proveedor> proveedores;
        List<Pieza> piezas;
        List<Proyecto> proyectos;

        try (
                GestionDao gd =
                        new GestionDao();

                ProveedorDao pd =
                        new ProveedorDao();

                PiezaDao zd =
                        new PiezaDao();

                ProyectoDao rd =
                        new ProyectoDao()
        ) {

            items =
                    gd.listarTodos();

            proveedores =
                    pd.listarTodos();

            piezas =
                    zd.listarTodos();

            proyectos =
                    rd.listarTodos();
        }

        StringBuilder contenido =
                inicioSeccion(
                        "Suministros",
                        "Gestión de suministros registrados."
                )
                        .append(
                                "<div class='panel'>"
                                        + "<h2>Nuevo suministro</h2>"
                        );

        if (
                proveedores.isEmpty()
                        ||
                        piezas.isEmpty()
                        ||
                        proyectos.isEmpty()
        ) {

            contenido.append(
                    "<p>"
                            + "Para registrar un suministro, "
                            + "crea al menos un proveedor, "
                            + "una pieza y un proyecto."
                            + "</p>"
            );

        } else {

            contenido
                    .append(
                            "<form method='post' "
                                    + "action='/suministros/crear' "
                                    + "class='form-grid'>"
                    )
                    .append(
                            selectOptions(
                                    "Proveedor",
                                    "proveedor",
                                    proveedores
                                            .stream()
                                            .map(
                                                    x ->
                                                            opcion(
                                                                    x.getCodigo(),
                                                                    x.getNombre()
                                                                            + " "
                                                                            + x.getApellidos()
                                                            )
                                            )
                                            .toList()
                            )
                    )
                    .append(
                            selectOptions(
                                    "Pieza",
                                    "pieza",
                                    piezas
                                            .stream()
                                            .map(
                                                    x ->
                                                            opcion(
                                                                    x.getCodigo(),
                                                                    x.getNombre()
                                                            )
                                            )
                                            .toList()
                            )
                    )
                    .append(
                            selectOptions(
                                    "Proyecto",
                                    "proyecto",
                                    proyectos
                                            .stream()
                                            .filter(
                                                    x ->
                                                            x.getEstado()
                                                                    !=
                                                                    EstadoProyecto.FINALIZADO
                                            )
                                            .map(
                                                    x ->
                                                            opcion(
                                                                    x.getCodigo(),
                                                                    x.getNombre()
                                                                            + " · "
                                                                            + x.getEstado()
                                                            )
                                            )
                                            .toList()
                            )
                    )
                    .append(
                            input(
                                    "Fecha",
                                    "fecha",
                                    LocalDate.now().toString(),
                                    true,
                                    "date",
                                    ""
                            )
                    )
                    .append(
                            input(
                                    "Cantidad",
                                    "cantidad",
                                    "",
                                    true,
                                    "number",
                                    "1"
                            )
                    )
                    .append(
                            "<button class='button' "
                                    + "type='submit'>"
                                    + "Registrar suministro"
                                    + "</button>"
                                    + "</form>"
                    );
        }

        contenido.append(
                "</div>"
                        + "<div class='records'>"
        );

        for (Gestion item : items) {

            contenido
                    .append(
                            "<article class='record'>"
                                    + "<div class='record-title'>"
                                    + "<span class='eyebrow'>"
                    )
                    .append(
                            esc(
                                    item.getFecha().toString()
                            )
                    )
                    .append(
                            " · "
                    )
                    .append(
                            esc(
                                    item.getProyecto()
                                            .getEstado()
                                            .toString()
                            )
                    )
                    .append(
                            "</span>"
                                    + "<h2>"
                    )
                    .append(
                            esc(
                                    item.getPieza()
                                            .getNombre()
                            )
                    )
                    .append(
                            "</h2>"
                                    + "<p>"
                    )
                    .append(
                            esc(
                                    item.getProveedor()
                                            .getNombre()
                            )
                    )
                    .append(
                            " → "
                    )
                    .append(
                            esc(
                                    item.getProyecto()
                                            .getNombre()
                            )
                    )
                    .append(
                            "</p>"
                                    + "<strong class='price'>"
                                    + "Cantidad: "
                    )
                    .append(
                            item.getCantidad()
                    )
                    .append(
                            "</strong>"
                                    + "</div>"
                                    + "<details>"
                                    + "<summary>Editar</summary>"
                                    + "<form method='post' "
                                    + "action='/suministros/editar' "
                                    + "class='form-grid'>"
                    )
                    .append(
                            hidden(
                                    "id",
                                    item.getId().toString()
                            )
                    )
                    .append(
                            selectOptions(
                                    "Proveedor",
                                    "proveedor",
                                    proveedores
                                            .stream()
                                            .map(
                                                    x ->
                                                            opcion(
                                                                    x.getCodigo(),
                                                                    x.getNombre()
                                                            )
                                            )
                                            .toList(),
                                    item
                                            .getProveedor()
                                            .getCodigo()
                            )
                    )
                    .append(
                            selectOptions(
                                    "Pieza",
                                    "pieza",
                                    piezas
                                            .stream()
                                            .map(
                                                    x ->
                                                            opcion(
                                                                    x.getCodigo(),
                                                                    x.getNombre()
                                                            )
                                            )
                                            .toList(),
                                    item
                                            .getPieza()
                                            .getCodigo()
                            )
                    )
                    .append(
                            selectOptions(
                                    "Proyecto",
                                    "proyecto",
                                    proyectos
                                            .stream()
                                            .filter(
                                                    x ->
                                                            x.getEstado()
                                                                    !=
                                                                    EstadoProyecto.FINALIZADO
                                                                    ||
                                                                    x.getCodigo()
                                                                            .equals(
                                                                                    item
                                                                                            .getProyecto()
                                                                                            .getCodigo()
                                                                            )
                                            )
                                            .map(
                                                    x ->
                                                            opcion(
                                                                    x.getCodigo(),
                                                                    x.getNombre()
                                                                            + " · "
                                                                            + x.getEstado()
                                                            )
                                            )
                                            .toList(),
                                    item
                                            .getProyecto()
                                            .getCodigo()
                            )
                    )
                    .append(
                            input(
                                    "Fecha",
                                    "fecha",
                                    item.getFecha().toString(),
                                    true,
                                    "date",
                                    ""
                            )
                    )
                    .append(
                            input(
                                    "Cantidad",
                                    "cantidad",
                                    Integer.toString(
                                            item.getCantidad()
                                    ),
                                    true,
                                    "number",
                                    "1"
                            )
                    )
                    .append(
                            "<button class='button' "
                                    + "type='submit'>"
                                    + "Guardar cambios"
                                    + "</button>"
                                    + "</form>"
                                    + "</details>"
                    )
                    .append(
                            formEliminar(
                                    "suministros",
                                    "id",
                                    item.getId().toString()
                            )
                    )
                    .append(
                            "</article>"
                    );
        }

        return contenido
                .append(
                        "</div>"
                )
                .toString();
    }

    private String paginaLogin(
            String mensaje
    ) {

        String contenido =
                "<section class='login-wrap'>"
                        + "<div class='login-card'>"

                        + "<h1>Iniciar sesión</h1>"

                        + "<p class='lead'>"
                        + "Introduce tu usuario y contraseña."
                        + "</p>"

                        + (
                        mensaje.isBlank()
                                ? ""
                                : "<p class='notice'>"
                                + esc(mensaje)
                                + "</p>"
                )

                        + "<form method='post' "
                        + "action='/login' "
                        + "class='login-form'>"

                        + "<label>"
                        + "Usuario"
                        + "<input name='usuario' "
                        + "required "
                        + "maxlength='100' "
                        + "autocomplete='username'>"
                        + "</label>"

                        + "<label>"
                        + "Contraseña"
                        + "<input name='contrasena' "
                        + "type='password' "
                        + "required "
                        + "autocomplete='current-password'>"
                        + "</label>"

                        + "<button class='button button-light' "
                        + "type='submit'>"
                        + "Entrar"
                        + "</button>"

                        + "</form>"

                        + "<p class='fine-print'>"
                        + "El usuario inicial se configura desde la terminal."
                        + "</p>"

                        + "</div>"
                        + "</section>";

        return pagina(
                "Iniciar sesión",
                contenido
        );
    }

    private String pagina(
            String titulo,
            String contenido
    ) {

        return pagina(
                titulo,
                contenido,
                null,
                null
        );
    }

    private String pagina(
            String titulo,
            String contenido,
            String usuario,
            String csrf
    ) {

        String plantilla =
                cargarPlantilla(
                        usuario == null
                                ? "/web/login.html"
                                : "/web/app.html"
                );

        if (usuario != null) {

            contenido =
                    contenido.replace(
                            "</form>",
                            hidden(
                                    "csrf",
                                    csrf
                            )
                                    +
                                    "</form>"
                    );
        }

        return plantilla
                .replace(
                        "{{TITLE}}",
                        esc(titulo)
                )
                .replace(
                        "{{CONTENT}}",
                        contenido
                )
                .replace(
                        "{{USER}}",
                        usuario == null
                                ? ""
                                : esc(usuario)
                )
                .replace(
                        "{{CSRF}}",
                        usuario == null
                                ? ""
                                : hidden(
                                "csrf",
                                csrf
                        )
                );
    }

    private StringBuilder inicioSeccion(
            String titulo,
            String subtitulo
    ) {

        return new StringBuilder(
                "<section class='section-head'>"
                        + "<h1>"
        )
                .append(
                        esc(titulo)
                )
                .append(
                        "</h1>"
                                + "<p class='lead'>"
                )
                .append(
                        esc(subtitulo)
                )
                .append(
                        "</p>"
                                + "</section>"
                );
    }

    private String input(
            String etiqueta,
            String nombre,
            String valor,
            boolean requerido
    ) {

        return input(
                etiqueta,
                nombre,
                valor,
                requerido,
                "text",
                ""
        );
    }

    private String input(
            String etiqueta,
            String nombre,
            String valor,
            boolean requerido,
            String tipo,
            String step
    ) {

        return "<label>"
                + esc(etiqueta)

                + "<input type='"
                + tipo
                + "'"

                + " name='"
                + nombre
                + "'"

                + " value='"
                + esc(
                valor == null
                        ? ""
                        : valor
        )
                + "'"

                + (
                step.isEmpty()
                        ? ""
                        : " step='"
                        + step
                        + "'"
        )

                + (
                requerido
                        ? " required"
                        : ""
        )

                + ">"

                + "</label>";
    }

    private String hidden(
            String nombre,
            String valor
    ) {

        return "<input type='hidden' "
                + "name='"
                + nombre
                + "' "
                + "value='"
                + esc(valor)
                + "'>";
    }

    private String selectEstado(
            String seleccionado
    ) {

        return selectOptions(
                "Estado",
                "estado",
                List.of(
                        opcion(
                                "PENDIENTE",
                                "Pendiente"
                        ),
                        opcion(
                                "EN_CURSO",
                                "En curso"
                        ),
                        opcion(
                                "FINALIZADO",
                                "Finalizado"
                        )
                ),
                seleccionado
        );
    }

    private String selectOptions(
            String etiqueta,
            String nombre,
            List<String> opciones
    ) {

        return selectOptions(
                etiqueta,
                nombre,
                opciones,
                null
        );
    }

    private String selectOptions(
            String etiqueta,
            String nombre,
            List<String> opciones,
            String seleccionado
    ) {

        return "<label>"
                + esc(etiqueta)

                + "<select name='"
                + nombre
                + "' required>"

                + String.join(
                "",
                opciones
                        .stream()
                        .map(
                                op -> {

                                    String[] partes =
                                            op.split(
                                                    "\\|",
                                                    2
                                            );

                                    return "<option value='"
                                            + esc(
                                            partes[0]
                                    )
                                            + "'"

                                            + (
                                            partes[0]
                                                    .equals(
                                                            seleccionado
                                                    )
                                                    ? " selected"
                                                    : ""
                                    )

                                            + ">"

                                            + esc(
                                            partes[1]
                                    )

                                            + "</option>";
                                }
                        )
                        .toList()
        )

                + "</select>"
                + "</label>";
    }

    private String opcion(
            String valor,
            String texto
    ) {

        return valor
                + "|"
                + texto;
    }

    private String formEliminar(
            String recurso,
            String nombreCampo,
            String valor
    ) {

        return "<form method='post' "
                + "action='/"
                + recurso
                + "/eliminar' "
                + "data-confirm='¿Eliminar este registro?'>"

                + hidden(
                nombreCampo,
                valor
        )

                + "<button class='button button-outline' "
                + "type='submit'>"
                + "Eliminar"
                + "</button>"

                + "</form>";
    }

    private String requerido(
            String valor
    ) {

        if (
                valor == null
                        ||
                        valor.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Completa todos los campos obligatorios."
            );
        }

        return valor.trim();
    }

    private String obligatorio(
            Map<String, String> parametros,
            String nombre
    ) {

        return requerido(
                parametros.get(nombre)
        );
    }

    private String codigo(
            Map<String, String> parametros,
            String nombre
    ) {

        String codigo =
                obligatorio(
                        parametros,
                        nombre
                );

        if (!codigo.matches("[A-Za-z0-9]+")) {

            throw new IllegalArgumentException("El código solo puede contener letras y números.");
        }

        return codigo;
    }

    private double precio(
            String valor
    ) {

        try {

            double resultado =
                    Double.parseDouble(
                            requerido(valor)
                    );

            if (
                    !Double.isFinite(resultado)
                            ||
                            resultado < 0
            ) {

                throw new IllegalArgumentException(
                        "El precio debe ser cero o mayor."
                );
            }

            return resultado;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Introduce un precio válido."
            );
        }
    }

    private int entero(
            String valor,
            String etiqueta
    ) {

        try {

            int resultado =
                    Integer.parseInt(
                            requerido(valor)
                    );

            if (resultado <= 0) {

                throw new IllegalArgumentException(
                        etiqueta
                                +
                                " debe ser mayor que cero."
                );
            }

            return resultado;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    etiqueta
                            +
                            " debe ser un número entero válido."
            );
        }
    }

    private LocalDate fecha(
            String valor
    ) {

        try {

            return LocalDate.parse(
                    requerido(valor)
            );

        } catch (RuntimeException e) {

            throw new IllegalArgumentException(
                    "Introduce una fecha válida."
            );
        }
    }

    private EstadoProyecto estado(
            String valor
    ) {

        try {

            return EstadoProyecto.valueOf(
                    requerido(valor)
            );

        } catch (RuntimeException e) {

            throw new IllegalArgumentException(
                    "Selecciona un estado válido."
            );
        }
    }

    private <T> T requerido(
            T valor,
            String mensaje
    ) {

        if (valor == null) {

            throw new IllegalArgumentException(
                    mensaje
            );
        }

        return valor;
    }

    private Map<String, String> leerParametros(
            HttpExchange exchange
    ) throws IOException {

        String body =
                new String(
                        exchange
                                .getRequestBody()
                                .readAllBytes(),
                        StandardCharsets.UTF_8
                );

        Map<String, String> parametros =
                new HashMap<>();

        for (
                String par :
                body.split("&")
        ) {

            String[] partes =
                    par.split(
                            "=",
                            2
                    );

            String clave =
                    URLDecoder.decode(
                            partes[0],
                            StandardCharsets.UTF_8
                    );

            String valor =
                    partes.length > 1
                            ? URLDecoder.decode(
                            partes[1],
                            StandardCharsets.UTF_8
                    )
                            : "";

            parametros.put(
                    clave,
                    valor
            );
        }

        return parametros;
    }

    private SesionWeb obtenerSesion(
            HttpExchange exchange
    ) {

        String cabecera =
                exchange
                        .getRequestHeaders()
                        .getFirst("Cookie");

        if (cabecera == null) {
            return null;
        }

        for (
                String pieza :
                cabecera.split(";")
        ) {

            String[] par =
                    pieza
                            .trim()
                            .split(
                                    "=",
                                    2
                            );

            if (
                    par.length == 2
                            &&
                            COOKIE.equals(
                                    par[0]
                            )
            ) {

                SesionWeb sesion =
                        sesiones.get(
                                par[1]
                        );

                if (sesion != null) {

                    sesion.ultimoAcceso =
                            System.currentTimeMillis();
                }

                return sesion;
            }
        }

        return null;
    }

    private void limpiarSesionesCaducadas() {

        long ahora =
                System.currentTimeMillis();

        sesiones
                .values()
                .removeIf(
                        sesion -> {

                            if (
                                    ahora
                                            -
                                            sesion.ultimoAcceso
                                            <=
                                            SESSION_LIFETIME_MILLIS
                            ) {

                                return false;
                            }

                            cerrarRegistro(
                                    sesion
                            );

                            return true;
                        }
                );
    }

    private void cerrarRegistro(
            SesionWeb sesion
    ) {

        try {

            autenticacion.cerrarSesion(
                    sesion.idRegistroAcceso
            );

        } catch (RuntimeException e) {

            AppLogger.error(
                    "No se pudo guardar el cierre de sesión web para "
                            +
                            sesion.usuario,
                    e
            );
        }
    }

    private String rutaBase(
            String ruta
    ) {

        int indice =
                ruta.lastIndexOf('/');

        return indice <= 0
                ? "/dashboard"
                : ruta.substring(
                0,
                indice
        );
    }

    private String esc(
            String valor
    ) {

        if (valor == null) {
            return "";
        }

        return valor
                .replace(
                        "&",
                        "&amp;"
                )
                .replace(
                        "<",
                        "&lt;"
                )
                .replace(
                        ">",
                        "&gt;"
                )
                .replace(
                        "\"",
                        "&quot;"
                )
                .replace(
                        "'",
                        "&#39;"
                );
    }

    private void redirigir(
            HttpExchange exchange,
            String ruta
    ) throws IOException {

        exchange
                .getResponseHeaders()
                .set(
                        "Location",
                        ruta
                );

        exchange.sendResponseHeaders(
                303,
                -1
        );
    }

    private String redireccion() {

        return "<!doctype html>"
                + "<meta http-equiv='refresh' "
                + "content='0;url=/dashboard'>";
    }

    private void responder(
            HttpExchange exchange,
            int codigo,
            String contenido
    ) throws IOException {

        byte[] bytes =
                contenido.getBytes(
                        StandardCharsets.UTF_8
                );

        exchange
                .getResponseHeaders()
                .set(
                        "Content-Type",
                        "text/html; charset=utf-8"
                );

        exchange
                .getResponseHeaders()
                .set(
                        "X-Content-Type-Options",
                        "nosniff"
                );

        exchange
                .getResponseHeaders()
                .set(
                        "X-Frame-Options",
                        "DENY"
                );

        exchange
                .getResponseHeaders()
                .set(
                        "Referrer-Policy",
                        "no-referrer"
                );

        exchange.sendResponseHeaders(
                codigo,
                bytes.length
        );

        exchange
                .getResponseBody()
                .write(bytes);
    }

    private void responderRecurso(
            HttpExchange exchange,
            String ruta,
            String tipoContenido
    ) throws IOException {

        try (
                var recurso =
                        InterfazWeb.class
                                .getResourceAsStream(
                                        ruta
                                )
        ) {

            if (recurso == null) {

                responder(
                        exchange,
                        404,
                        "Recurso no disponible."
                );

                return;
            }

            byte[] bytes =
                    recurso.readAllBytes();

            exchange
                    .getResponseHeaders()
                    .set(
                            "Content-Type",
                            tipoContenido
                    );

            exchange
                    .getResponseHeaders()
                    .set(
                            "X-Content-Type-Options",
                            "nosniff"
                    );

            exchange.sendResponseHeaders(
                    200,
                    bytes.length
            );

            exchange
                    .getResponseBody()
                    .write(bytes);
        }
    }

    private String cargarPlantilla(
            String ruta
    ) {

        try (
                var recurso =
                        InterfazWeb.class
                                .getResourceAsStream(
                                        ruta
                                )
        ) {

            if (recurso == null) {

                throw new IllegalStateException(
                        "Plantilla web no disponible: "
                                +
                                ruta
                );
            }

            return new String(
                    recurso.readAllBytes(),
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "No se pudo cargar la plantilla web: "
                            +
                            ruta,
                    e
            );
        }
    }

    private static final class SesionWeb {

        private final String token;

        private final String usuario;

        private final Long idRegistroAcceso;

        private volatile long ultimoAcceso;

        private SesionWeb(
                String token,
                String usuario,
                Long idRegistroAcceso,
                long ultimoAcceso
        ) {

            this.token =
                    token;

            this.usuario =
                    usuario;

            this.idRegistroAcceso =
                    idRegistroAcceso;

            this.ultimoAcceso =
                    ultimoAcceso;
        }
    }

    private static final class IntentosAcceso {

        private int fallos;

        private long bloqueadoHasta;
    }
}