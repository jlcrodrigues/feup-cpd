package server.game;

import server.ConnectionHandler;

import java.util.List;

public abstract class Game extends ConnectionHandler {
    protected List<Player> players;

    public Game(List<Player> players) {
        this.players = players;
    }
}
