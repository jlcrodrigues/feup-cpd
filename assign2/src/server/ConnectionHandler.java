package server;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

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

        String line = readSocketLine(socket).toLowerCase();
        switch (line) {
            case "auth":
                store.execute(new Auth(socket));
                break;
            default:
                writeSocket(socket, "1 Invalid command");
        }
    }

    protected String readSocketLine(Socket socket) {
        InputStream input = null;
        try {
            input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            return reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void writeSocket(Socket socket, String message) {
        try {
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            writer.println(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
