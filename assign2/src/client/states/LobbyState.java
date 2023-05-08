package client.states;

import client.Session;

import java.util.Map;
import java.util.Scanner;

public class LobbyState implements State {
    @Override
    public State step() {
        breakLn();
        printTitle("Lobby");

        System.out.println("\n\t1) Play casual");
        System.out.println("\t2) Play ranked");
        System.out.println("\t-) See profile");

        Scanner scanner = new Scanner(System.in);

        String option = scanner.nextLine();
        while (!(option.equals("1") || option.equals("2"))) {
            System.out.println("Invalid option");
            option = scanner.nextLine();
        }

        switch (option) {
            case "1":
                if (joinGame("casual")) {
                    return new GameState();
                } else {
                    return new LobbyState();
                }
        }
        return null;
    }

    public boolean joinGame(String type) {
        Session session = Session.getSession();
        Map<String, Object> args = Map.of("token", session.getProfileInfo("token"));
        session.writeMessage("MATCHMAKING",  type, args);

        String[] response = session.readResponse();
        if (response[0].equals("0")) {
            return true;
        } else {
            System.out.println("Error joining game: " + response[1] );
            return false;
        }
    }
}
