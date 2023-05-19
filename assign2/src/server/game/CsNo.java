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
    private final int nrOfSpots;

    private List<List<String>> spotsTeam1;
    private List<List<String>> spotsTeam2;

    private Map<String, Object> shots;
    private Map<User, String> input;

    public CsNo(List<User> users, boolean isRanked) {
        super(users, isRanked);

        nrOfSpots = users.size() / 2 + 2;

        shots = new HashMap<>();
        input = new HashMap<>();
        spotsTeam1 = new ArrayList<>();
        spotsTeam2 = new ArrayList<>();

        initSpots(spotsTeam1, users.size() / 2);
        initSpots(spotsTeam2, users.size() / 2);
    }

    @Override
    public void run() {
        Store.getStore().log(Level.INFO, (isRanked ? "Ranked" : "Casual") + " game #" + this.hashCode() + " started");

        // send team members and elo
        sendTeams();

        // process input from every user
        disconnected = readFromUsers();

        // play the game and check the winner
        processGame();
        String winner = checkWinner();

        updateElo(winner.equals("TERRORISTS") ? 1 : (winner.equals("COUNTER-TERRORISTS") ? 2 : 0));

        // send the game info to each user to be displayed there
        sendGameInfo(winner);

        finish();
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
     * Returns a set of users that did not send input in time.
     */
    private Set<User> readFromUsers() {
        Set<User> userSet = new HashSet<>();
        userSet.addAll(team1);
        userSet.addAll(team2);
        // count time
        long startTime = System.currentTimeMillis();
        int maxTime = Store.getStore().getProperty("playerTimeout") * 1000;

        // users can change sockets mid-game
        while (userSet.size() > 0) {
            if (System.currentTimeMillis() - startTime > maxTime) {
                for (User user : userSet) {
                    user.writeLine("1 Time limit exceeded");
                }
                return userSet;
            }
            for (User user : userSet) {
                if (user.getSocket().hasInput()) {
                    readFromUser(user);
                    userSet.remove(user);
                    break;
                }
            }
        }
        return userSet;
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

    /**
     * Check which team has fewer deaths.
     */
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
}
