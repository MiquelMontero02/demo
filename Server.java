import com.sun.net.httpserver.SimpleFileServer;
import java.net.InetSocketAddress;
import java.nio.file.Path;

public class Server {
    public static void main(String[] args) {
        var puerto = new InetSocketAddress(8080);
        // Carpeta raíz donde se encuentra tu index.html
        var directorio = Path.of(".").toAbsolutePath();

        var servidor = SimpleFileServer.createFileServer(
            puerto,
            directorio,
            SimpleFileServer.OutputLevel.INFO
        );

        servidor.start();
        System.out.println("Servidor activo en http://localhost:8080");
    }
}