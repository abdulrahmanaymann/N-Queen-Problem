import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Chessboard {
    private int[][] board;
    private int size;

    private volatile boolean isSolutionFound = false;

    // locks for the board and solution
    private final Lock solutionLock = new ReentrantLock();
    private final Lock boardLock = new ReentrantLock();

    // atomic integer to keep track of the number of solutions found
    private final AtomicInteger solutionCount = new AtomicInteger(0);


    public Chessboard(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Board size must be greater than 0");
        }
        this.size = size;
        this.board = new int[size][size];
        initializeBoard();
    }


    private void initializeBoard() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = 0;
            }
        }
    }

    public void resetBoard(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Board size must be greater than 0");
        }

        boardLock.lock();
        try {
            this.size = size;
            this.board = new int[size][size];
            initializeBoard();
            isSolutionFound = false;
        } finally {
            boardLock.unlock();
        }

        solutionLock.lock();
        try {
            solutionCount.set(0);
        } finally {
            solutionLock.unlock();
        }
    }

    public void incrementSolutionCount() {
        solutionCount.incrementAndGet();
    }

    public int getSolutionCount() {
        return solutionCount.get();
    }

    // Package-private method.
    boolean isSafe(int row, int col) {
        return !isRowSafe(row) && !isColumnSafe(col) && !isDiagonalSafe(row, col);
    }

    private boolean isRowSafe(int row) {
        boardLock.lock();
        try {
            if (row < 0 || row >= size) {
                return false;
            }

            for (int j = 0; j < size; j++) {
                if (board[row][j] == 1) {
                    return true;
                }
            }
            return false;
        } finally {
            boardLock.unlock();
        }
    }

    private boolean isColumnSafe(int col) {
        boardLock.lock();
        try {
            if (col < 0 || col >= size) {
                return false;
            }
            for (int i = 0; i < size; i++) {
                if (board[i][col] == 1) {
                    return true;
                }
            }
            return false;
        } finally {
            boardLock.unlock();
        }
    }


    private boolean isDiagonalSafe(int row, int col) {
        int[][] diagonalOffsets = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        boardLock.lock();
        try {
            for (int[] offset : diagonalOffsets) {
                if (isDiagonalAttack(row, col, offset[0], offset[1])) {
                    return true;
                }
            }
            return false;
        } finally {
            boardLock.unlock();
        }
    }

    private boolean isDiagonalAttack(int row, int col, int deltaRow, int deltaCol) {
        int i = row + deltaRow;
        int j = col + deltaCol;
        while (i >= 0 && i < size && j >= 0 && j < size) {
            if (board[i][j] == 1) {
                return true;
            }
            i += deltaRow;
            j += deltaCol;
        }
        return false;
    }

    public int getSize() {
        return size;
    }

    public void placeQueen(int row, int col) {
        boardLock.lock();
        try {
            if (!isSolutionFound && isValidPosition(row, col)) {
                board[row][col] = 1;
            }
        } finally {
            boardLock.unlock();
        }
    }

    public void removeQueen(int row, int col) {
        boardLock.lock();
        try {
            if (!isSolutionFound && isValidPosition(row, col)) {
                board[row][col] = 0;
            }
        } finally {
            boardLock.unlock();
        }
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < size && col >= 0 && col < size;
    }

    public int[][] getBoard() {
        boardLock.lock();
        try {
            int[][] copy = new int[size][size];
            for (int i = 0; i < size; i++) {
                System.arraycopy(board[i], 0, copy[i], 0, size);
            }
            return copy;
        } finally {
            boardLock.unlock();
        }
    }

    public void printBoard() {
        System.out.println("------------------------------");
        System.out.print("   ");
        for (int c = 0; c < size; c++) {
            System.out.print(" " + (c + 1) + " ");
        }
        System.out.println();

        for (int r = 0; r < size; r++) {
            System.out.print(" " + (r + 1) + " ");
            for (int c = 0; c < size; c++) {
                System.out.print("|" + (board[r][c] == 1 ? "Q" : " ") + "|");
            }
            System.out.println();
        }
        System.out.println("------------------------------");
    }

    public int getQueenRow(int col) {
        boardLock.lock();
        try {
            for (int i = 0; i < size; i++) {
                if (board[i][col] == 1) {
                    return i;
                }
            }
            return -1;
        } finally {
            boardLock.unlock();
        }
    }

}
