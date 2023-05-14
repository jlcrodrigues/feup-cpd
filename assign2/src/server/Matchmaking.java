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
                Store.getStore().registerIdleSocket(socket);
                break;
            default:
                socket.writeLine("1 Invalid command");
        }
    }

    private void sendProfile(User user) {
        if (user == null) {
            socket.writeLine("1 Invalid token");
            return;
        }
        user.setSocket(socket);
        socket.writeLine("0 " + mapToJsonString(user.toMap()));
    }
}
