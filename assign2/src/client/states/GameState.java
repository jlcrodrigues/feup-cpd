package client.states;

import client.Session;
import server.store.Store;

import java.util.*;

import static client.Session.jsonStringToMap;

/**
 * Defines the actual game playing.
 */
public class GameState implements State {

    List<List<String>> team1 = new ArrayList<>();
    List<List<String>> team1Spots = new ArrayList<>();
    List<List<String>> team2 = new ArrayList<>();

    List<List<String>> team2Spots = new ArrayList<>();

    Map<String,Object> shotsMap = new HashMap<>();

    @Override
    public State step() {
        System.out.println("Waiting in queue");
        Session session = Session.getSession();

        // display the teams and each player elo
        String teams = session.readLine();
        createTeams(teams);
        breakLn();
        System.out.println("Game started\n");
        printTeams();

        // get the input from the user
        String spot = getInput("Choose a spot to camp");
        String shot = getInput("Choose a spot to shoot");
        System.out.println("Waiting for other players to make their actions");

        // send the input to the server
        Map<String,Object> args = new HashMap<>();
        args.put("spot",spot);
        args.put("shot",shot);
        session.writeMessage("game","choice",args);
        session.readResponse();

        // get the round info from the server
        String round = session.readLine();
        breakLn();

        // display the starting positions
        createTeamSpots(round);

        // display the shots taken in the round
        processShots(round);
        System.out.println("ROUND HISTORY:");
        printShots();

        // display the final positions after the shots were taken
        System.out.println("Round final positions:\n");
        printTeamSpots();

        // display the winner of the round
        displayWinner(round);

        System.out.println("Press enter to continue");
        session.getScanner().nextLine();
        return new LobbyState();
    }

    private String getInput(String message) {
        Session session = Session.getSession();
        int nrSpots = Store.getStore().getTeamSize() + 2;

        System.out.println(message + " (1-" + nrSpots + ")");
        String input = session.getScanner().nextLine();

        while (!(input.matches("[1-" + nrSpots + "]"))) {
            System.out.println("Invalid input.\n" + message + " (1-" + nrSpots + ")");
            input = session.getScanner().nextLine();
        }

        return input;
    }

    private void createTeams (String teams) {
        String team1 = teams.split(";")[0];
        String team2 = teams.split(";")[1];
        createTeam(team1, this.team1);
        createTeam(team2, this.team2);
    }

    private void createTeam(String team, List<List<String>> teamList) {
        Map<String,Object> teamMap = jsonStringToMap(team);
        for (String user : teamMap.keySet()) {
            String elo = teamMap.get(user).toString();
            List <String> player = new ArrayList<>();
            player.add(user);
            player.add(elo);
            teamList.add(player);
        }
    }

    private void createTeamSpots(String teamSpots){
        String team1Spots = teamSpots.split(";")[0];
        String team2Spots = teamSpots.split(";")[1];
        createTeamSpot(team1Spots, this.team1Spots);
        createTeamSpot(team2Spots, this.team2Spots);
    }

    private void createTeamSpot(String teamSpot, List<List<String>> teamSpotList){
        Map<String,Object> teamSpotMap = jsonStringToMap(teamSpot);
        for (String spot : teamSpotMap.keySet()) {
            String users = teamSpotMap.get(spot).toString();
            List <String> player = new ArrayList<>();
            player.add("alive");
            player.add(users);
            teamSpotList.add(player);
        }
    }

    private String formatTeamSpots(List<List<String>> spots){
        StringBuilder teamString = new StringBuilder();
        for (List<String> player : spots) {
            // string with 30 chars max of name of player
            int fixedSize = 30;

            String name = player.get(1);
            if (player.get(0).equals("dead")) {
                name+="(DEAD)";
            }
            int padding = fixedSize - name.length();
            int leftPadding = padding / 2;
            int rightPadding = padding - leftPadding;
            String username = String.format("%" + leftPadding + "s%s%" + rightPadding + "s", "", name, "");
            teamString.append(username).append("     ");
        }
        teamString.append("\n");
        teamString.append("______________________________     ".repeat(spots.size()));
        teamString.append("\n");
        return teamString.toString();
    }

    private void printTeamSpots(){
        System.out.println("Terrorists:");
        System.out.println(formatTeamSpots(this.team1Spots));

        System.out.println("Counter-Terrorists:");
        System.out.println(formatTeamSpots(this.team2Spots));
    }


    private void printTeams () {
        int maxTeamSize = Math.max(team1.size(), team2.size());
        int teamNameWidth = 20; // Width of team name column

        String terroristTeamName = "Terrorists";
        String counterTerroristTeamName = "Counter-Terrorists";

        // Center the team names within their column
        printSpot(teamNameWidth, terroristTeamName, counterTerroristTeamName);
        System.out.printf("%s | %s%n", "-".repeat(teamNameWidth), "-".repeat(teamNameWidth));

        for (int i = 0; i < maxTeamSize; i++) {
            String terroristName = i < team1.size() ? team1.get(i).get(0) + " (" + team1.get(i).get(1) + ")" : "";
            String counterTerroristName = i < team2.size() ? team2.get(i).get(0) + " (" + team2.get(i).get(1) + ")" : "";

            // Center the player names within their columns
            printSpot(teamNameWidth, terroristName, counterTerroristName);
        }
        System.out.println("\n");
    }

    private void printSpot(int teamNameWidth, String terroristTeamName, String counterTerroristTeamName) {
        terroristTeamName = String.format("%-" + (teamNameWidth - terroristTeamName.length()) / 2 + "s%s%-" + (teamNameWidth - terroristTeamName.length()) / 2 + "s", "", terroristTeamName, "");
        counterTerroristTeamName = String.format("%-" + (teamNameWidth - counterTerroristTeamName.length()) / 2 + "s%s%-" + (teamNameWidth - counterTerroristTeamName.length()) / 2 + "s", "", counterTerroristTeamName, "");

        System.out.printf("%s | %s%n", terroristTeamName, counterTerroristTeamName);
    }

    private void processShots(String round){
        String shots = round.split(";")[2];
        this.shotsMap = jsonStringToMap(shots);

        for (String shot : this.shotsMap.keySet()) {
            String user = this.shotsMap.get(shot).toString();
            if (Objects.equals(user, "")){
                continue;
            }
            for (List<String> player : this.team1Spots) {
                if (Objects.equals(player.get(1), user)){
                    player.set(0, "dead");
                }
            }
            for (List<String> player : this.team2Spots) {
                if (Objects.equals(player.get(1), user)){
                    player.set(0, "dead");
                }
            }
        }

    }

    private void printShots (){
        for (String shot : this.shotsMap.keySet()) {
            String user = this.shotsMap.get(shot).toString();
            if (Objects.equals(user, "")){
                System.out.println(shot + " missed!");
            }
            else{
                Random r = new Random();
                System.out.println(shot + " shot " + user
                        + (r.nextDouble() < 0.2 ?" right in the face!" : "!"));
            }
        }
        System.out.println("\n");
    }

    public void displayWinner(String round) {
        String winner = round.split(";")[3];
        Map<String,Object> winnerMap = jsonStringToMap(winner);
        String winnerTeam = winnerMap.get("winner").toString();
        if (Objects.equals(winnerTeam, "draw")){
            System.out.println("ROUND DRAW!");
        }
        else{
            System.out.println(winnerTeam + " WIN!");
        }
    }
}
