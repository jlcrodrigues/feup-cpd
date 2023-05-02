package server;

import server.game.Player;
import server.store.SocketWrapper;
import server.store.Store;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Level;

public class Auth extends ConnectionHandler {
    private final String fileName = "src/server/users.txt";

    public Auth(SocketWrapper socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String method = socket.readLine().toLowerCase();
        switch (method) {
            case "login":
                login();
                break;
            case "register":
                register();
                break;
            default:
                socket.writeLine("1 Invalid command");
        }
    }

    public void login() {
        String argsString = socket.readLine();
        Map<String, Object> args = jsonStringToMap(argsString);

        if (!checkUser((String) args.get("username"), (String) args.get("password"))) {
            socket.writeLine("1 Invalid username or password");
            return;
        }

        UUID uuid = UUID.randomUUID();
        String token = uuid.toString();

        Store.getStore().log(Level.INFO, "New login: " + args.get("username") + " " + token);

        socket.writeLine("0 " + token);
        Matchmaking.getMatchmaking().addPlayer(new Player(socket, token));
    }

    public void register() {
        String argsString = socket.readLine();
        Map<String, Object> args = jsonStringToMap(argsString);

        try {
            FileWriter fileWriter = new FileWriter(fileName, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(
                    ((String) args.get("password")).getBytes(StandardCharsets.UTF_8));

            bufferedWriter.newLine();
            bufferedWriter.write(args.get("username") + "," + bytesToHex(encodedHash));

            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            socket.writeLine("1 An error occurred");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        socket.writeLine("0 ");
        Store.getStore().log(Level.INFO, "New register: " + args.get("username"));
    }

    private boolean checkUser(String username, String password)  {
        String userPassword = getUserPassword(username);
        if (userPassword == null) {
            return false;
        }
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
        byte[] encodedHash = digest.digest(
                password.getBytes(StandardCharsets.UTF_8));
        return userPassword.equals(bytesToHex(encodedHash));
    }

    private String getUserPassword(String username) {
        File file = new File(fileName);

        try {
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] fields = line.split(",");

                if (fields[0].equals(username)) {
                    return fields[1];
                }
            }

            scanner.close();
        } catch (FileNotFoundException e) {
            Store.getStore().log(Level.SEVERE, "File not found: " + fileName);
        }
        return null;
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}