package client.states;

import client.Session;
import server.store.Store;

import java.util.HashMap;
import java.util.Map;

public class GameState implements State {
    @Override
    public State step() {
        System.out.println("Waiting in queue");
        Session session = Session.getSession();

        System.out.println(session.readLine());

        boolean spotChosen = false;

        while (!spotChosen) {
            spotChosen = chooseSpot();
        }

        System.out.println("Waiting for other players to choose spots");
        System.out.println(session.readLine());

        boolean shotTaken = false;

        while (!shotTaken) {
            shotTaken = takeShot();
        }

        System.out.println("Waiting for other players to take shots");
        System.out.println(session.readLine());
        System.out.println("The game has ended");
        System.out.println(session.readLine());

        System.out.println("Press enter to continue");
        session.getScanner().nextLine();
        return new LobbyState();
    }

    private boolean chooseSpot() {
        Session session = Session.getSession();
        int nrSpots = Store.getStore().getTeamSize() + 2;

        System.out.println("Choose a spot in the game between 1 and " + nrSpots);
        String spot = session.getScanner().nextLine();

        while (!(spot.matches("[1-" + nrSpots + "]"))) {
            System.out.println("Invalid spot.\nChoose a spot in the game between 1 and " + nrSpots);
            spot = session.getScanner().nextLine();
        }

        Map<String, Object> args = Map.of("spot", spot);
        String [] response = sendGameRequest("chooseSpot",args);
        if (response[0].equals("0")) {
            return true;
        } else {
            System.out.println("Error choosing spot: " + response[1] );
            return false;
        }
    }

    private boolean takeShot() {
        Session session = Session.getSession();
        int nrSpots = Store.getStore().getTeamSize() + 2;

        System.out.println("What spot do you want to shoot (1-" + nrSpots + ")?");
        String spot = session.getScanner().nextLine();

        while (!(spot.matches("[1-" + nrSpots + "]"))) {
            System.out.println("Invalid spot.\nChoose a spot to shoot between 1 and " + nrSpots);
            spot = session.getScanner().nextLine();
        }

        Map<String, Object> args = Map.of("spot", spot);

        String [] response = sendGameRequest("takeShot",args);
        if (response[0].equals("0")) {
            return true;
        } else {
            System.out.println("Error taking shot: " + response[1] );
            return false;
        }
    }

    private String [] sendGameRequest(String method, Map<String, Object> args) {
        Session session = Session.getSession();
        session.writeMessage("GAME", method, args);
        return session.readResponse();
    }
}
