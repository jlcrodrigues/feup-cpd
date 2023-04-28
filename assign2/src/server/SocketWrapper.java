package server;

import java.io.*;
import java.net.Socket;

public class SocketWrapper {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

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
}
