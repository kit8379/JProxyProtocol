import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
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
                logger.log(Level.INFO, "Connection established from: " + clientSocket.getInetAddress().getHostAddress()); // Log connection made

                // Handle each connection in a new thread
                new Thread(() -> {
                    try (Socket targetSocket = new Socket(targetIp, targetPort)) {
                        OutputStream targetOut = targetSocket.getOutputStream();

                        // Write Proxy Protocol v1 header
                        String header = "PROXY TCP4 " + clientSocket.getInetAddress().getHostAddress() + " "
                                + targetSocket.getInetAddress().getHostAddress() + " " + clientSocket.getPort() + " "
                                + targetSocket.getPort() + "\r\n";
                        targetOut.write(header.getBytes());
                        targetOut.flush();

                        // Forward data between client and server
                        Thread t1 = new Thread(new Forwarder(clientSocket.getInputStream(), targetOut, clientSocket, targetSocket));
                        Thread t2 = new Thread(new Forwarder(targetSocket.getInputStream(), clientSocket.getOutputStream(), targetSocket, clientSocket));

                        t1.start();
                        t2.start();

                        t1.join();
                        t2.join();

                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "An exception occurred in the main method.", e);
                    } finally {
                        logger.log(Level.INFO, "Connection disconnected from: " + clientSocket.getInetAddress().getHostAddress()); // Log connection disconnected
                    }
                }).start();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An exception occurred in the main method.", e);
        }
    }
}

class Forwarder implements Runnable {
    private final InputStream in;
    private final OutputStream out;
    private final Socket inSocket;
    private final Socket outSocket;

    Forwarder(InputStream in, OutputStream out, Socket inSocket, Socket outSocket) {
        this.in = in;
        this.out = out;
        this.inSocket = inSocket;
        this.outSocket = outSocket;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[4096];
        try {
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (SocketException se) {
            // Socket has been closed, handle this scenario here
        } catch (Exception e) {
            JProxyProtocol.logger.log(Level.SEVERE, "An exception occurred while forwarding data.", e);
        } finally {
            try {
                // Here, you shutdown the output streams without closing the sockets
                if(!inSocket.isOutputShutdown()) {
                    inSocket.shutdownOutput();
                }
                if(!outSocket.isOutputShutdown()) {
                    outSocket.shutdownOutput();
                }
            } catch (Exception e) {
                JProxyProtocol.logger.log(Level.SEVERE, "An exception occurred while closing streams.", e);
            }
        }
    }
}