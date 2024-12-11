package chinesecheckers.client;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class GameClient {
    private final String host;
    private final int port;
    private boolean isPlayerTurn = false;
    private boolean isConnected = true;
    public GameClient(String host, int port) {
        this.host = host;
        this.port = port;
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
                                if (serverMessage.equals("Serwer został zamknięty.")) {
                                    System.out.println(serverMessage);
                                    stopConnection();
                                    break;
                                } else if (serverMessage.equals("Twoja tura!")) {
                                    System.out.println(serverMessage);
                                    isPlayerTurn = true;
                                } else if (serverMessage.equals("Gra już się rozpoczęła.")) {
                                    closeConnection();
                                    break;
                                } else {
                                    System.out.println(serverMessage);
                                }
                            }
                            
                        }
                    } catch (IOException e) {
                        System.out.println("Połączenie z serwerem zostało przerwane: " + e.getMessage());
                    } finally {
                        isConnected = false;
                        stopConnection();
                    }
                }).start();

                while (isConnected) {
                    synchronized (System.out) {
                        if (isPlayerTurn) {
                            clearInputBuffer();
                            String move = scanner.nextLine();
                            out.println(move);
                            isPlayerTurn = false; 
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Niepołączono z serwerem! Sprawdź, czy serwer jest uruchomiony i spróbuj ponownie.");
            System.exit(1); 
        }
    }

    private void clearInputBuffer() {
        try {
            while (System.in.available() > 0) {
                System.in.read(); 
            }
        } catch (IOException e) {
            System.out.println("Błąd podczas czyszczenia bufora wejściowego: " + e.getMessage());
        }
    }

    public void stopConnection() {
        try {
            System.out.println("Rozłączono z serwerem.");
        } catch (Exception e) {
            System.out.println("Błąd podczas zamykania połączenia: " + e.getMessage());
        } finally {
            System.exit(0); 
        }
    }

    public void closeConnection() {
        try {
            System.out.println("Gra już się rozpoczęła.");
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