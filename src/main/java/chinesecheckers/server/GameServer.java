package chinesecheckers.server;
import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {
    private final int port;
    private final int maxPlayers;
    private final List<ClientHandler> players = new ArrayList<>();
    private final List<Integer> playerOrder = new ArrayList<>();
    private final Set<Integer> finishedPlayers = new HashSet<>();
    private int currentPlayerIndex = 0;

    public GameServer(int port, int maxPlayers) {
        this.port = port;
        this.maxPlayers = maxPlayers;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serwer uruchomiony na porcie: " + port);
            System.out.println("Oczekiwanie na graczy...");

            while (players.size() < maxPlayers) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket, players.size() + 1);
                players.add(clientHandler);
                System.out.println("Gracz " + players.size() + " dołączył.");
            }

            System.out.println("Wszyscy gracze dołączyli. Losowanie kolejności...");
            initializeGame();

            System.out.println("Start gry!");
            while (finishedPlayers.size() < maxPlayers) {
                processTurn();
            }

            System.out.println("Gra zakończona!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeGame() {
        for (int i = 0; i < maxPlayers; i++) {
            playerOrder.add(i);
        }
        Collections.shuffle(playerOrder);
        
        for (int i = 0; i < players.size(); i++) {
            List<Integer> displayOrder = new ArrayList<>();
            for (int order : playerOrder) {
                displayOrder.add(order + 1);
            }
            players.get(i).sendMessage("Kolejność gry: " + displayOrder.toString());
        }
    }

    private void processTurn() {
        int playerId = playerOrder.get(currentPlayerIndex);

        if (finishedPlayers.contains(playerId)) {
            currentPlayerIndex = (currentPlayerIndex + 1) % maxPlayers;
            return;
        }

        ClientHandler currentPlayer = players.get(playerId);
        currentPlayer.sendMessage("Twoja tura!");
        broadcastMessage("Gracz " + (playerId + 1) + " wykonuje ruch.", playerId);

        String move = currentPlayer.receiveMessage();
        System.out.println("Gracz " + (playerId + 1) + " wykonał ruch: " + move);

        if (move.equalsIgnoreCase("WYGRANA")) {
            finishedPlayers.add(playerId);
            broadcastMessage("Gracz " + (playerId + 1) + " zakończył grę!", playerId);
        }

        currentPlayerIndex = (currentPlayerIndex + 1) % maxPlayers;
    }

    private void broadcastMessage(String message, int excludePlayerId) {
        for (int i = 0; i < players.size(); i++) {
            if (i != excludePlayerId) {
                players.get(i).sendMessage(message);
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Podaj liczbę graczy (2, 3, 4, 6): ");
        int maxPlayers = scanner.nextInt();

        if (maxPlayers != 2 && maxPlayers != 3 && maxPlayers != 4 && maxPlayers != 6) {
            System.out.println("Nieprawidłowa liczba graczy.");
            return;
        }

        GameServer server = new GameServer(12345, maxPlayers);
        server.start();
    }
}

class ClientHandler {
    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;

    public ClientHandler(Socket socket, int playerId) throws IOException {
        this.socket = socket;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        sendMessage("Witaj, Graczu " + playerId + "!");
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public String receiveMessage() {
        try {
            return in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}