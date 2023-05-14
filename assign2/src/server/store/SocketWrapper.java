package server.store;

import java.io.*;
import java.net.Socket;

/**
 * Defines utility functions to deal with sockets.
 */
public class SocketWrapper {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    /**
     * Creates the wrapper. Opens relevant buffers.
     * @param socket
     */
    public SocketWrapper(Socket socket) {
        this.socket = socket;
        InputStream input = null;
        try {
            input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));

            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        reader = new BufferedReader(new InputStreamReader(input));

    }

    public Socket getSocket() {
        return socket;
    }

    public synchronized String readLine() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeLine(String message) {
        writer.println(message);
    }

    public String toString() {
        return String.format("%s:%d", socket.getInetAddress().getHostAddress(), socket.getPort());
    }
}
