import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JProxyProtocol {
    public static final Logger logger = Logger.getLogger(JProxyProtocol.class.getName());

    public static void main(String[] args) {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            prop.load(input);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Unable to load properties file.", ex);
        }

        int listenPort = Integer.parseInt(prop.getProperty("listen.port"));
        String targetIp = prop.getProperty("target.ip");
        int targetPort = Integer.parseInt(prop.getProperty("target.port"));

        logger.log(Level.INFO, "Starting JProxyProtocol...");
        try (ServerSocket serverSocket = new ServerSocket(listenPort)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();

                Socket targetSocket = new Socket(targetIp, targetPort);
                OutputStream targetOut = targetSocket.getOutputStream();

                // Write Proxy Protocol v1 header
                String header = "PROXY TCP4 " + clientSocket.getInetAddress().getHostAddress() + " "
                        + targetSocket.getInetAddress().getHostAddress() + " " + clientSocket.getPort() + " "
                        + targetSocket.getPort() + "\r\n";
                targetOut.write(header.getBytes());
                targetOut.flush();

                // Forward data between client and server
                new Thread(new Forwarder(clientSocket.getInputStream(), targetOut)).start();
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
            JProxyProtocol.logger.log(Level.SEVERE, "An exception occurred while forwarding data.", e);
        } finally {
            try {
                in.close();
                out.close();
            } catch (Exception e) {
                JProxyProtocol.logger.log(Level.SEVERE, "An exception occurred while closing streams.", e);
            }
        }
    }
}
