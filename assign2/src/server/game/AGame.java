package server.game;


import java.util.*;

public class AGame extends Game {

    List<User> team1;
    List<User> team2;

    int nrOfSpots;

    List<List<String>> spotsTeam1;

    List<List<String>> spotsTeam2;

    Map<String,Object> shots;

    Map<User,String> input;


    public AGame(List<User> users) {
        super(users);

        nrOfSpots = users.size()/2 + 2;

        // divide users into 2 teams
        team1 = users.subList(0, users.size() / 2);
        team2 = users.subList(users.size() / 2, users.size());

        shots = new HashMap<>();
        input = new HashMap<>();
        spotsTeam1 = new ArrayList<>();
        spotsTeam2 = new ArrayList<>();

        // initialize the spots for each team
        initializeSpots(spotsTeam1,users.size()/2);
        initializeSpots(spotsTeam2,users.size()/2);


    }

    @Override
    public void run() {

        // send team members and elo
        sendTeams();

        // process input from each user
        for (User user : users) {
            readFromUser(user);
        }

        // play the game and check the winner
        processGame();
        String winner = checkWinner();

        // send the game info to each user to be displayed there
        sendGameInfo(winner);

        finish();
    }

    private void sendTeams(){

        Map<String,Object> team1Map = new HashMap<>();
        Map<String,Object> team2Map = new HashMap<>();
        for (User user : team1) {
            team1Map.put(user.getUsername(), user.getElo());
        }

        for (User user : team2){
            team2Map.put(user.getUsername(),user.getElo());
        }

        for (User user : users){
            user.writeLine(mapToJsonString(team1Map)+";"+ mapToJsonString(team2Map));
        }
    }

    private void sendGameInfo(String winner){
        Map<String,Object> team1SpotsMap = new HashMap<>();
        Map<String,Object> team2SpotsMap = new HashMap<>();

        for (int i = 0; i < nrOfSpots; i++){
            team1SpotsMap.put(String.valueOf(i+1),getTargetNames(spotsTeam1.get(i)));
        }
        for (int i = 0; i < nrOfSpots; i++){
            team2SpotsMap.put(String.valueOf(i+1),getTargetNames(spotsTeam2.get(i)));
        }

        for (User user : users){
            user.writeLine(mapToJsonString(team1SpotsMap)+";"
                    + mapToJsonString(team2SpotsMap) + ";"
                    + mapToJsonString(shots)+";"
                    + mapToJsonString(Map.of("winner",winner)));
        }
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
        if (type.equals("choice")) {
            getInput(user);
        } else {
            user.writeLine("1 Invalid command");
        }
    }

    private void getInput(User user){

        String argsString = user.readLine();
        Map<String,Object> args = jsonStringToMap(argsString);

        // check if the arguments are valid
        if (!args.containsKey("spot") || !args.containsKey("shot")) {
            user.writeLine("1 Invalid arguments");
            return;
        }

        int spot = Integer.parseInt(args.get("spot").toString());
        int shot = Integer.parseInt(args.get("shot").toString());

        // store the input from each user to be processed later
        Map<String,Object> playerChoices = new HashMap<>();
        playerChoices.put("spot",spot);
        playerChoices.put("shot",shot);
        input.put(user,mapToJsonString(playerChoices));

        // send the confirmation to the user
        user.writeLine("0");

    }

    private void processGame(){

        // put each user in a spot
        for (User user : input.keySet()){
            Map<String,Object> playerMap = jsonStringToMap(input.get(user));
            int spot = (int) playerMap.get("spot");
            updateSpot(getSpots(user).get(spot-1),user.getUsername());
        }

        // process the shots
        for (User user : input.keySet()){
            Map<String,Object> playerMap = jsonStringToMap(input.get(user));
            int shot = (int) playerMap.get("shot");
            List<List<String>> opponentSpots = getOpponentSpots(user);

            if (!spotTaken(opponentSpots.get(shot-1))){
                shots.put(user.getUsername(),"");  // missed
            }
            else {
                String targetName = getTargetNames(opponentSpots.get(shot-1));
                shots.put(user.getUsername(),targetName); // hit
                markSpotAsDead(opponentSpots.get(shot-1));
            }
        }
    }

    private void updateSpot(List<String> spot,String username){
        if (spotTaken(spot)){
            addPlayertoSpot(spot,username);
        }
        else {
            spot.set(0,"alive");
            spot.set(1,username);
        }

    }

    private String getTargetNames(List<String> spot){
        StringBuilder targetNames = new StringBuilder();
        for (String player : spot){
            if (player.equals("alive") || player.equals("dead")){
                continue;
            }
            if (player.equals("")){
                break;
            }
            targetNames.append(player).append(" + ");
        }
        return targetNames.toString();
    }

    private void markSpotAsDead(List<String> spot){
        spot.set(0,"dead");
    }

    private boolean spotTaken(List<String> spot){
        return !spot.get(0).equals("");
    }


    private void addPlayertoSpot(List<String> spot,String username){
        for (String player : spot){
            if (player.equals("")){
                spot.set(spot.indexOf(player),username);
                break;
            }
        }
    }

    private int getSpotNrOfPlayers(List<String> spot){
        int nrOfPlayers = 0;
        for (String player : spot){
            if (player.equals("alive") || player.equals("dead")){
                continue;
            }
            if (player.equals("")){
                break;
            }
            nrOfPlayers++;
        }
        return nrOfPlayers;
    }

    private List<List<String>> getSpots(User user){
        if (team1.contains(user)){
            return spotsTeam1;
        }
        return spotsTeam2;
    }

    private List<List<String>> getOpponentSpots(User user){
        if (team1.contains(user)){
            return spotsTeam2;
        }
        return spotsTeam1;
    }


    // check who has less dead spots
    private String checkWinner(){
        int team1Dead  = teamNrOfDeads(spotsTeam1);
        int team2Dead  = teamNrOfDeads(spotsTeam2);
        if (team1Dead < team2Dead){
            return "TERRORISTS";
        }
        else if (team2Dead < team1Dead){
            return "COUNTER-TERRORISTS";
        }
        else {
            return "draw";
        }
    }

    private int teamNrOfDeads(List<List<String>> spotsTeam){
        int deads = 0;

        for (List<String> spot : spotsTeam){
            if (spot.get(0).equals("dead")){
                int spotPlayers = getSpotNrOfPlayers(spot);
                deads += spotPlayers;
            }
        }

        return deads;
    }

    private void initializeSpots(List<List<String>> spots, int maxPerSpot)
    {
        for (int i = 0; i < nrOfSpots; i++) {
            List<String> spot = new ArrayList<>();
            for (int j = 0; j < maxPerSpot + 1 ; j++) {
                spot.add("");
            }
            spots.add(spot);
        }
    }
}


