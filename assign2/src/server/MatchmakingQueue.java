package server;

import server.game.AGame;
import server.game.Game;
import server.game.User;
import server.store.Store;

import java.util.*;

/**
 * Defines matchmaking logic. This class is a singleton that contains the current pool of queued players.
 */
public class MatchmakingQueue {
    private static MatchmakingQueue instance;
    private List<User> casualQueue;
    private Queue<User> rankedQueue;
    private Map<String, Date> rankedQueueTimes;
    private final int timeFactor = 8;
    private final int timeBaseline = 100;

    public MatchmakingQueue() {
        casualQueue = new ArrayList<>();
        rankedQueue = new ArrayDeque<>();
        rankedQueueTimes = new HashMap<>();
    }

    /**
     * Add a player to queue. If the stopping criteria is met, the game will be started.
     * @param user
     */
    public void addCasualPlayer(User user) {
        casualQueue.add(user);
        if (casualQueue.size() == Store.getStore().getTeamSize()) {
            startGame();
        }
    }

    public void addRankedPlayer(User user) {
        rankedQueue.add(user);
        rankedQueueTimes.put(user.getUsername(), new Date());
        matchRanked();
    }

    /**
     * Run matchmaking for ranked games.
     * This is done by iterating a queue of players (this gives priority to players who have been waiting the longest).
     * Then, it will group players into games based on their eligibility to play (elo and time spent waiting).
     */
    public void matchRanked() {
        int teamSize = Store.getStore().getTeamSize();
        if (rankedQueue.size() < teamSize) return;

        Queue<User> temp = new LinkedList<>(rankedQueue);
        ArrayList<ArrayList<User>> games = new ArrayList<>();

        while (!temp.isEmpty()) {
            boolean assigned = false;
            User user = temp.poll();
            for (ArrayList<User> game : games) {
                if (game.size() < teamSize * 2 && compareElo(user, game.get(0))) {
                    game.add(user);
                    assigned = true;
                    break;
                }
            }
            if (!assigned) {
                ArrayList<User> newGame = new ArrayList<>();
                newGame.add(user);
                games.add(newGame);
            }
        }
        startRankedGames(games);
    }

    /**
     * Get the matchmaking instance. If it does not exist yet, one will be created.
     * @return Matchmaking instance.
     */
    public synchronized static MatchmakingQueue getQueue() {
        if (instance == null) {
            instance = new MatchmakingQueue();
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

    /**
     * After ranked matchmaking, start eligible games.
     * @param gamePlayers Result of matchmaking. List of games, each containing a list of players.
     */
    private void startRankedGames(ArrayList<ArrayList<User>> gamePlayers) {
        ArrayList<Game> games = new ArrayList<>();
        for (ArrayList<User> game : gamePlayers) {
            if (game.size() < Store.getStore().getTeamSize() * 2) continue;
            Game g = new AGame(game);
            for (User user : game) {
                rankedQueue.remove(user);
            }
            games.add(g);
        }

        Store store = Store.getStore();
        for (Game game : games) {
            store.execute(game);
        }
    }

    /**
     * Compare if two players are suitable to play each other.
     * The distance in skill should be less than a distance criteria, that will relax over time.
     * @param user1 First user.
     * @param user2 Second user.
     * @return Returns true if the users are suitable to play each other and false otherwise.
     */
    private boolean compareElo(User user1, User user2) {
        int dist = Math.abs(user1.getElo() - user2.getElo());
        int time = (int) Math.max((new Date().getTime() - rankedQueueTimes.get(user1.getUsername()).getTime()) / 1000,
                (new Date().getTime() - rankedQueueTimes.get(user2.getUsername()).getTime()) / 1000);
        return dist < time * timeFactor + timeBaseline;
    }
}