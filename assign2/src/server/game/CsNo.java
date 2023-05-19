package server.game;


import server.store.Store;

import java.util.*;
import java.util.logging.Level;

/**
 * Defines a cs:no game.
 * The basic idea is that players will choose spots to both hide and shoot.
 * In the end, the teams that guesses most spots wins.
 */
public class CsNo extends Game {
    private List<User> team1;
    private List<User> team2;

    private final int nrOfSpots;

    private List<List<String>> spotsTeam1;
    private List<List<String>> spotsTeam2;

    private Map<String, Object> shots;
    private Map<User, String> input;

    public CsNo(List<User> users, boolean isRanked) {
        super(users, isRanked);

        nrOfSpots = users.size() / 2 + 2;

        initTeams();

        initSpots(spotsTeam1, users.size() / 2);
        initSpots(spotsTeam2, users.size() / 2);
    }

    @Override
    public void run() {
        Store.getStore().log(Level.INFO, (isRanked ? "Ranked" : "Casual") + " game #" + this.hashCode() + " started");

        // send team members and elo
        sendTeams();

        // process input from every user
        readFromUsers();

        // play the game and check the winner
        processGame();
        String winner = checkWinner();

        updateElo(winner);

        // send the game info to each user to be displayed there
        sendGameInfo(winner);

        finish();
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

        shots = new HashMap<>();
        input = new HashMap<>();
        spotsTeam1 = new ArrayList<>();
        spotsTeam2 = new ArrayList<>();

    }

