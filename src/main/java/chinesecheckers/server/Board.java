package chinesecheckers.server;

public class Board {
    private int[][] board;
    private static final int BOARD_SIZE = 13;

    public Board() {
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

    public synchronized String processMove(String move, int playerId) {
        if (isValidMove(move)) {
            return "Ruch wykonany: " + move;
        } else {
            return "Nieprawidłowy ruch: " + move;
        }
    }

    private boolean isValidMove(String move) {
        return true;
    }
}