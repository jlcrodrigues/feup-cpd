package server;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Store {
    private static Store instance;
    private ExecutorService threadPool;
    private SocketPool socketPool;
    private int port;

    private Store() {
        port = 8081;
        int numThreads = Runtime.getRuntime().availableProcessors(); // use one thread per CPU core
        threadPool = Executors.newFixedThreadPool(numThreads);
        socketPool = new SocketPool("localhost", port);
    }

    public static Store getStore() {
        if (instance == null) {
            instance = new Store();
        }
        return instance;
    }

    public int getPort() {
        return port;
    }

    public Socket getSocket() throws InterruptedException, IOException {
        return socketPool.getSocket();
    }

    public void returnSocket(Socket socket) {
        socketPool.returnSocket(socket);
    }

    public void execute(Runnable task) {
        threadPool.execute(task);
    }
}