    /**
     * Send teams list to all the players in the game.
     */
    private void sendTeams() {
        String teams = mapTeams();

        for (User user : users) {
            user.writeLine(teams);
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

    private String mapTeams() {
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

    private void sendGameInfo(String winner) {
        Map<String, Object> team1SpotsMap = new HashMap<>();
        Map<String, Object> team2SpotsMap = new HashMap<>();

        for (int i = 0; i < nrOfSpots; i++) {
            team1SpotsMap.put(String.valueOf(i + 1), getTargetNames(spotsTeam1.get(i)));
        }
        for (int i = 0; i < nrOfSpots; i++) {
            team2SpotsMap.put(String.valueOf(i + 1), getTargetNames(spotsTeam2.get(i)));
        }

        for (User user : users) {
            user.writeLine(mapToJsonString(team1SpotsMap) + ";"
                    + mapToJsonString(team2SpotsMap) + ";"
                    + mapToJsonString(shots) + ";"
                    + mapToJsonString(Map.of("winner", winner)) + ";"
                    + mapTeams()
            );
        }
    }

    /**
     * Read input from every user. <br>
     */
    private void readFromUsers() {
        Set<User> userSet = new HashSet<>();
        userSet.addAll(team1);
        userSet.addAll(team2);

        // users can change sockets mid-game
        while (userSet.size() > 0) {
            for (User user : userSet) {
                if (user.getSocket().hasInput()) {
                    readFromUser(user);
                    userSet.remove(user);
                    break;
                }
            }
        }
    }

    /**
     * Read a user's choices.
     */
    private void readFromUser(User user) {
        String module = user.readLine().toLowerCase();
        if (module.equals("game")) {
            String type = user.readLine().toLowerCase();
            if (type.equals("choice")) {
                getInput(user);
                return;
            }
        }
        user.writeLine("1 Invalid command");
    }

    private void getInput(User user) {
        String argsString = user.readLine();
        Map<String, Object> args = jsonStringToMap(argsString);

        // check if the arguments are valid
        if (!args.containsKey("spot") || !args.containsKey("shot")) {
            user.writeLine("1 Invalid arguments");
            return;
        }

        int spot = Integer.parseInt(args.get("spot").toString());
        int shot = Integer.parseInt(args.get("shot").toString());

        // store the input from each user to be processed later
        Map<String, Object> playerChoices = new HashMap<>();
        playerChoices.put("spot", spot);
        playerChoices.put("shot", shot);
        input.put(user, mapToJsonString(playerChoices));

        // send the confirmation to the user
        user.writeLine("0");
    }

    private void processGame() {
        // put each user in a spot
        for (User user : input.keySet()) {
            Map<String, Object> playerMap = jsonStringToMap(input.get(user));
            int spot = (int) playerMap.get("spot");
            updateSpot(getSpots(user).get(spot - 1), user.getUsername());
        }

        // process the shots
        for (User user : input.keySet()) {
            Map<String, Object> playerMap = jsonStringToMap(input.get(user));
            int shot = (int) playerMap.get("shot");
            List<List<String>> opponentSpots = getOpponentSpots(user);

            if (!spotTaken(opponentSpots.get(shot - 1))) {
                shots.put(user.getUsername(), "");  // missed
            } else {
                String targetName = getTargetNames(opponentSpots.get(shot - 1));
                shots.put(user.getUsername(), targetName); // hit
                markSpotAsDead(opponentSpots.get(shot - 1));
            }
        }
    }

    private void updateSpot(List<String> spot, String username) {
        if (spotTaken(spot)) {
            addPlayerToSpot(spot, username);
        } else {
            spot.set(0, "alive");
            spot.set(1, username);
        }
    }

    private String getTargetNames(List<String> spot) {
        StringBuilder targetNames = new StringBuilder();
        for (String player : spot) {
            if (player.equals("alive") || player.equals("dead")) {
                continue;
            }
            if (player.equals("")) {
                break;
            }
            targetNames.append(player).append(" + ");
        }
        if (targetNames.length() > 0)
            targetNames.delete(targetNames.length() - 3, targetNames.length());

        return targetNames.toString();
    }

    private void markSpotAsDead(List<String> spot) {
        spot.set(0, "dead");
    }

    private boolean spotTaken(List<String> spot) {
        return !spot.get(0).equals("");
    }

    private void addPlayerToSpot(List<String> spot, String username) {
        for (String player : spot) {
            if (player.equals("")) {
                spot.set(spot.indexOf(player), username);
                break;
            }
        }
    }

    private int getSpotNrOfPlayers(List<String> spot) {
        int nrOfPlayers = 0;
        for (String player : spot) {
            if (player.equals("alive") || player.equals("dead")) {
                continue;
            }
            if (player.equals("")) {
                break;
            }
            nrOfPlayers++;
        }
        return nrOfPlayers;
    }

    private List<List<String>> getSpots(User user) {
        if (team1.contains(user)) {
            return spotsTeam1;
        }
        return spotsTeam2;
    }

    private List<List<String>> getOpponentSpots(User user) {
        if (team1.contains(user)) {
            return spotsTeam2;
        }
        return spotsTeam1;
    }


    // check who has less dead spots
    private String checkWinner() {
        int team1Dead = teamNrOfDeaths(spotsTeam1);
        int team2Dead = teamNrOfDeaths(spotsTeam2);
        if (team1Dead < team2Dead) {
            return "TERRORISTS";
        } else if (team2Dead < team1Dead) {
            return "COUNTER-TERRORISTS";
        } else {
            return "draw";
        }
    }

    private int teamNrOfDeaths(List<List<String>> spotsTeam) {
        int deads = 0;

        for (List<String> spot : spotsTeam) {
            if (spot.get(0).equals("dead")) {
                int spotPlayers = getSpotNrOfPlayers(spot);
                deads += spotPlayers;
            }
        }

        return deads;
    }

    private void initSpots(List<List<String>> spots, int maxPerSpot) {
        for (int i = 0; i < nrOfSpots; i++) {
            List<String> spot = new ArrayList<>();
            for (int j = 0; j < maxPerSpot + 1; j++) {
                spot.add("");
            }
            spots.add(spot);
        }
    }

    private void updateElo(String winner) {
        Map<String, Integer> elo = new HashMap<>();
        if (!isRanked) return;
        int average1 = (int) team1.stream().mapToInt(User::getElo)
                .average().orElse(0);
        int average2 = (int) team2.stream().mapToInt(User::getElo)
                .average().orElse(0);
        double result = (winner.equals("TERRORISTS") ? 1 : (winner.equals("COUNTER-TERRORISTS") ? 0 : 0.5));
        updateTeamElo(elo, team1, average2, result);
        updateTeamElo(elo, team2, average1, 1 - result);
        Store.getStore().getDatabase().updateElo(elo);
    }

    private void updateTeamElo(Map<String, Integer> elo, List<User> team, int opponentAverage, double result) {
        for (User user : team) {
            double winProbability =  1.0  / (1 + Math.pow(10, (double) (opponentAverage - user.getElo()) / 400));
            int newElo = (int) (user.getElo() + 32 * (result - winProbability));
            user.setElo(newElo);
            elo.put(user.getUsername(), newElo);
        }
    }
}


