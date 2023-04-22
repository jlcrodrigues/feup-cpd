package server.game;

import java.net.Socket;

public class Player {
    private Socket socket;

    public Player(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }
}