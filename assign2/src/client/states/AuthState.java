package client.states;

import client.Session;

import java.util.Map;
import java.util.Scanner;

public class AuthState implements State {
    @Override
    public State step() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("username: ");
        String username = scanner.nextLine();
        System.out.print("password: ");
        String password = scanner.nextLine();
        scanner.close();

        Session session = Session.getSession();
        session.writeMessage("auth",
                "login",
                Map.of("username", username, "password", password));

        System.out.println(session.readLine());
        System.out.println(session.readLine());

        return null;
    }

}
