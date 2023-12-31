package client.states;

import client.Session;

import java.util.Map;

/**
 * This class represents the lobby state of the client.
 * From here, client can move on to joining a queue, among other things.
 */
public class LobbyState implements State {
    @Override
    public State step() {
        breakLn();
        printTitle("Lobby");

        System.out.println("\n\t1) Play casual");
        System.out.println("\t2) Play ranked");
        System.out.println("\t3) See profile");
        System.out.println("\t4) Log out");
        System.out.println("\t0) Exit");

        Session session = Session.getSession();
        String option = session.readInputLine();
        while (!(option.matches("[0-4]"))) {
            System.out.println("Invalid option");
            option = session.readInputLine();
        }

        switch (option) {
            case "1":
                if (joinGame("casual")) {
                    return new GameState();
                } else {
                    return new LobbyState();
                }
            case "2":
                if (joinGame("ranked")) {
                    return new GameState();
                } else {
                    return new LobbyState();
                }
            case "3":
                return printProfile();
            case "4":
                logout();
                return new AuthState();
        }
        return null;
    }

    private boolean joinGame(String type) {
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

    private State printProfile() {
        Session session = Session.getSession();
        breakLn();
        printTitle("Profile");
        System.out.println();
        System.out.println("Username: " + session.getProfileInfo("username"));
        System.out.println("Elo: " + session.getProfileInfo("elo"));
        System.out.println();

        System.out.println("\t1) Back");
        System.out.println("\t0) Exit");


        String option = session.readInputLine();
        while (!(option.matches("[0-1]"))) {
            System.out.println("Invalid option");
            option = session.readInputLine();
        }

        if (option.equals("1")) {
            return new LobbyState();
        }
        return null;
    }

    private void logout() {
        Session session = Session.getSession();
        Map<String, Object> args = Map.of("token", session.getProfileInfo("token"));
        session.writeMessage("auth", "LOGOUT", args);
        session.setProfile(null);
        session.readResponse();
    }
}
