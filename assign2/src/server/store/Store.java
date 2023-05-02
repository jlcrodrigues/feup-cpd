package server.store;

import server.concurrent.ConcurrentHashMap;
import server.game.User;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Store {
    private static Store instance;
    private ExecutorService threadPool;
    private int port;
    private Logger logger;
    private ConcurrentHashMap<String, User> users;

    private Store() {
        port = 8080;
        int numThreads = Runtime.getRuntime().availableProcessors(); // use one thread per CPU core
        threadPool = Executors.newFixedThreadPool(numThreads);

        initLogger();
        users = new ConcurrentHashMap<>();
    }

    public synchronized static Store getStore() {
        if (instance == null) {
            instance = new Store();
        }
        return instance;
    }

    public int getPort() {
        return port;
    }

    public void execute(Runnable task) {
        threadPool.execute(task);
    }

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