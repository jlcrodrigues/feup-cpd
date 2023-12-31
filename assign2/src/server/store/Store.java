package server.store;

import server.MatchmakingQueue;
import server.Server;
import server.concurrent.MyConcurrentHashMap;
import server.concurrent.Database;
import server.game.User;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton class for general storage of shared information.
 */
public class Store {
    private static Store instance;
    private final ExecutorService threadPool;
    private final Selector selector;
    private final Properties properties;
    private Logger logger;
    private final MyConcurrentHashMap<String, User> users;
    private final Database database;

    /**
     * Initiates the store and associated data structures.
     */
    private Store() {
        properties = new Properties();
        try (InputStream is = Server.class.getResourceAsStream("application.properties")) {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int numThreads = Runtime.getRuntime().availableProcessors(); // use one thread per CPU core
        threadPool = Executors.newFixedThreadPool(numThreads);

        try {
            selector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        initLogger();
        users = new MyConcurrentHashMap<>();

        setMatchmakingScheduler();
        database = new Database();
    }

    /**
     * Initialize the Store at the start of the program. <br>
     * This way getStore does not need to be synchronized.
     */
    public static void init() {
        if (instance != null) return;
        instance = new Store();
    }

    /**
     * Get the store instance.
     * @return Store instance.
     */
    public static Store getStore() {
        if (instance == null) {
            throw new RuntimeException("Store not initialized");
        }
        return instance;
    }

    public int getProperty(String key) {
        try {
            return Integer.parseInt(properties.getProperty(key));
        }
        catch (NumberFormatException e) {
            Store.getStore().log(Level.SEVERE, "Invalid property: " + key);
            return 0;
        }
    }

    /**
     * Fetch a thread from the thread pool and execute a given task.
     * @param task
     */
    public void execute(Runnable task) {
        threadPool.execute(task);
    }

    public User getUser(String token) {
        User user = users.get(token);
        if (user == null) return null;
        return user;
    }

    public Database getDatabase() {
        return database;
    }

    /**
     * Login a user to memory.
     * @param user User to log in.
     * @return Returns true if the user was logged in and false if they were already on.
     */
    public boolean loginUser(User user) {
        for (User u : users.values()) {
            if (u.getUsername().equals(user.getUsername())) {
                return false;
            }
        }

        users.put(user.getToken(), user);
        return true;
    }

    public void logoutUser(String token) {
        users.remove(token);
    }

    public void log(Level level, String message) {
        logger.log(level, message);
    }

    public Selector getSelector() {
        return selector;
    }

    /**
     * Register a socket to the selector for reading.
     * When a user is idling, their socket will be placed in the selector so that it can be redirected.
     */
    public void registerIdleSocket(SocketWrapper socket) {
        try {
            SocketChannel channel = socket.getSocket().getChannel();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
            selector.wakeup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initLogger() {
        logger = Logger.getLogger("server");
        logger.setUseParentHandlers(false);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new LoggerFormatter());
        logger.addHandler(consoleHandler);

        try {
            FileHandler fileHandler = new FileHandler("logs.log", true);
            fileHandler.setFormatter(new LoggerFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Run ranked matchmaking every x seconds.
     * Because matchmaking will vary through time, it has to run periodically and not only when new players join.
     */
    private void setMatchmakingScheduler() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(() -> threadPool.execute(
                () -> MatchmakingQueue.getQueue().matchRanked()), 0, getProperty("queueRefresh"), TimeUnit.SECONDS);
    }
}