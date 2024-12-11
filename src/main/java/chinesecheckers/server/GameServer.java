package chinesecheckers.server;
import java.io.*;
import java.net.*;
import java.util.*;

import chinesecheckers.patterns.Observable;
import chinesecheckers.patterns.Observer;

public class GameServer implements Observable{
    private static GameServer instance;    
    private final int port;
    private final List<ClientHandler> players = new ArrayList<>();
    private final List<Integer> playerOrder = new ArrayList<>();
    private final Set<Integer> disconnectedPlayers = new HashSet<>();
    private final List<Integer> standings = new ArrayList<>();
    private final List<Observer> observers = new ArrayList<>();
    private int currentPlayerIndex = 0;
    private int maxPlayers;
    private int nextPlayerId = 1;
    private final Board board;
    private boolean gameStarted = false;
    
    private GameServer(int port) {
        this.port = port;
        this.board = new Board();
    }

    public static synchronized GameServer getInstance(int port) {
        if (instance == null) {
            instance = new GameServer(port);
        }
        return instance;
    }
    
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serwer uruchomiony na porcie: " + port);
            initializeGame(serverSocket);
            startGame(serverSocket);
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
        new Thread(() -> handleNewConnections(serverSocket)).start();
        synchronized (players) {
            while (players.size() < maxPlayers) {
                try {
                    players.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Waiting for players interrupted.");
                }
            }
        }
    
        for (ClientHandler player : players) {
            playerOrder.add(player.getPlayerId());
        }
        System.out.println("Wszyscy gracze dołączyli. Losowanie kolejności...");
        Collections.shuffle(playerOrder);
            
        for (ClientHandler player : players) {
            player.sendMessage("Kolejność gry: " + playerOrder.toString());
        }
    
        gameStarted = true;
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
    private void startGame(ServerSocket serverSocket) throws IOException {
        while ((standings.size() + disconnectedPlayers.size()) < maxPlayers) {
            processTurn();
        }
        System.out.println("Gra zakończona!");
        cleanupDisconnectedPlayers();
        displayStandings();
        System.exit(0);
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

    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(String message) {
        for (Observer observer : observers) {
            observer.update(message);
        }
    }

    public void broadcastMessage(String message) {
        notifyObservers(message);
    }

    private void broadcastMessage(String message, int excludePlayerId) {
        for (Observer observer : observers) {
            if (observer instanceof ClientHandler && 
                ((ClientHandler) observer).getPlayerId() != excludePlayerId) {
                observer.update(message);
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

    private void handleNewConnections(ServerSocket serverSocket) {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                synchronized (players) {
                    if (gameStarted) {
                        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                            out.println("Gra już się rozpoczęła.");
                        } finally {
                            clientSocket.close();
                        }
                    } else {
                        ClientHandler player = new ClientHandler(clientSocket, nextPlayerId++);
                        if (player.isConnected()) {
                            players.add(player);
                            addObserver(player);
                            System.out.println("Gracz " + player.getPlayerId() + " dołączył do gry.");
                            players.notifyAll();
                        } else {
                            System.out.println("Gracz " + player.getPlayerId() + " rozłączył się przed dołączeniem do gry.");
                        }
                        removeDisconnectedPlayersBeforeStart();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        GameServer server = new GameServer(12345);
        server.start();
    }
}