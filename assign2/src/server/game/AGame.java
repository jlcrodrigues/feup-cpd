package server.game;

import server.store.Store;

import java.util.*;
import java.util.logging.Level;

public class AGame extends Game {

    // 2 teams and divide users per team
    List<User> team1;
    List<User> team2;

    int nrOfSpots;

    List<String> spotsTeam1;

    List<String> spotsTeam2;

    Map<User,String> shots;


    public AGame(List<User> users) {
        super(users);
        nrOfSpots = users.size()/2 + 2;

        // divide users into 2 teams
        team1 = users.subList(0, users.size() / 2);
        team2 = users.subList(users.size() / 2, users.size());

        spotsTeam1 = new ArrayList<>(Collections.nCopies(nrOfSpots, ""));
        spotsTeam2 = new ArrayList<>(Collections.nCopies(nrOfSpots, ""));

        shots = new HashMap<>();

    }

    @Override
    public void run() {
        Store.getStore().log(Level.INFO, "Game started with " + users.size() + " players.");
        for (User user : team1){
            user.writeLine("You are in team 1");
        }
        for (User user : team2){
            user.writeLine("You are in team 2");
        }
        for (User user : users) {
            readFromUser(user);
        }

        printTeamSpots();

        for (User user : users) {
            user.writeLine("Spots chosen! Round started!");
        }

        for (User user : users) {
            readFromUser(user);
        }

        for (User user : shots.keySet()){
            Store.getStore().log(Level.INFO, user.getUsername() + " : " + shots.get(user));
            user.writeLine(shots.get(user));
        }

        printTeamSpots();

        List<User> winner = checkWinner();

        if (winner == null){
            for (User user : users) {
                user.writeLine("Round draw!");
            }
        }
        else {
            for (User user : winner) {
                user.writeLine("You won!");
            }
            for (User user : users) {
                if (!winner.contains(user)){
                    user.writeLine("You lost!");
                }
            }
        }


        Store.getStore().log(Level.INFO, "Game finished.");
        finish();
    }

    private void readFromUser(User user) {
       String module = user.readLine().toLowerCase();
        if (module.equals("game")) {
            game(user);
        } else {
            user.writeLine("1 Invalid command");
        }
    }

    private void game(User user) {
        String type = user.readLine().toLowerCase();
        switch (type) {
            case "choosespot":
                chooseSpot(user);
                break;
            case "takeshot":
                takeShot(user);
                break;
            default:
                user.writeLine("1 Invalid command");
        }
    }

    private void chooseSpot(User user) {
        String argsString = user.readLine();
        Map<String,Object> args = jsonStringToMap(argsString);
        if (!args.containsKey("spot")) {
            user.writeLine("1 Missing spot");
            return;
        }
        int spot = Integer.parseInt(args.get("spot").toString());
        getSpots(user).set(spot-1, user.getUsername());
        Store.getStore().log(Level.INFO, "User " + user.getUsername() + " chose spot " + spot);
        user.writeLine("0");
    }

    private void takeShot(User user) {
        String argsString = user.readLine();
        Map<String,Object> args = jsonStringToMap(argsString);
        if (!args.containsKey("spot")) {
            user.writeLine("1 Missing spot");
            return;
        }
        int spot = Integer.parseInt(args.get("spot").toString());
        List<User> opponentTeam = getOpponentTeam(user);
        List<String> opponentSpots = getOpponentSpots(user);
        String targetName = opponentSpots.get(spot-1);
        User target = opponentTeam.stream().filter(u -> u.getUsername().equals(targetName)).findFirst().orElse(null);
        if (target == null) {
            shots.put(user, "you missed");
        }
        else {
            shots.put(user, "you hit " + target.getUsername());
            //shots.put(target, "you were shot by " + user.getUsername());
            opponentSpots.set(spot-1, "dead");
        }

        user.writeLine("0");
    }

    private List<User> getTeam(User user){
        if (team1.contains(user)){
            return team1;
        }
        return team2;
    }

    private List<String> getSpots(User user){
        if (team1.contains(user)){
            return spotsTeam1;
        }
        return spotsTeam2;
    }

    private List<User> getOpponentTeam(User user){
        if (team1.contains(user)){
            return team2;
        }
        return team1;
    }

    private List<String> getOpponentSpots(User user){
        if (team1.contains(user)){
            return spotsTeam2;
        }
        return spotsTeam1;
    }

    private List<String> formatTeamSpots(List<String> spots){
        List<String> teamSpots = new ArrayList<>();
        StringBuilder teamString = new StringBuilder();
        for (String s : spots) {
            // string with 10 chars max of name of player
            int fixedSize = 10;
            int padding = fixedSize - s.length();
            int leftPadding = padding / 2;
            int rightPadding = padding - leftPadding;
            String player = String.format("%" + leftPadding + "s%s%" + rightPadding + "s", "", s, "");
            teamString.append(player).append("     ");
        }
        teamSpots.add(teamString.toString());
        teamString.setLength(0);
        teamString.append("__________     ".repeat(spots.size()));
        teamSpots.add(teamString.toString());
        return teamSpots;
    }

    private void printTeamSpots(){
        Store.getStore().log(Level.INFO, "Team 1:");
        List<String> team1Spots = formatTeamSpots(spotsTeam1);
        Store.getStore().log(Level.INFO, team1Spots.get(0));
        Store.getStore().log(Level.INFO, team1Spots.get(1));

        Store.getStore().log(Level.INFO, "Team 2:");
        List<String> team2Spots = formatTeamSpots(spotsTeam2);
        Store.getStore().log(Level.INFO, team2Spots.get(0));
        Store.getStore().log(Level.INFO, team2Spots.get(1));
    }

    // check who has less dead spots
    private List<User> checkWinner(){
        int team1Dead = Collections.frequency(spotsTeam1, "dead");
        int team2Dead = Collections.frequency(spotsTeam2, "dead");
        if (team1Dead < team2Dead){
            return team1;
        }
        else if (team2Dead < team1Dead){
            return team2;
        }
        else {
            return null;
        }
    }

}


