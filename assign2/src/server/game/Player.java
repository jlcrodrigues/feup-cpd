package server.game;

import java.net.Socket;

public class Player {
    private Socket socket;
    private String token;

    public Player(Socket socket, String token) {
        this.socket = socket;
        this.token = token;
    }

    public Socket getSocket() {
        return socket;
    }
}