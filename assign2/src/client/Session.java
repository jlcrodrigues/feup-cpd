package client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Defines a session with the server.
 * This is used to both interact with the server and keep relevant ephemeral information.
 */
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

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isLoggedIn() {
        return profile != null;
    }

    /**
     * Loads a session from permanent storage.
     * Every time a token is created it is stored so it can be used to load the session.
     */
    public void load() {
        try {
            File file = new File("client/session.txt");
            FileInputStream fileStream = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream));

            String token = reader.readLine();

            Map<String, Object> args = Map.of("token", token);
            writeMessage("matchmaking", "profile", args);
            String[] response = readResponse();
            if (response[0].equals("0")) {
                profile = jsonStringToMap(response[1]);
            }
        } catch (Exception e) {
            // do not load if session does not exist
            System.out.println("Session file not found");
        }
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getProfileInfo(String field) {
        return profile.get(field).toString();
    }

    public void setProfile(Map<String, Object> profile) {
        this.profile = profile;

        try {
            PrintWriter writer = new PrintWriter("client/session.txt");
            writer.println(profile == null ? "" : profile.get("token"));
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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

    /**
     * Get the scanner used to read user input.
     * This is used to avoid creating multiple scanners.
     */
    public Scanner getScanner() {
        return scanner;
    }

    /**
     * Utility function to read a response from the server.
     * This follows the protocol's specification.
     * @return String array with two elements: status code and response body.
     */
    public String[] readResponse() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            return reader.readLine().split(" ", 2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Utility function to read a line from the server.
     * @return String with the line read.
     */
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
