package server;

import java.io.*;
import java.net.Socket;

public class ConnectionHandler implements Runnable {
    private Socket socket;

    public ConnectionHandler() {
        socket = null;
    }

    public ConnectionHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.println("New connection: " + socket.getInetAddress().getHostAddress());
        Store store = Store.getStore();
        store.execute(new Auth(socket));
    }

    public String readSocketLine(Socket socket) {
        InputStream input = null;
        try {
            input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            return reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeSocket(Socket socket, String message) {
        try {
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            writer.println(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
