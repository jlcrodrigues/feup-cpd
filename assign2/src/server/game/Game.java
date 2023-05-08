package server.game;

import server.ConnectionHandler;
import server.store.Store;

import java.util.List;

public abstract class Game extends ConnectionHandler {
    protected List<User> users;

    public Game(List<User> users) {
        this.users = users;
    }

    protected void finish() {
        Store store = Store.getStore();
        for (User user : users) {
            store.registerIdleSocket(user.getSocket());
        }
    }
}
