package server;

import java.io.*;
import java.net.Socket;
import java.util.Date;

public class Auth implements Runnable {
    private Socket socket;

    public Auth(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        InputStream input = null;
        try {
            input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            String time = reader.readLine();

            System.out.println("New client connected: "+ time);

            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            writer.println(new Date().toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void login() {

    }

    public static void register() {

    }

    public static void logout() {

    }

    // get player from storage
    public static void getPlayer() {

    }

}