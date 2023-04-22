package server;

import server.game.Player;

import java.net.Socket;

public class Auth extends ConnectionHandler {
    private Socket socket;

    public Auth(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        writeSocket(socket, "Waiting in queue!");
        Matchmaking.getMatchmaking().addPlayer(new Player(socket));
    }

    public static void login() {

    }

    public static void register() {

    }

    public static void logout() {

    }

    // get player from storage
    public static void getPlayer() {

    }

}