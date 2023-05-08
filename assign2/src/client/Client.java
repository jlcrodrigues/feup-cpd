package client;

import client.states.AuthState;
import client.states.State;

import java.net.*;
import java.io.*;
import java.util.Properties;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        Session.getSession();

        State state = new AuthState();
        while (state != null) {
            state = state.step();
        }
    }
}