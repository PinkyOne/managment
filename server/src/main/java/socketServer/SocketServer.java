package socketServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {

    private ServerSocket serverSocket;

    public void setPort(int port) {
        this.port = port;
    }

    private int port = 1234;

    public void start() throws IOException {
        System.out.println("Starting the socket server at port:" + port);
        serverSocket = new ServerSocket(port);

        //Listen for clients. Block till one connects
        Socket client;

        while (true) {
            System.out.println("Waiting for clients...");
            client = serverSocket.accept();
            System.out.println("The following client has connected:" + client.getInetAddress().getCanonicalHostName());
            //A client has connected to this server. Send welcome message
            Thread thread = new Thread(new SocketClientHandler(client));
            thread.start();
        }
    }


    /**
     * Creates a SocketServer object and starts the server.
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            // initializing the Socket Server
            SocketServer socketServer = new SocketServer();
            socketServer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() throws IOException {
        serverSocket.close();
    }
}