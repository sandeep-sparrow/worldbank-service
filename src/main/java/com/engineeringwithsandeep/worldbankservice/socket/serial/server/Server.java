package com.engineeringwithsandeep.worldbankservice.socket.serial.server;

import com.engineeringwithsandeep.worldbankservice.socket.serial.command.*;
import com.engineeringwithsandeep.worldbankservice.socket.serial.dao.WDIDao;
import com.engineeringwithsandeep.worldbankservice.socket.serial.service.WorldBankServiceImpl;

import java.io.*;
import java.net.*;
import java.util.HashMap;

public class Server {

    private static final int PORT = 8080;
    private static int clientCount = 0;
    private static volatile boolean stopServer = false;
    private static final HashMap<Integer, Thread> clientThreadMap = new HashMap<>();
    private static ServerSocket serverSocket;

    public static void main(String[] args) {

        System.out.println("Initialization complete");

        try {
            serverSocket = new ServerSocket(PORT);  // Assign the server socket here
            System.out.println("Server is listening on port " + PORT);

            while (!stopServer) {  // Continue until stopServer is true
                try {
                    Socket socket = serverSocket.accept();  // Accept new client connections
                    System.out.println("New connection from " + socket.getRemoteSocketAddress());

                    clientCount++;
                    ServerThread serverThread = new ServerThread(socket, clientCount);

                    clientThreadMap.put(clientCount, serverThread);
                    serverThread.start();  // Start the client handler thread

                } catch (SocketException e) {
                    // When server socket is closed, this will be triggered
                    if (stopServer) {
                        System.out.println("Server is stopping...");
                    } else {
                        System.out.println("Socket error: " + e.getMessage());
                    }
                }
            }

            System.out.println("Server Stopped");

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            stopAllClients();  // Cleanly stop all client threads when server stops
        }
    }

    public static void stopServer() {
        stopServer = true;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();  // Closing the server socket to stop accepting clients
            }
        } catch (IOException e) {
            System.out.println("Error while closing server socket: " + e.getMessage());
        }
    }

    public static void stopAllClients() {
        for (Thread clientThread : clientThreadMap.values()) {
            System.out.println("Interrupting client thread: " + clientThread.getName());
            clientThread.interrupt();  // Interrupt each thread
        }
        System.out.println("All client threads stopped.");
    }
}

class ServerThread extends Thread {
    private final Socket socket;
    private final int clientId;

    public ServerThread(Socket socket, int clientId) {
        this.socket = socket;
        this.clientId = clientId;
    }

    public void run() {
        WorldBankServiceImpl service = new WorldBankServiceImpl(new WDIDao());

        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Client " + clientId + " connected.");
            String command;

            while ((command = in.readLine()) != null) {
                System.out.println("Received command from client " + clientId + ": " + command);
                String[] commandData = command.split(";");

                // Process the command and get the appropriate response
                Command processedCommand = processCommand(commandData, service);

                String response = processedCommand.execute();
                System.out.println("Response: " + response);
                out.println(response);
                out.flush();

                // If StopCommand is received, stop the server
                if (processedCommand instanceof StopCommand) {
                    Server.stopServer();  // Stop the server
                    break;
                }

                // Handle DisconnectCommand to stop this client thread
                if (processedCommand instanceof DisconnectCommand) {
                    System.out.println("Client " + clientId + " disconnected.");
                    break;  // Break the loop to stop the client thread
                }
            }

        } catch (IOException e) {
            if (!Thread.currentThread().isInterrupted()) {
                System.out.println("Error in client thread " + clientId + ": " + e.getMessage());
                e.printStackTrace();
            }
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Failed to close socket for client " + clientId + ": " + e.getMessage());
            }
        }
    }

    private Command processCommand(String[] commandData, WorldBankServiceImpl service) {
        Command command;
        System.out.println("Command: " + commandData[0]);
        command = switch (commandData[0]) {
            case "q" -> {
                System.out.println("Query");
                yield new QueryCommand(commandData, service);
            }
            case "r" -> {
                System.out.println("Report");
                yield new ReportCommand(commandData, service);
            }
            case "z" -> {
                System.out.println("Stop");
                yield new StopCommand(commandData);  // Stops the entire server
            }
            case "e" -> {
                System.out.println("Client disconnected.");
                yield new DisconnectCommand(commandData);  // Stops the current client thread
            }
            default -> {
                System.out.println("Error");
                yield new ErrorCommand(commandData);  // Handles any unknown commands
            }
        };
        return command;
    }
}
