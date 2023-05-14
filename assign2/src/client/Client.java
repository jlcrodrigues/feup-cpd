package client;

import client.states.AuthState;
import client.states.LobbyState;
import client.states.State;

import java.net.*;
import java.io.*;
import java.util.Properties;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        Session session = Session.getSession();

        // commenting this line to make it easier running multiple instances
        // uncomment it if you want to get persistent sessions
        //session.load();

        State state = session.isLoggedIn() ? new LobbyState() : new AuthState();
        while (state != null) {
            state = state.step();
        }

        session.close();
    }
}