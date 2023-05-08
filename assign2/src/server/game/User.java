package server.game;

import server.store.SocketWrapper;

public class User {
    private SocketWrapper socket;
    private String token;
    private String username;
    private int elo;

    public User(SocketWrapper socket, String username, int elo) {
        this.socket = socket;
        this.username = username;
        this.elo = elo;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public SocketWrapper getSocket() {
        return socket;
    }

    public String readLine() {
        return socket.readLine();
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public void writeLine(String message) {
        socket.writeLine(message);
    }

    public int getElo() {
        return elo;
    }
}