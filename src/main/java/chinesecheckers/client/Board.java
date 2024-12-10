package chinesecheckers.client;

import chinesecheckers.server.GameServer;

public class Board {
    private int[][] board;
    private static final int BOARD_SIZE = 13;
    private GameServer gameServer;
    public Board(GameServer gameServer) {
        this.gameServer = gameServer;
        board = new int[BOARD_SIZE][BOARD_SIZE];
        initializeBoard();
    }

    private void initializeBoard() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = 1;
            }
        }
    }

    public void updateBoard(String move) {
        System.out.println("Twój ruch " + move);
        gameServer.broadcastMessage("Aktualizacja planszy: " + move);
    }
}