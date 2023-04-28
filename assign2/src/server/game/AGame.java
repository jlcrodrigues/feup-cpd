package server.game;

import java.util.List;

public class AGame extends Game {

    public AGame(List<Player> players) {
        super(players);
    }

    @Override
    public void run() {
        System.out.println("Game started with " + players.size() + " players.\n");
        for (Player player : players) {
            player.writeLine("Playing the game!");
        }
    }
}
