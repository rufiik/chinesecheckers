package chinesecheckers.server;
import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {
    private final int port;
    private final List<ClientHandler> players = new ArrayList<>();
    private final List<Integer> playerOrder = new ArrayList<>();
    private final Set<Integer> finishedPlayers = new HashSet<>();
    private final List<Integer> standings = new ArrayList<>();
    private int currentPlayerIndex = 0;
    private int maxPlayers;

    

    public GameServer(int port) {
        this.port = port;
;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serwer uruchomiony na porcie: " + port);
            initializeGame(serverSocket);
            startGame();
        } catch (BindException e) {
            System.out.println("Serwer już działa na porcie: " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeGame(ServerSocket serverSocket) throws IOException {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Podaj liczbę graczy (2, 3, 4, 6): ");
            maxPlayers = scanner.nextInt();
        }
        System.out.println("Oczekiwanie na graczy...");
        for (int i = 0; i < maxPlayers; i++) {
            Socket clientSocket = serverSocket.accept();
            ClientHandler player = new ClientHandler(clientSocket, i+1);
            players.add(player);
            playerOrder.add(i);
            System.out.println("Gracz " + (i + 1) + " dołączył do gry.");
        }
        System.out.println("Wszyscy gracze dołączyli. Losowanie kolejności...");
        Collections.shuffle(playerOrder);
        
        for (int i = 0; i < players.size(); i++) {
            List<Integer> displayOrder = new ArrayList<>();
            for (int order : playerOrder) {
                displayOrder.add(order + 1);
            }
            players.get(i).sendMessage("Kolejność gry: " + displayOrder.toString());
        }
    }

    private void startGame() {
        while (standings.size() < maxPlayers) {
            processTurn();
        }
        System.out.println("Gra zakończona!");
        cleanupDisconnectedPlayers();
        displayStandings();
    }

    private void processTurn() {
        int playerId = playerOrder.get(currentPlayerIndex);
        ClientHandler currentPlayer = players.get(playerId);

        if (standings.contains(playerId)) {
            currentPlayerIndex = (currentPlayerIndex + 1) % maxPlayers;
            return;
        }

        if (!currentPlayer.isConnected()) {
            System.out.println("Gracz " + (playerId + 1) + " rozłączył się.");
            finishedPlayers.add(playerId);
            standings.add(playerId);
            broadcastMessage("Gracz " + (playerId + 1) + " rozłączył się.");
            currentPlayerIndex = (currentPlayerIndex + 1) % maxPlayers;
            return;
        }

        currentPlayer.sendMessage("Twoja tura!");
        broadcastMessage("Gracz " + (playerId + 1) + " wykonuje ruch.", playerId);

        String move = currentPlayer.receiveMessage();
        if (move == null) {
            System.out.println("Gracz " + (playerId + 1) + " rozłączył się w trakcie swojej tury.");
            broadcastMessage("Gracz " + (playerId + 1) + " rozłączył się w trakcie swojej tury!");
            finishedPlayers.add(playerId);
        } else if (move.equalsIgnoreCase("WYGRANA")) {
            standings.add(playerId);
            broadcastMessage("Gracz " + (playerId + 1) + " zajął miejsce " + standings.size() + "!");
            
            if (standings.size() == maxPlayers - 1) {
                for (int id : playerOrder) {
                    if (!standings.contains(id)) {
                        standings.add(id);
                        broadcastMessage("Gracz " + (id + 1) + " zajął miejsce " + standings.size() + "!");
                        break;
                    }
                }
            }
        } else {
            System.out.println("Gracz " + (playerId + 1) + " wykonał ruch: " + move);
            broadcastMessage("Gracz " + (playerId + 1) + " wykonał ruch: " + move, playerId);
        }

        currentPlayerIndex = (currentPlayerIndex + 1) % maxPlayers;
    }

    public void broadcastMessage(String message) {
        for (ClientHandler player : players) {
            player.sendMessage(message);
        }
    }

    private void broadcastMessage(String message, int excludePlayerId) {
        for (int i = 0; i < players.size(); i++) {
            if (i != excludePlayerId) {
                players.get(i).sendMessage(message);
            }
        }
    }

    private void cleanupDisconnectedPlayers() {
        for (ClientHandler player : players) {
            if (!player.isConnected()) {
                player.close();
            }
        }
    }

    private void displayStandings() {
        System.out.println("Kolejność końcowa:");
        for (int i = 0; i < standings.size(); i++) {
            System.out.println((i + 1) + ". Gracz " + (standings.get(i) + 1));
        }
        broadcastMessage("Gra zakończona! Kolejność końcowa: ");
        for (int i = 0; i < standings.size(); i++) {
            broadcastMessage((i + 1) + ". miejsce: Gracz " + (standings.get(i) + 1));
        }
    }

    public static void main(String[] args) {
        GameServer server = new GameServer(12345);
        server.start();
    }
}