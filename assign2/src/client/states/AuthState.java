package client.states;

import client.Session;

import java.util.Map;
import java.util.Scanner;

public class AuthState implements State {
    @Override
    public State step() {
        System.out.println("Welcome to the game!");
        System.out.println("Type 'register' to register or 'login' to login");

        Scanner scanner = new Scanner(System.in);
        String option = scanner.nextLine();

        if (option.equals("login")) login();
        if (option.equals("register")) register();

        scanner.close();
        Session session = Session.getSession();
        System.out.println(session.readLine());

        return null;
    }

    private boolean register() {
        String[] response = sendAuthRequest("register");

        if (response[0].equals("0")) {
            System.out.println("Successfully registered");
            return true;
        } else {
            System.out.println("Error registering");
            return false;
        }
    }

    private boolean login() {
        String[] response = sendAuthRequest("login");

        if (response[0].equals("0")) {
            System.out.println("Successfully registered");
            Session.getSession().setToken(response[1]);
            System.out.println("Token: " + response[1]);
            return true;
        } else {
            System.out.println("Error registering: " + response[1] );
            return false;
        }
    }

    private String[] sendAuthRequest(String method) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("username: ");
        String username = scanner.nextLine();
        System.out.print("password: ");
        String password = scanner.nextLine();

        scanner.close();
        Session session = Session.getSession();
        session.writeMessage("auth",
                method,
                Map.of("username", username, "password", password));

        return session.readResponse();
    }

}
