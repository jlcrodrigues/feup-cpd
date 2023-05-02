package server;

import server.game.AGame;
import server.game.Game;
import server.game.User;
import server.store.Store;

import java.util.ArrayList;
import java.util.List;

public class Matchmaking {
    private static Matchmaking instance;
    private List<User> users;

    public Matchmaking() {
        users = new ArrayList<>();
    }

    public void addPlayer(User user) {
        users.add(user);
        if (users.size() == 2) {
            startGame();
        }
    }

    public synchronized static Matchmaking getMatchmaking() {
        if (instance == null) {
            instance = new Matchmaking();
        }
        return instance;
    }

    private void startGame() {
        Game game = new AGame(new ArrayList<>(users));
        users.clear();
        Store store = Store.getStore();
        store.execute(game);
    }
}