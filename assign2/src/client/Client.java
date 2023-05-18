package client;

import client.states.AuthState;
import client.states.GameState;
import client.states.LobbyState;
import client.states.State;

public class Client {

    public static void main(String[] args) {
        boolean restart = false;
        for (String arg : args) {
            if (arg.equals("-r")) {
                restart = true;
                break;
            }
        }

        Session session = Session.getSession();

        if (!restart)
            session.load();

        State state = initialState(session);
        while (state != null) {
            state = state.step();
        }

        session.close();
    }

    private static State initialState(Session session) {
        if (session.isLoggedIn()) {
            if (session.getProfileInfo("state").equals("none"))
                return new LobbyState();
            else if (session.getProfileInfo("state").equals("queue")
             ||session.getProfileInfo("state").equals("game"))
                return new GameState();
        }
        return new AuthState();
    }
}