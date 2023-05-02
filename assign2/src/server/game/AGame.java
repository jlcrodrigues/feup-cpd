package server.game;

import server.store.Store;

import java.util.List;
import java.util.logging.Level;

public class AGame extends Game {

    public AGame(List<Player> players) {
        super(players);
    }

    @Override
    public void run() {
        Store.getStore().log(Level.INFO, "Game started with " + players.size() + " players.");
        for (Player player : players) {
            player.writeLine("Playing the game!");
        }
    }
}
