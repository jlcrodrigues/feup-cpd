package server;

import server.store.SocketWrapper;
import server.store.Store;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Defines a general use task. It can be used to handle new connections or as a super class for other tasks.
 */
public class ConnectionHandler implements Runnable {
    protected SocketWrapper socket;
    protected BufferedReader reader;

    public ConnectionHandler() {
        socket = null;
    }

    public ConnectionHandler(SocketWrapper socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        Store.getStore().log(Level.INFO, "New connection: " + socket.getSocket().getInetAddress().getHostAddress());
        Store store = Store.getStore();

        String line = socket.readLine().toLowerCase();
        switch (line) {
            case "auth":
                store.execute(new Auth(socket));
                break;
            default:
                socket.writeLine("1 Invalid command");
        }
    }

    protected static Map<String, Object> jsonStringToMap(String jsonString) {
        Map<String, Object> map = new HashMap<>();
        jsonString = jsonString.substring(1, jsonString.length() - 1);
        String[] keyValuePairs = jsonString.split(",");

        for (String pair : keyValuePairs) {
            String[] keyValue = pair.split(":");
            String key = keyValue[0].replaceAll("\"", "").trim();
            String valueString = keyValue[1].replaceAll("\"", "").trim();
            Object value;

            try {
                value = Integer.parseInt(valueString);
            } catch (NumberFormatException e) {
                try {
                    value = Double.parseDouble(valueString);
                } catch (NumberFormatException ex) {
                    value = valueString;
                }
            }

            map.put(key, value);
        }

        return map;
    }

}
