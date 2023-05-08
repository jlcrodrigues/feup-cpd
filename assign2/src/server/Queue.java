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
public class Queue {
    private static Queue instance;
    private List<User> casualQueue;
    private List<User> rankedQueue;

    public Queue() {
        casualQueue = new ArrayList<>();
        rankedQueue = new ArrayList<>();
    }

    /**
     * Add a player to queue. If the stopping criteria is met, the game will be started.
     * @param user
     */
    public void addCasualPlayer(User user) {
        casualQueue.add(user);
        if (casualQueue.size() == 2) {
            startGame();
        }
    }

    /**
     * Get the matchmaking instance. If it does not exist yet, one will be created.
     * @return Matchmaking instance.
     */
    public synchronized static Queue getQueue() {
        if (instance == null) {
            instance = new Queue();
        }
        return instance;
    }

    /**
     * Create a new game task. Removes users from queue.
     */
    private void startGame() {
        Game game = new AGame(new ArrayList<>(casualQueue));
        casualQueue.clear();
        Store store = Store.getStore();
        store.execute(game);
    }
}