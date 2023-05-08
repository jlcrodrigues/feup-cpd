package client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

public class Session {
    private static Session instance;
    private Socket socket;
    private Map<String, Object> profile;
    private Scanner scanner;

    private Session() {
        Properties properties = new Properties();
        try (InputStream is = Client.class.getResourceAsStream("application.properties")) {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String hostname = properties.getProperty("hostname");
        int port = Integer.parseInt(properties.getProperty("port"));

        try  {
            socket = new Socket(hostname, port);
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }

        scanner = new Scanner(System.in);
    }

    public static Session getSession() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getProfileInfo(String field) {
        return profile.get(field).toString();
    }

    public void setProfile(Map<String, Object> profile) {
        this.profile = profile;
    }

    public void writeMessage(String module, String method, Map<String, Object> args) {
        try {
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            writer.println(module);
            writer.println(method);
            writer.println(mapToJsonString(args));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Scanner getScanner() {
        return scanner;
    }

    public String[] readResponse() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            return reader.readLine().split(" ", 2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String readLine() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            return reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String mapToJsonString(Map<String, Object> map) {
        String json = "{";
        json += map.entrySet().stream()
                .map(entry -> "\"" + entry.getKey() + "\":" + "\"" + entry.getValue().toString() + "\"")
                .collect(Collectors.joining(","));
        json += "}";
        return json;
    }

    public static Map<String, Object> jsonStringToMap(String jsonString) {
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
