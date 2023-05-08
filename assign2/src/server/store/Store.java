package server.store;

import server.concurrent.ConcurrentHashMap;
import server.game.User;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
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
    private Selector selector;
    private int port;
    private Logger logger;
    private ConcurrentHashMap<String, User> users;

    /**
     * Initiates the store and associated data structures.
     */
    private Store() {
        port = 8080;
        int numThreads = Runtime.getRuntime().availableProcessors(); // use one thread per CPU core
        threadPool = Executors.newFixedThreadPool(numThreads);

        try {
            selector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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

    public User getUser(String token) {
        User user = users.get(token);
        if (user == null) return null;
        return user;
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

    public void log(Level level, String message) {
        logger.log(level, message);
    }

    public Selector getSelector() {
        return selector;
    }

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
            FileHandler fileHandler = new FileHandler();
            fileHandler.setFormatter(new LoggerFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}