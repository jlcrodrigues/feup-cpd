package server;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class SocketPool {
    private static final int MAX_SOCKETS = 10;
    private int sockets = 0;
    private final Queue<Socket> pool = new LinkedList<>();
    private final String host;
    private final int port;

    public SocketPool(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public synchronized Socket getSocket() throws InterruptedException, IOException {
        if (pool.isEmpty()) {
            return createSocket();
        } else {
            return pool.poll();
        }
    }

    public synchronized void returnSocket(Socket socket) {
        if (pool.size() < MAX_SOCKETS) {
            pool.offer(socket);
        } else {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket");
            }
        }
    }

    private Socket createSocket() throws IOException {
        if (sockets < MAX_SOCKETS) {
            sockets++;
            return new Socket(host, port);
        } else {
            return null;
        }
    }
}
