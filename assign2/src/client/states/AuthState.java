package client.states;

import client.Session;

import java.util.Map;
import java.util.Scanner;

public class AuthState implements State {
    @Override
    public State step() {
        printBanner();
        String option = "";
        Boolean status = false;

        while (!status) {
            System.out.println("\t\t1) Login");
            System.out.println("\t\t2) Register");

            option = Session.getSession().getScanner().nextLine();

            if (option.equals("1")) status = login();
            if (option.equals("2")) status = register();
        }

        return new LobbyState();
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
            Map<String, Object> profile = Session.getSession().jsonStringToMap(response[1]);
            Session.getSession().setProfile(profile);
            return true;
        } else {
            System.out.println("Error registering: " + response[1] );
            return false;
        }
    }

    private String[] sendAuthRequest(String method) {
        Scanner scanner = Session.getSession().getScanner();
        System.out.print("username: ");
        String username = scanner.nextLine();
        System.out.print("password: ");
        String password = scanner.nextLine();

        Session session = Session.getSession();
        session.writeMessage("auth",
                method,
                Map.of("username", username, "password", password));

        return session.readResponse();
    }

    private void printBanner() {
        System.out.println(" a88888b.  .d88888b   **  888888ba    .88888.   d8888b. ");
        System.out.println("d8'   `88  88.    \"'  **  88    `8b  d8'   `8b      `88 ");
        System.out.println("88         `Y88888b.      88     88  88     88  .aaadP' ");
        System.out.println("88              `8b       88     88  88     88  88'     ");
        System.out.println("Y8.   .88  d8'   .8P  **  88     88  Y8.   .8P  88.     ");
        System.out.println(" Y88888P'  Y88888P    **  dP     dP   `8888P'   Y88888P");
        System.out.println();
    }

}
