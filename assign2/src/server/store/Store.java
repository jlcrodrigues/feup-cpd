package server.store;

import server.concurrent.ConcurrentHashMap;
import server.game.User;

import java.io.IOException;
import java.util.PropertyPermission;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton class for general storage of shared information.
 */
public class Store {
    private static Store instance;
    private ExecutorService threadPool;
    private int port;
    private Logger logger;
    private ConcurrentHashMap<String, User> users;

    /**
     * Initiates the store and associated data structues.
     */
    private Store() {
        port = 8080;
        int numThreads = Runtime.getRuntime().availableProcessors(); // use one thread per CPU core
        threadPool = Executors.newFixedThreadPool(numThreads);

        initLogger();
        users = new ConcurrentHashMap<>();
    }

    /**
     * Get the store instance. If it does not exist yet, one will be created.
     * @return Store instance.
     */
    public synchronized static Store getStore() {
        if (instance == null) {
            instance = new Store();
        }
        return instance;
    }

    public int getPort() {
        return port;
    }

    /**
     * Fetch a thread from the thread pool and execute a given task.
     * @param task
     */
    public void execute(Runnable task) {
        threadPool.execute(task);
    }

    /**
     * Login a user to memory.
     * @param user User to log in.
     * @return Returns true if the user was logged in and false if they were already on.
     */
    public boolean loginUser(User user) {
        if (users.containsKey(user.getUsername())) return false;
        users.put(user.getUsername(), user);
        return true;
    }

    public void log(Level level, String message) {
        logger.log(level, message);
    }

    private void initLogger() {
        logger = Logger.getLogger("server");
        logger.setUseParentHandlers(false);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new LoggerFormatter());
        logger.addHandler(consoleHandler);

        try {
            FileHandler fileHandler = new FileHandler();
            fileHandler.setFormatter(new LoggerFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}