package com.example.java2_a2;

public class Game {
    int[][] chessboard = new int[3][3];
    int turn = 1;

    public int[][] getChessboard() {
        return chessboard.clone();
    }

    public int getTurn() {
        return turn;
    }

    public int winCheck() {
        boolean haveZero = false;
        // row & col
        for (int i = 0; i < 3; i++) {
            int rowStatus = 0;
            int colStatus = 0;
            for (int j = 0; j < 3; j++) {
                rowStatus += chessboard[i][j];
                colStatus += chessboard[j][i];
                haveZero |= chessboard[i][j] == 0;
            }
            if (rowStatus == 3 || colStatus == 3) return 1;
            if (rowStatus == -3 || colStatus == -3) return -1;
        }

        // diag
        for (int code : new int[]{1, -1}) {
            if (chessboard[0][0] == code && chessboard[1][1] == code && chessboard[2][2] == code)
                return code;
            if (chessboard[0][2] == code && chessboard[1][1] == code && chessboard[2][0] == code)
                return code;
        }

        if (!haveZero) return 2;
        return 0;
    }

    public boolean play(int i, int j, int player) {
        if (winCheck() != 0) return false;
        if (chessboard[i][j] == 0 && turn == player) {
            chessboard[i][j] = player;
            turn *= -1;
            return true;
        }
        return false;
    }

    public void restart() {
        chessboard = new int[3][3];
    }
}
