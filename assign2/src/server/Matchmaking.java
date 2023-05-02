package server;

import server.game.AGame;
import server.game.Game;
import server.game.Player;
import server.store.Store;

import java.util.ArrayList;
import java.util.List;

public class Matchmaking {
    private static Matchmaking instance;
    private List<Player> players;

    public Matchmaking() {
        players = new ArrayList<>();
    }

    public void addPlayer(Player player) {
        players.add(player);
        if (players.size() == 2) {
            startGame();
        }
    }

    public static Matchmaking getMatchmaking() {
        if (instance == null) {
            instance = new Matchmaking();
        }
        return instance;
    }

    private void startGame() {
        Game game = new AGame(new ArrayList<>(players));
        players.clear();
        Store store = Store.getStore();
        store.execute(game);
    }
}