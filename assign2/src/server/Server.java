package server;

import server.store.SocketWrapper;
import server.store.Store;

import java.io.*;
import java.net.*;
import java.util.logging.Level;

public class Server {

    public static void main(String[] args) {
        Store store = Store.getStore();

        try (ServerSocket serverSocket = new ServerSocket(store.getPort())) {
            store.log(Level.INFO, "Server is listening on port "  + store.getPort());

            while (true) {
                Socket socket = serverSocket.accept();

                ConnectionHandler handler = new ConnectionHandler(new SocketWrapper(socket));
                store.execute(handler);
            }

        } catch (IOException ex) {
            store.log(Level.SEVERE, "Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}