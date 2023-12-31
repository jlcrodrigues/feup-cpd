package server;

import server.store.SocketWrapper;
import server.store.Store;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

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
        Store store = Store.getStore();

        String tempLine = socket.readLine();
        if (tempLine == null) {
            store.log(Level.INFO, "Error reading from socket " + socket + ", may be disconnected.");
            return;
        }
        String line = tempLine.toLowerCase();
        switch (line) {
            case "auth":
                store.execute(new Auth(socket));
                break;
            case "matchmaking":
                store.execute(new Matchmaking(socket));
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

    protected static String mapToJsonString(Map<String, Object> map) {
        String json = "{";
        json += map.entrySet().stream()
                .map(entry -> "\"" + entry.getKey() + "\":" + "\"" + entry.getValue().toString() + "\"")
                .collect(Collectors.joining(","));
        json += "}";
        return json;
    }

}
