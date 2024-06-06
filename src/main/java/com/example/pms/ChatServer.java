package com.example.pms;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatServer {
    private static final int STARTING_PORT = 12345;
    private static final int MAX_CLIENTS = 10; // Maximum number of clients
    private static Map<String, ObjectOutputStream> clients = new ConcurrentHashMap<>();
    private static AtomicInteger portCounter = new AtomicInteger(STARTING_PORT);

    public static void main(String[] args) {
        try {
            int port = findAvailablePort();
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port + " ...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected: " + socket);

                // Allocate a unique port for the client
                int clientPort = getNextPort();
                System.out.println("Allocated port " + clientPort + " for client.");

                // Create a thread to handle client communication
                ClientHandler handler = new ClientHandler(socket, clientPort);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static int findAvailablePort() throws IOException {
        for (int port = STARTING_PORT; port < STARTING_PORT + MAX_CLIENTS; port++) {
            try {
                new ServerSocket(port).close();
                return port;
            } catch (IOException e) {
                // Port is already in use, try the next one
            }
        }
        throw new IOException("No available ports in the range.");
    }

    private static int getNextPort() {
        return portCounter.getAndIncrement();
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private ObjectInputStream inputStream;
        private ObjectOutputStream outputStream;
        private String clientId;
        private int clientPort;

        public ClientHandler(Socket socket, int clientPort) {
            this.socket = socket;
            this.clientPort = clientPort;
        }

        @Override
        public void run() {
            try {
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                inputStream = new ObjectInputStream(socket.getInputStream());
                while (true) {
                    // Receive sender's ID
                    String senderId = (String) inputStream.readObject();
                    clients.put(senderId, outputStream); // Add sender's ID to the map
                    System.out.println("Client ID '" + senderId + "' connected.");

                    // Receive recipient's ID
                    String recipientId = (String) inputStream.readObject();
                    clients.put(recipientId, outputStream); // Add recipient's ID to the map

                    // Listen for messages from this client

                    // Read message from the client
                    String message = (String) inputStream.readObject();
                    System.out.println("Message received from client " + senderId + " to " + recipientId + ": " + message);
                    sendMessageToClient(recipientId, senderId, message); // Send the message to the specified recipient
                }

            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Client disconnected: " + clientId);
            } finally {
                // Clean up resources
                clients.remove(clientId);
                try {
                    if (outputStream != null) outputStream.close();
                    if (inputStream != null) inputStream.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendMessageToClient(String recipientId, String senderId, String message) {
            ObjectOutputStream clientStream = clients.get(recipientId);
            if (clientStream != null) {
                try {
                    // Write the sender's ID, recipient's ID, and message to the specified client's stream
                    clientStream.writeObject(senderId); // Send sender's ID
                    clientStream.writeObject(recipientId); // Send recipient's ID
                    clientStream.writeObject(message); // Send the message
                    clientStream.flush();

                    // Debugging line: Print the message sent to the client
                    System.out.println("Message sent to client " + recipientId + ": " + message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.err.println("Recipient not found: " + recipientId);
            }
        }
    }

}