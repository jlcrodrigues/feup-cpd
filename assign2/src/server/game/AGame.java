package server.game;

import server.store.Store;

import java.util.List;
import java.util.logging.Level;

public class AGame extends Game {

    public AGame(List<User> users) {
        super(users);
    }

    @Override
    public void run() {
        Store.getStore().log(Level.INFO, "Game started with " + users.size() + " players.");
        for (User user : users) {
            System.out.println(user.getState());
            user.writeLine("Playing the game!");
        }

        finish();
    }
}
