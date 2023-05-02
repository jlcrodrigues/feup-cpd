package server.game;

import server.store.SocketWrapper;

public class User {
    private SocketWrapper socket;
    private String token;
    private String username;

    public User(SocketWrapper socket, String token, String username) {
        this.socket = socket;
        this.token = token;
        this.username = username;
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

}