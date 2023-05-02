package server.game;

import server.store.SocketWrapper;

public class Player {
    private SocketWrapper socket;
    private String token;

    public Player(SocketWrapper socket, String token) {
        this.socket = socket;
        this.token = token;
    }

    public String readLine() {
        return socket.readLine();
    }

    public void writeLine(String message) {
        socket.writeLine(message);
    }

}