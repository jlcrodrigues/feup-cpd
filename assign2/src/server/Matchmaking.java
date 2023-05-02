package server;

import server.game.AGame;
import server.game.Game;
import server.game.User;
import server.store.Store;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines matchmaking logic. This class is a singleton that contains the current pool of queued players.
 */
public class Matchmaking {
    private static Matchmaking instance;
    private List<User> users;

    public Matchmaking() {
        users = new ArrayList<>();
    }

    /**
     * Add a player to queue. If the stopping criteria is met, the game will be started.
     * @param user
     */
    public void addPlayer(User user) {
        users.add(user);
        if (users.size() == 2) {
            startGame();
        }
    }

    /**
     * Get the matchmaking instance. If it does not exist yet, one will be created.
     * @return Matchmaking instance.
     */
    public synchronized static Matchmaking getMatchmaking() {
        if (instance == null) {
            instance = new Matchmaking();
        }
        return instance;
    }

    /**
     * Create a new game task. Removes users from queue.
     */
    private void startGame() {
        Game game = new AGame(new ArrayList<>(users));
        users.clear();
        Store store = Store.getStore();
        store.execute(game);
    }
}