package client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class Session {
    private static Session instance;
    private Socket socket;
    private String token;

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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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
}
