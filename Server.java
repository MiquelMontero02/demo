import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Server {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // 1. Ruta principal: sirve el index.html
        server.createContext("/", exchange -> {
            Path archivo = Paths.get("index.html");
            if (Files.exists(archivo)) {
                byte[] contenido = Files.readAllBytes(archivo);
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, contenido.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(contenido);
                }
            } else {
                byte[] error = "404 - No se encontró index.html".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(404, error.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(error);
                }
            }
        });

        // 2. NUEVA RUTA: recibe el submit del formulario en /login
        server.createContext("/login", exchange -> {
            try (exchange) {
                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    // Leemos los bytes enviados por el formulario
                    byte[] datosCrudos = exchange.getRequestBody().readAllBytes();
                    String cuerpo = new String(datosCrudos, StandardCharsets.UTF_8);
                    
                    // Decodificamos formato URL (ej: usuario=juan%40mail.com&password=123)
                    String datosDecodificados = URLDecoder.decode(cuerpo, StandardCharsets.UTF_8);
                    
                    // Imprimimos en la consola del servidor
                    System.out.println("=== Nuevo Submit Recibido ===");
                    System.out.println("Crudo: " + cuerpo);
                    for (String parametro : datosDecodificados.split("&")) {
                        System.out.println(" -> " + parametro);
                    }
                    
                    // Redirigimos de vuelta al inicio (/) para completar el ciclo HTTP
                    exchange.getResponseHeaders().set("Location", "/");
                    exchange.sendResponseHeaders(302, -1);
                } else {
                    // Si alguien entra a /login por GET, devolvemos Method Not Allowed
                    exchange.sendResponseHeaders(405, -1);
                }
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Servidor iniciado en http://localhost:8080");
    }
}