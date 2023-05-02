package server;

import server.store.SocketWrapper;
import server.store.Store;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

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

        // Remove the outer curly braces from the JSON string
        jsonString = jsonString.substring(1, jsonString.length() - 1);

        // Split the remaining string into key-value pairs
        String[] keyValuePairs = jsonString.split(",");

        // Iterate over each key-value pair and add it to the map
        for (String pair : keyValuePairs) {
            // Split each key-value pair into key and value
            String[] keyValue = pair.split(":");
            String key = keyValue[0].replaceAll("\"", "").trim();
            String valueString = keyValue[1].replaceAll("\"", "").trim();
            Object value;

            // Try to parse the value as an integer or a double
            try {
                value = Integer.parseInt(valueString);
            } catch (NumberFormatException e) {
                try {
                    value = Double.parseDouble(valueString);
                } catch (NumberFormatException ex) {
                    // If the value is not a number, use it as a string
                    value = valueString;
                }
            }

            // Add the key-value pair to the map
            map.put(key, value);
        }

        return map;
    }

}
