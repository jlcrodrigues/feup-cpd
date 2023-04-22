package server;

import java.io.*;
import java.net.*;

public class Server {

    public static void main(String[] args) {
        Store store = Store.getStore();

        try (ServerSocket serverSocket = new ServerSocket(store.getPort())) {

            System.out.println("Server is listening on port " + store.getPort());

            while (true) {
                Socket socket = serverSocket.accept();

                Auth auth = new Auth(socket);
                store.execute(auth);
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}