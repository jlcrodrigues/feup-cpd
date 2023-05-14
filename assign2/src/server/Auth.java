package server;

import server.game.User;
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

/**
 * Defines functions within the authentication task.
 */
public class Auth extends ConnectionHandler {
    private final String fileName = "src/server/users.txt";

    public Auth(SocketWrapper socket) {
        this.socket = socket;
    }

    /**
     * Executes authentication method specified in the message: either login or register.
     */
    @Override
    public void run() {
        String method = socket.readLine().toLowerCase();
        switch (method) {
            case "login":
                login();
                Store.getStore().registerIdleSocket(socket);
                break;
            case "register":
                register();
                Store.getStore().registerIdleSocket(socket);
                break;
            case "logout":
                logout();
                Store.getStore().registerIdleSocket(socket);
                break;
            default:
                socket.writeLine("1 Invalid command");
        }
    }

    public void login() {
        String argsString = socket.readLine();
        Map<String, Object> args = jsonStringToMap(argsString);

        if (!validInput(args)) return;
        Store store = Store.getStore();

        User user = checkUser(args.get("username").toString(), args.get("password").toString());
        if (user == null) {
            socket.writeLine("1 Invalid username or password");
            return;
        }

        UUID uuid = UUID.randomUUID();
        String token = uuid.toString();

        user.setToken(token);

        if (!Store.getStore().loginUser(user)) {
            socket.writeLine("1 User already logged in");
            store.log(Level.INFO, "Login attempt: " + args.get("username") + " already logged in");
            return;
        }
        socket.writeLine("0 " + mapToJsonString(user.toMap()));
        store.log(Level.INFO, "New login: " + args.get("username") + " " + token);
    }

    public void register() {
        String argsString = socket.readLine();
        Map<String, Object> args = jsonStringToMap(argsString);

        if (!validInput(args)) return;

        if (getUserInfo(args.get("username").toString()) != null) {
            socket.writeLine("1 Username already in use.");
            return;
        }

        try {
            FileWriter fileWriter = new FileWriter(fileName, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(
                    (args.get("password").toString()).getBytes(StandardCharsets.UTF_8));

            bufferedWriter.newLine();
            bufferedWriter.write(args.get("username") + "," + bytesToHex(encodedHash) + ",1000");

            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            socket.writeLine("1 An error occurred");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        socket.writeLine("0 ");
        Store store = Store.getStore();
        store.log(Level.INFO, "New register: " + args.get("username"));
    }

    private void logout() {
        String argsString = socket.readLine();
        String token = jsonStringToMap(argsString).get("token").toString();
        Store.getStore().log(Level.INFO, "Logout: " + token);
        Store.getStore().logoutUser(token);
        socket.writeLine("0 ");
    }

    /**
     * Check if the provided credentials match our records.
     * @param username Username to check.
     * @param password Password for that account.
     * @return User object if the credentials are correct and null otherwise.
     */
    private User checkUser(String username, String password)  {
        String[] info = getUserInfo(username);
        if (info == null) {
            return null;
        }
        String userPassword = info[0];
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        byte[] encodedHash = digest.digest(
                password.getBytes(StandardCharsets.UTF_8));
        if (!userPassword.equals(bytesToHex(encodedHash))) {
            return null;
        }
        return new User(socket, username, Integer.parseInt(info[1]));
    }

    /**
     * Retrieve the hashed user password and elo from storage.
     * @param username Username to search for.
     * @return List with user password hashed with sha-256 and elo or null if the user does not exist.
     */
    private String[] getUserInfo(String username) {
        File file = new File(fileName);

        try {
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] fields = line.split(",");

                if (fields[0].equals(username)) {
                    return new String[]{fields[1], fields[2]};
                }
            }

            scanner.close();
        } catch (FileNotFoundException e) {
            Store.getStore().log(Level.SEVERE, "File not found: " + fileName);
        }
        return null;
    }

    private Boolean validInput(Map<String, Object> args) {
        if (args.get("username") == null || args.get("username").equals("")) {
            socket.writeLine("1 Username not provided");
            return false;
        }
        else if (args.get("password") == null || args.get("password").equals("")) {
            socket.writeLine("1 Password not provided");
            return false;
        }
        return true;
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