package server.game;

import server.ConnectionHandler;

import java.util.List;

public abstract class Game extends ConnectionHandler {
    protected List<User> users;

    public Game(List<User> users) {
        this.users = users;
    }
}
