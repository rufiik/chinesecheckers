package chinesecheckers.server;
import java.io.*;
import java.net.*;

import chinesecheckers.patterns.Observer;

public class ClientHandler implements Observer {
    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;
    private final int playerId;

    public ClientHandler(Socket socket, int playerId) throws IOException {
        this.socket = socket;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.playerId = playerId;
        sendMessage("Witaj, Graczu " + playerId + "!");
    }

    @Override
    public void update(String message) {
        sendMessage(message);
    }
    
    public void sendMessage(String message) {
            out.println(message);
    }

    public String receiveMessage() {
        try {
            String message = in.readLine();
            if (message != null && !message.isEmpty()) {
                return message;
            }
        } catch (IOException e) {
            System.out.println("Błąd podczas odbierania wiadomości: " + e.getMessage());
        }
        return null;
    }

    public boolean isConnected() {
        try {
            socket.sendUrgentData(0xFF);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getPlayerId() {
        return playerId;
    }
}