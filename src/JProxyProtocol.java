import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JProxyProtocol {
    private static final Logger logger = Logger.getLogger(SimpleProx.class.getName());

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8080)) { // listen on port 8080
            while (true) {
                Socket clientSocket = serverSocket.accept(); // accept connection from client
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String proxyHeader = reader.readLine(); // read Proxy Protocol header

                Socket targetSocket = new Socket("localhost", 8081); // connect to target server
                PrintWriter writer = new PrintWriter(targetSocket.getOutputStream());
                writer.println(proxyHeader); // write Proxy Protocol header
                writer.flush();

                // forward data between client and server
                new Thread(new Forwarder(clientSocket.getInputStream(), targetSocket.getOutputStream())).start();
                new Thread(new Forwarder(targetSocket.getInputStream(), clientSocket.getOutputStream())).start();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An exception occurred in the main method.", e);
        }
    }
}

class Forwarder implements Runnable {
    private final InputStream in;
    private final OutputStream out;

    Forwarder(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (Exception e) {
            SimpleProx.logger.log(Level.SEVERE, "An exception occurred while forwarding data.", e);
        } finally {
            try {
                in.close();
                out.close();
            } catch (Exception e) {
                SimpleProx.logger.log(Level.SEVERE, "An exception occurred while closing streams.", e);
            }
        }
    }
}
