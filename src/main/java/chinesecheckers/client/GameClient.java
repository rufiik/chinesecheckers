package chinesecheckers.client;
import java.io.*;
import java.net.*;
import java.util.Scanner;

import chinesecheckers.server.GameServer;

public class GameClient {
    private final String host;
    private final int port;
    private Board board;
    private GameServer gameServer;
    public GameClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.gameServer = new GameServer(port);
        this.board = new Board(gameServer);
    }

    public void start() {
        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            try (Scanner scanner = new Scanner(System.in)) {
                System.out.println("Połączono z serwerem!");

                new Thread(() -> {
                    try {
                        String serverMessage;
                        while ((serverMessage = in.readLine()) != null) {
                            synchronized (System.out) {
                                System.out.println("Serwer: " + serverMessage);
                            }
                            
                        }
                    }catch (IOException e) {
                        System.out.println("Połączenie z serwerem zostało przerwane: " + e.getMessage());
                    } finally {
                        stopConnection();
                    }
                }).start();

                while (true) {
                    String move = scanner.nextLine();
                    out.println(move);
                    synchronized (System.out) {
                        board.updateBoard(move);
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Niepołączono z serwerem! Sprawdź, czy serwer jest uruchomiony i spróbuj ponownie.");
            System.exit(1); 
    }
    }
    public void stopConnection() {
        try {
            System.out.println("Gra została zakończona");
        } catch (Exception e) {
            System.out.println("Błąd podczas zamykania połączenia: " + e.getMessage());
        } finally {
            System.exit(0); 
        }
    }

    public static void main(String[] args) {
        GameClient client = new GameClient("localhost", 12345);
        client.start();
    }
}