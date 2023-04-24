package server;

import server.game.Player;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class Auth extends ConnectionHandler {
    private Socket socket;

    public Auth(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String method = readSocketLine(socket).toLowerCase();
        switch (method) {
            case "login":
                login();
                break;
            case "register":
                register();
                break;
            default:
                writeSocket(socket, "1 Invalid command");
        }
    }

    public void login() {
        String argsString = readSocketLine(socket);
        Map<String, Object> args = jsonStringToMap(argsString);

        if (!checkUser((String) args.get("username"), (String) args.get("password"))) {
            writeSocket(socket, "1 Invalid username or password");
            return;
        }

        UUID uuid = UUID.randomUUID();
        String token = uuid.toString();

        System.out.println("New login: " + args.get("username") + " " + token);

        writeSocket(socket, "0 " + token);
        Matchmaking.getMatchmaking().addPlayer(new Player(socket, token));
    }

    public void register() {
        //TODO
    }

    private boolean checkUser(String username, String password) {
        String userPassword = getUserPassword(username);
        if (userPassword == null) {
            return false;
        }
        return userPassword.equals(password);
    }

    private String getUserPassword(String username) {
        String fileName = "src/server/users.txt";
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
            System.out.println("File not found: " + fileName);
        }
        return null;
    }
}