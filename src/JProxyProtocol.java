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
        boolean isChainedProxy = Boolean.parseBoolean(prop.getProperty("proxy.chained"));

        logger.log(Level.INFO, "Starting JProxyProtocol...");
        System.out.println("JProxyProtocol has started. Done!");

        try (ServerSocket serverSocket = new ServerSocket(listenPort)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                String clientIp = clientSocket.getInetAddress().getHostAddress();

                // Log connection made
                logger.log(Level.INFO, "Connection made from: " + clientIp);

                // Handle each connection in a new thread
                new Thread(() -> {
                    try (Socket targetSocket = new Socket(targetIp, targetPort)) {
                        OutputStream targetOut = targetSocket.getOutputStream();

                        // Write Proxy Protocol v1 header
                        String header;
                        String finalClientIp = clientIp;
                        if (isChainedProxy) {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                            String proxyHeader = reader.readLine(); // Proxy Protocol header from client
                            String[] parts = proxyHeader.split(" ");
                            if (parts.length < 6) {
                                throw new IOException("Invalid Proxy Protocol header from client");
                            }
                            finalClientIp = parts[2];
                        }
                        header = "PROXY TCP4 " + finalClientIp + " " + targetSocket.getInetAddress().getHostAddress() + " "
                                + clientSocket.getPort() + " " + targetSocket.getPort() + "\r\n";
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
                        logger.log(Level.INFO, "Connection disconnected from: " + clientIp); // Log connection disconnected
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
