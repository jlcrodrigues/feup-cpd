package server;

import server.game.User;
import server.store.SocketWrapper;
import server.store.Store;

import java.util.Map;

public class Matchmaking extends ConnectionHandler {
    public Matchmaking(SocketWrapper socketWrapper) {
        super(socketWrapper);
    }

    @Override
    public void run() {
        String type = socket.readLine();
        Map<String, Object> args = jsonStringToMap(socket.readLine());
        User user = Store.getStore().getUser((String) args.get("token"));
        MatchmakingQueue queue = MatchmakingQueue.getQueue();
        switch (type) {
            case "casual":
                queue.addCasualPlayer(user);
                socket.writeLine("0");
                break;
            case "ranked":
                queue.addRankedPlayer(user);
                socket.writeLine("0");
                break;
            case "profile":
                sendProfile(user);
                break;
            default:
                socket.writeLine("1 Invalid command");
        }
    }

    /**
     * Sends the user's profile to the client upon request.
     * This is used to reestablish broken connections.
     * @param user The user whose profile is to be sent.
     */
    private void sendProfile(User user) {
        if (user == null) {
            socket.writeLine("1 Invalid token");
            Store.getStore().registerIdleSocket(socket);
            return;
        }
        user.setSocket(socket);
        socket.writeLine("0 " + mapToJsonString(user.toMap()));
        if (user.getState().equals("game")) {
            user.getActiveGame().sendTeams(user);
        }
        if (user.getState() == null || user.getState().equals("none"))
            Store.getStore().registerIdleSocket(socket);
    }
}
