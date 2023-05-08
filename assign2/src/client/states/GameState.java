package client.states;

import client.Session;

public class GameState implements State {
    @Override
    public State step() {
        System.out.println("Waiting in queue");
        Session session = Session.getSession();

        System.out.println(session.readLine());
        System.out.println("Press enter to continue");
        session.getScanner().nextLine();
        return new LobbyState();
    }
}
