package server;

import server.concurrent.Database;
import server.game.User;
import server.store.SocketWrapper;
import server.store.Store;
import server.utils.Utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Defines functions within the authentication task.
 */
public class Auth extends ConnectionHandler {

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
                break;
            case "register":
                register();
                break;
            case "logout":
                logout();
                break;
            default:
                socket.writeLine("1 Invalid command");
        }
        Store.getStore().registerIdleSocket(socket);
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

        Database db = Store.getStore().getDatabase();
        if (db.getUserInfo(args.get("username").toString()) != null) {
            socket.writeLine("1 Username already in use.");
            return;
        }

        try {
            db.registerUser(args);
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
        String[] info = Store.getStore().getDatabase().getUserInfo(username);
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
        if (!userPassword.equals(Utils.bytesToHex(encodedHash))) {
            return null;
        }
        return new User(socket, username, Integer.parseInt(info[1]));
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

}