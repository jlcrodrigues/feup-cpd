package server.game;

import server.ConnectionHandler;
import server.store.Store;

import java.util.List;

public abstract class Game extends ConnectionHandler {
    protected List<User> users;
    protected boolean isRanked;

    public Game(List<User> users, boolean isRanked) {
        this.users = users;
        this.isRanked = isRanked;
    }

    protected void finish() {
        Store store = Store.getStore();
        for (User user : users) {
            user.setState("none");
            store.registerIdleSocket(user.getSocket());
        }
    }

    public abstract void sendTeams(User user);
}
