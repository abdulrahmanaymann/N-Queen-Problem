import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class ChessboardPanel extends JPanel {
    private final Chessboard chessboard;
    private BufferedImage queenImage;

    public ChessboardPanel() {
        setPreferredSize(new Dimension(400, 400));
        chessboard = new Chessboard(8);

        // Load the queen image
        try {
            queenImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("/image/queen.png")));
        } catch (IOException e) {
            System.out.println("Error loading queen image.");
        }
    }

    public Chessboard getChessboard() {
        return chessboard;
    }

    public void resetBoard(int size) {
        chessboard.resetBoard(size);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int[][] board = chessboard.getBoard();
        int cellSize = Math.min(getWidth() / (chessboard.getSize() + 1), getHeight() / (chessboard.getSize() + 1));

        Font numbersFont = new Font("Arial", Font.BOLD, 16);
        g.setFont(numbersFont);
        g.setColor(Color.BLACK);

        // row numbers on the left side
        for (int row = 0; row < chessboard.getSize(); row++) {
            int x = cellSize / 4;
            int y = (row + 1) * cellSize + cellSize / 2;
            g.drawString(Integer.toString(row + 1), x, y);
        }

        // columns numbers above the board
        for (int col = 0; col < chessboard.getSize(); col++) {
            int x = (col + 1) * cellSize + cellSize / 3;
            int y = cellSize / 2;
            g.drawString(Integer.toString(col + 1), x, y);
        }

        // Draw the chessboard
        for (int row = 0; row < chessboard.getSize(); row++) {
            for (int col = 0; col < chessboard.getSize(); col++) {
                int x = (col + 1) * cellSize;
                int y = (row + 1) * cellSize;

                if ((row + col) % 2 == 0) {
                    g.setColor(new Color(222, 184, 135)); // Light brown color
                } else {
                    g.setColor(new Color(139, 69, 19)); // Dark brown color
                }

                g.fillRect(x, y, cellSize, cellSize);

                if (board[row][col] == 1) {
                    // Draw the queen image
                    int imageWidth = cellSize - 10;
                    int imageHeight = cellSize - 10;
                    g.drawImage(queenImage, x + 5, y + 5, imageWidth, imageHeight, null);
                }
            }
        }
    }

    // Fire when the board is updated
    public void fireChessboardUpdated() {
        repaint();
    }
}
