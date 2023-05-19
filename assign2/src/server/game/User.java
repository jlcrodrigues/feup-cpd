package server.game;

import server.store.SocketWrapper;

import java.util.HashMap;
import java.util.Map;

public class User {
    private SocketWrapper socket;
    private String token;
    private String username;
    private int elo;
    private String state;
    private Game activeGame;

    public User(SocketWrapper socket, String username, int elo) {
        this.socket = socket;
        this.username = username;
        this.elo = elo;
        this.activeGame = null;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public SocketWrapper getSocket() {
        return socket;
    }

    public void setSocket(SocketWrapper socket) {
        this.socket = socket;
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

    public void setElo(int elo) {
        this.elo = elo;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setActiveGame(Game game) {
        this.activeGame = game;
    }

    public Game getActiveGame() {
        return activeGame;
    }

    public Map<String, Object> toMap() {
        return new HashMap<>() {{
            put("username", username);
            put("token", token);
            put("elo", elo);
            put("state", state == null ? "none" : state);
        }};
    }
}