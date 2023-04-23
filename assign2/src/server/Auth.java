package server;

import server.game.Player;

import java.net.Socket;
import java.util.Map;

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
                writeSocket(socket, "1\nInvalid command");
        }
    }

    public void login() {
        String argsString = readSocketLine(socket);
        Map<String, Object> args = jsonStringToMap(argsString);
        System.out.println("New login: " + args.get("username"));

        writeSocket(socket, "Waiting in queue!");
        Matchmaking.getMatchmaking().addPlayer(new Player(socket));
    }

    public void register() {
        //TODO
    }

}