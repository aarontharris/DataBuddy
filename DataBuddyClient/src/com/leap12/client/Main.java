package com.leap12.client;

import java.io.IOException;

public class Main {

    public static void main(String args[]) {
        ClientConnector connector = new ClientConnector();
        try {
            connector.test();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
