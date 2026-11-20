import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Server {

    // Credenciales fijas a nivel de constante
    private static final String USER = "admin";
    private static final String PASSWORD = "admin";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // 1. Ruta principal: sirve index.html
        server.createContext("/", exchange -> {
            if ("/".equals(exchange.getRequestURI().getPath())) {
                servirArchivo(exchange, "index.html");
            } else {
                try (exchange) {
                    exchange.sendResponseHeaders(404, -1);
                }
            }
        });

        // 2. Nueva ruta: sirve home.html (ignora automáticamente los parámetros ?usuario=...)
        server.createContext("/home.html", exchange -> {
            servirArchivo(exchange, "home.html");
        });

        // 3. Procesamiento del login y redirección
        server.createContext("/login", exchange -> {
            try (exchange) {
                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    String cuerpo = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    Map<String, String> datos = parsearFormulario(cuerpo);
                    
                    String usuarioRecibido = datos.getOrDefault("usuario", "");
                    String passwordRecibido = datos.getOrDefault("password", "");
                    
                    System.out.println("Intento de login -> Usuario: " + usuarioRecibido + " | Password: " + passwordRecibido);
                    
                    if (USER.equals(usuarioRecibido) && PASSWORD.equals(passwordRecibido)) {
                        // Login correcto: redirigimos a home.html pasando el usuario por query param
                        String usuarioUrl = URLEncoder.encode(usuarioRecibido, StandardCharsets.UTF_8);
                        exchange.getResponseHeaders().set("Location", "/home.html?usuario=" + usuarioUrl);
                        exchange.sendResponseHeaders(302, -1);
                    } else {
                        // Login incorrecto: devolvemos al login con indicador de error
                        System.out.println("Credenciales incorrectas.");
                        exchange.getResponseHeaders().set("Location", "/?error=1");
                        exchange.sendResponseHeaders(302, -1);
                    }
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Servidor iniciado en http://localhost:8080");
    }

    // Método auxiliar para enviar archivos HTML
    private static void servirArchivo(HttpExchange exchange, String nombreArchivo) throws IOException {
        try (exchange) {
            Path archivo = Paths.get(nombreArchivo);
            if (Files.exists(archivo)) {
                byte[] contenido = Files.readAllBytes(archivo);
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, contenido.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(contenido);
                }
            } else {
                byte[] error = ("404 - No se encontró " + nombreArchivo).getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(404, error.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(error);
                }
            }
        }
    }

    // Convierte "usuario=admin&password=admin" en un Map<String, String> decodificado
    private static Map<String, String> parsearFormulario(String cuerpo) {
        Map<String, String> parametros = new HashMap<>();
        if (cuerpo == null || cuerpo.isEmpty()) return parametros;

        for (String par : cuerpo.split("&")) {
            String[] kv = par.split("=", 2);
            String clave = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String valor = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            parametros.put(clave, valor);
        }
        return parametros;
    }
}