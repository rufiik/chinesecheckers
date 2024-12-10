package chinesecheckers.server;
import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {
    private final int port;
    private final List<ClientHandler> players = new ArrayList<>();
    private final List<Integer> playerOrder = new ArrayList<>();
    private final Set<Integer> disconnectedPlayers = new HashSet<>();
    private final List<Integer> standings = new ArrayList<>();
    private int currentPlayerIndex = 0;
    private int maxPlayers;
    private int nextPlayerId = 1;
    private final Board board;

    public GameServer(int port) {
        this.port = port;
        this.board = new Board();
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
            int inputPlayers;
            while (true) {
                System.out.println("Podaj liczbę graczy (2, 3, 4, 6): ");
                inputPlayers = scanner.nextInt();
                if (inputPlayers == 2 || inputPlayers == 3 || inputPlayers == 4 || inputPlayers == 6) {
                    maxPlayers = inputPlayers;
                    break;
                } else {
                    System.out.println("Niepoprawna liczba graczy! Wybierz 2, 3, 4 lub 6.");
                }
            }
        }
    
        System.out.println("Oczekiwanie na graczy...");
        while (players.size() < maxPlayers) {
            Socket clientSocket = serverSocket.accept();
            ClientHandler player = new ClientHandler(clientSocket, nextPlayerId++);
            if (player.isConnected()) {
                players.add(player);
                System.out.println("Gracz " + player.getPlayerId() + " dołączył do gry.");
            } else {
                System.out.println("Gracz " + player.getPlayerId() + " rozłączył się przed dołączeniem do gry.");
            }
            removeDisconnectedPlayersBeforeStart();
        }

        for (ClientHandler player : players) {
            playerOrder.add(player.getPlayerId());
        }
        System.out.println("Wszyscy gracze dołączyli. Losowanie kolejności...");
        Collections.shuffle(playerOrder);
        
        for (ClientHandler player : players) {
            player.sendMessage("Kolejność gry: " + playerOrder.toString());
        }
    }
    private void removeDisconnectedPlayersBeforeStart() {
        Iterator<ClientHandler> iterator = players.iterator();
        while (iterator.hasNext()) {
            ClientHandler player = iterator.next();
            if (!player.isConnected()) {
                System.out.println("Gracz " + player.getPlayerId() + " rozłączył się.");
                iterator.remove();
            }
        }
    }
    private void startGame() {
        while ((standings.size() + disconnectedPlayers.size()) < maxPlayers) {
            processTurn();
        }
        System.out.println("Gra zakończona!");
        cleanupDisconnectedPlayers();
        displayStandings();
    }

    private void processTurn() {
        int playerId = playerOrder.get(currentPlayerIndex);
        ClientHandler currentPlayer = null;

        for (ClientHandler player : players) {
            if (player.getPlayerId() == playerId) {
                currentPlayer = player;
                break;
            }
        }

        if (standings.contains(playerId) || disconnectedPlayers.contains(playerId)) {
            currentPlayerIndex = (currentPlayerIndex + 1) % maxPlayers;
            return;
        }

        if (currentPlayer == null || !currentPlayer.isConnected()) {
            System.out.println("Gracz " + playerId + " rozłączył się.");
            broadcastMessage("Gracz " + playerId + " rozłączył się.");
            disconnectedPlayers.add(playerId);
            currentPlayerIndex = (currentPlayerIndex + 1) % playerOrder.size();
            return;
        }
        
        currentPlayer.sendMessage("Twoja tura!");
        broadcastMessage("Gracz " + playerId + " wykonuje ruch.", playerId);

        String move = currentPlayer.receiveMessage();
        if (move == null) {
            System.out.println("Gracz " + playerId + " rozłączył się w trakcie swojej tury.");
            broadcastMessage("Gracz " + playerId + " rozłączył się w trakcie swojej tury!");
            disconnectedPlayers.add(playerId);
        } else if (move.equalsIgnoreCase("WYGRANA")) {
            standings.add(playerId);
            broadcastMessage("Gracz " + playerId + " zajął miejsce " + standings.size() + "!");
        } else {
            String result = board.processMove(move, playerId);
            currentPlayer.sendMessage(result);
    
            if (result.startsWith("Ruch wykonany")) {
                System.out.println("Gracz " + playerId + " wykonał ruch: " + move);
                broadcastMessage("Gracz " + playerId + " wykonał ruch: " + move, playerId);
            } else {
                currentPlayer.sendMessage("Spróbuj ponownie.");
            }
        }

        currentPlayerIndex = (currentPlayerIndex + 1) % maxPlayers;

        if ((standings.size() + disconnectedPlayers.size()) == maxPlayers - 1) {
            for (int id : playerOrder) {
                if (!standings.contains(id)) {
                    standings.add(id);
                    broadcastMessage("Gracz " + id + " zajął miejsce " + standings.size() + "!");
                    break;
                }
            }
        }
    }


    public void broadcastMessage(String message) {
        for (ClientHandler player : players) {
            player.sendMessage(message);
        }
    }

    private void broadcastMessage(String message, int excludePlayerId) {
        for (ClientHandler player : players) {
            if (player.getPlayerId() != excludePlayerId) {
                player.sendMessage(message);
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
        broadcastMessage("Gra zakończona! Kolejność końcowa: ");
        for (int i = 0; i < standings.size(); i++) {
            int playerId = standings.get(i);
            System.out.println((i + 1) + ". miejsce: Gracz " + playerId);
            broadcastMessage((i + 1) + ". miejsce: Gracz " + playerId);
        }
        for (int playerId : disconnectedPlayers) {
            System.out.println("Gracz " + playerId + " rozłączył się przed zakończeniem gry");
            broadcastMessage("Gracz " + playerId + " rozłączył się przed zakończeniem gry");
        }
    }

    public static void main(String[] args) {
        GameServer server = new GameServer(12345);
        server.start();
    }
}