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
            team1SpotsMap.put(String.valueOf(i+1),spotsTeam1.get(i).get(0));
        }
        for (int i = 0; i < nrOfSpots; i++){
            team2SpotsMap.put(String.valueOf(i+1),spotsTeam2.get(i).get(0));
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
            List<String> player = new ArrayList<>();
            player.add(user.getUsername());
            player.add("alive");
            getSpots(user).set(spot-1, player);
        }

        // process the shots
        for (User user : input.keySet()){
            Map<String,Object> playerMap = jsonStringToMap(input.get(user));
            int shot = (int) playerMap.get("shot");
            List<List<String>> opponentSpots = getOpponentSpots(user);
            String targetName = opponentSpots.get(shot-1).get(0);


            if (Objects.equals(targetName, "")) {
                shots.put(user.getUsername(),"");  // missed
            }
            else {
                shots.put(user.getUsername(),targetName); // hit
                opponentSpots.set(shot-1, Arrays.asList(targetName, "dead")); // mark the spot as dead
            }
        }
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
        int team1Dead  = Collections.frequency(spotsTeam1.stream().map(spot -> spot.get(1)).toList(),"dead");
        int team2Dead  = Collections.frequency(spotsTeam2.stream().map(spot -> spot.get(1)).toList(),"dead");
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

    private void initializeSpots(List<List<String>> spots, int maxPerSpot)
    {
        for (int i = 0; i < nrOfSpots; i++) {
            List<String> spot = new ArrayList<>();
            for (int j = 0; j < maxPerSpot * 2 ; j++) {
                spot.add("");
            }
            spots.add(spot);
        }
    }
}


