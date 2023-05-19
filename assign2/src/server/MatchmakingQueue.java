package server;

import server.concurrent.MyConcurrentArrayDeque;
import server.game.CsNo;
import server.game.Game;
import server.game.User;
import server.store.Store;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Defines matchmaking logic. This class is a singleton that contains the current pool of queued players.
 */
public class MatchmakingQueue {
    private static MatchmakingQueue instance;
    private final MyConcurrentArrayDeque<User> casualQueue; // casual collection behaves as a list
    private final MyConcurrentArrayDeque<User> rankedQueue; // ranked collection behaves as a queue
    private final ReentrantLock casualQueueLock;
    private final ReentrantLock rankedQueueLock;
    private final Map<String, Date> rankedQueueTimes;

    private final int timeFactor;
    private final int timeBaseline;

    public MatchmakingQueue() {
        casualQueue = new MyConcurrentArrayDeque<>();
        rankedQueue = new MyConcurrentArrayDeque<>();
        casualQueueLock = new ReentrantLock();
        rankedQueueLock = new ReentrantLock();
        rankedQueueTimes = new ConcurrentHashMap<>();

        Store store = Store.getStore();
        timeFactor = store.getProperty("timeFactor");
        timeBaseline = store.getProperty("timeBaseline");
    }

    /**
     * Get the matchmaking instance. If it does not exist yet, one will be created.
     * @return Matchmaking instance.
     */
    public static MatchmakingQueue getQueue() {
        if (instance == null) {
            synchronized (MatchmakingQueue.class) {
                if (instance == null)
                    instance = new MatchmakingQueue();
            }
        }
        return instance;
    }

    /////// casual games ///////

    /**
     * Add a player to queue. If the stopping criteria is met, the game will be started.
     * @param user
     */
    public void addCasualPlayer(User user) {
        casualQueueLock.lock();
        try {
            user.setState("queue");
            casualQueue.addLast(user);
            if (casualQueue.size() == Store.getStore().getProperty("teamSize") * 2) {
                startGame();
            }
        }
        finally {
            casualQueueLock.unlock();
        }
    }

    /**
     * Create a new game task. Removes users from queue.
     */
    private void startGame() {
        if (casualQueue.size() < Store.getStore().getProperty("teamSize") * 2) return;
        ArrayList<User> players = new ArrayList<>(casualQueue.getQueue());
        Game game = new CsNo(players, false);
        for (User player : players) {
            player.setState("game");
            player.setActiveGame(game);
        }
        casualQueue.clear();
        Store store = Store.getStore();
        store.execute(game);
    }

    /////// ranked games ///////

    /**
     * Add a player to the ranked queue.
     * The users are kept in a queue (FIFO) and their joining times in a separate map.
     * @param user User to add to queue.
     */
    public void addRankedPlayer(User user) {
        rankedQueueLock.lock();
        try {
            user.setState("queue");
            rankedQueue.addLast(user);
            rankedQueueTimes.put(user.getUsername(), new Date());
            matchRankedUnsafe();
        }
        finally {
            rankedQueueLock.unlock();
        }
    }

    public void matchRanked() {
        rankedQueueLock.lock();
        try {
            matchRankedUnsafe();
        }
        finally {
            rankedQueueLock.unlock();
        }
    }

    /**
     * Run matchmaking for ranked games.
     * This is done by iterating a queue of players (this gives priority to players who have been waiting the longest).
     * Then, it will group players into games based on their eligibility to play (elo and time spent waiting).
     */
    public void matchRankedUnsafe() {
        int teamSize = Store.getStore().getProperty("teamSize");
        if (rankedQueue.size() < teamSize) return;

        Queue<User> temp = new LinkedList<>(rankedQueue.getQueue());
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
     * After ranked matchmaking, start eligible games.
     * @param gamePlayers Result of matchmaking. List of games, each containing a list of players.
     */
    private void startRankedGames(ArrayList<ArrayList<User>> gamePlayers) {
        ArrayList<Game> games = new ArrayList<>();
        for (ArrayList<User> game : gamePlayers) {
            if (game.size() < Store.getStore().getProperty("teamSize") * 2) continue;
            Game g = new CsNo(game, true);
            for (User user : game) {
                user.setState("game");
                user.setActiveGame(g);
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