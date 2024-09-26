package com.engineeringwithsandeep.worldbankservice.socket.serial.client;

// Client.java
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
            System.out.println("Connected to the server");

            // Input and output streams to communicate with the server
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            // Read commands from the console
            Scanner scanner = new Scanner(System.in);
            String command;

            System.out.println("Enter a command (type 'e' to quit):");

            do {
                System.out.print(">> ");
                command = scanner.nextLine();

                // Send the command to the server
                writer.println(command);

                // Read the server's response
                String response = reader.readLine();
                System.out.println("Server response: " + response);

            } while (!command.equalsIgnoreCase("e"));

        } catch (UnknownHostException e) {
            System.out.println("Server not found: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        }
    }
}

