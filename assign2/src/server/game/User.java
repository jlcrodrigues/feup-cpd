package server.game;

import server.store.SocketWrapper;
import server.store.Store;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class User {
    private SocketWrapper socket;
    private final ReentrantLock socketLock;
    private String token;
    private final String username;
    private int elo;
    private String state;
    private Game activeGame;

    public User(SocketWrapper socket, String username, int elo) {
        this.socket = socket;
        this.socketLock = new ReentrantLock();
        this.username = username;
        this.elo = elo;
        this.state = "none";
        this.activeGame = null;
    }

    public synchronized void setToken(String token) {
        this.token = token;
    }

    public boolean hasInput() {
        socketLock.lock();
        try {
            return socket.hasInput();
        }
        finally {
            socketLock.unlock();
        }
    }

    public void registerAsIdle() {
        socketLock.lock();
        try {
            Store.getStore().registerIdleSocket(socket);
        }
        finally {
            socketLock.unlock();
        }
    }

    public SocketWrapper getSocket() {
        socketLock.lock();
        try {
            return socket;
        }
        finally {
            socketLock.unlock();
        }
    }

    public void setSocket(SocketWrapper socket) {
        socketLock.lock();
        try {
            this.socket = socket;
        }
        finally {
            socketLock.unlock();
        }
    }

    public String readLine() {
        socketLock.lock();
        try {
            return socket.readLine();
        }
        finally {
            socketLock.unlock();
        }
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