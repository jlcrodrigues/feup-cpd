package server.game;

import server.ConnectionHandler;
import server.store.Store;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;

public abstract class Game extends ConnectionHandler {
    protected List<User> users;
    protected Selector selector;
    protected Set<User> disconnected;
    protected boolean isRanked;

    protected List<User> team1;
    protected List<User> team2;

    public Game(List<User> users, boolean isRanked) {
        this.users = users;
        this.isRanked = isRanked;
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }

        initTeams();
    }

    protected void finish() {
        Store store = Store.getStore();
        for (User user : users) {
            user.setState("none");
            user.registerAsIdle();
        }
    }

    /**
     * Divide players into approximately equal teams using a simple greedy approach.
     */
    private void initTeams() {
        users.sort(Comparator.comparingInt(User::getElo));
        team1 = new ArrayList<>();
        team2 = new ArrayList<>();
        for (int i = 0; i < users.size(); i += 2) {
            team1.add(users.get(i));
            team2.add(users.get(i + 1));
        }
    }

    /**
     * Send teams list to a certain player.
     *
     * @param user User to send the teams to
     */
    public void sendTeams(User user) {
        String teams = mapTeams();
        user.writeLine(teams);
    }

    public void rejoin(User user) {
        SocketChannel channel = user.getSocket().getSocket().getChannel();
        try {
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
            selector.wakeup();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String mapTeams() {
        Map<String, Object> team1Map = new HashMap<>();
        Map<String, Object> team2Map = new HashMap<>();
        for (User user : team1) {
            team1Map.put(user.getUsername(), user.getElo());
        }

        for (User user : team2) {
            team2Map.put(user.getUsername(), user.getElo());
        }
        return mapToJsonString(team1Map) + ";" + mapToJsonString(team2Map);
    }

    /**
     * Update the elo of each player in the game according to the result. <br>
     * The formula used is in accordance with the elo rating system.
     * @param winner 1 if team1 won, 2 if team2 won, 0 if it was a draw
     */
    protected void updateElo(int winner) {
        Map<String, Integer> elo = new HashMap<>();
        if (!isRanked) return;
        int average1 = (int) team1.stream().mapToInt(User::getElo)
                .average().orElse(0);
        int average2 = (int) team2.stream().mapToInt(User::getElo)
                .average().orElse(0);
        double result = (winner == 1 ? 1 : (winner == 2 ? 0 : 0.5));
        updateTeamElo(elo, team1, average2, result);
        updateTeamElo(elo, team2, average1, 1 - result);
        Store.getStore().getDatabase().updateElo(elo);
    }

    private void updateTeamElo(Map<String, Integer> elo, List<User> team, int opponentAverage, double result) {
        for (User user : team) {
            // disconnected players cannot gain elo
            if (disconnected.contains(user) && result >= 0.5) continue;

            double winProbability =  1.0  / (1 + Math.pow(10, (double) (opponentAverage - user.getElo()) / 400));
            int k = Store.getStore().getProperty("eloFactor");
            int newElo = (int) (user.getElo() + k * (result - winProbability));
            user.setElo(newElo);
            elo.put(user.getUsername(), newElo);
        }
    }

    protected void startSelector() throws IOException {
        for (User user : users) {
            SocketChannel channel = user.getSocket().getSocket().getChannel();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
        }
    }

    protected void closeSelector() throws IOException {
        for (SelectionKey key : selector.keys()) {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            key.cancel();
            socketChannel.configureBlocking(true);
        }
        selector.selectedKeys().clear();
        selector.close();
    }
}
