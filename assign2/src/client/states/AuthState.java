package client.states;

import client.Session;

import java.io.Console;
import java.util.Map;
import java.util.Scanner;

/**
 * Default entry point of the program. Allows user to authenticate.
 */
public class AuthState implements State {
    @Override
    public State step() {
        breakLn();
        printBanner();
        String option = "";
        Boolean status = false;

        while (!status) {
            System.out.println("\t\t1) Login");
            System.out.println("\t\t2) Register");
            System.out.println("\t\t0) Exit");

            option = Session.getSession().getScanner().nextLine();

            if (option.equals("1")) status = login();
            if (option.equals("2")) register();
            if (option.equals("0")) return null;
        }

        return new LobbyState();
    }

    private boolean register() {
        String[] response = sendAuthRequest("register");

        if (response[0].equals("0")) {
            System.out.println("Successfully registered");
            return true;
        } else {
            System.out.println("Error registering: " + response[1]);
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
            breakLn();
            System.out.println("Error signing in: " + response[1] );
            return false;
        }
    }

    private String[] sendAuthRequest(String method) {
        Scanner scanner = Session.getSession().getScanner();
        System.out.print("Username: ");
        String username = scanner.nextLine();

        String password;
        Console console = System.console();
        if (console == null) {
            System.out.print("Password: ");
            password = scanner.nextLine();
        } else {
            char[] passwordArray = console.readPassword("Password: ");
            password = new String(passwordArray);
        }

        Session session = Session.getSession();
        session.writeMessage("auth",
                method,
                Map.of("username", username, "password", password));

        return session.readResponse();
    }

    private void printBanner() {
        System.out.println("   ___________    _   ______     ___ \n" +
                "  / ____/ ___/   / | / / __ \\   |__ \\\n" +
                " / /    \\__ \\   /  |/ / / / /   __/ /\n" +
                "/ /___ ___/ /  / /|  / /_/ /   / __/ \n" +
                "\\____//____/  /_/ |_/\\____/   /____/ \n");
        System.out.println();
    }

}
