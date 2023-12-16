import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CountDownLatch;

public class NQueensSolverGUI implements Runnable {
    private final ChessboardPanel chessboardPanel;
    private final JTextArea solutionTextArea;
    private final JTextField boardSizeTextField;
    private final CountDownLatch latch;
    private final JFrame frame;
    private JButton startButton;
    // ---------------------------- CONSTANTS FOR SPEED ----------------------------
    private int speed = 2000;
    private final int FAST = 1;
    private final int MODERATE = 500;
    private final int SLOW = 1000;
    // ---------------------------- CONSTANTS FOR SPEED ----------------------------
    private boolean solutionFound = false;
    // lock for the solution
    private final Object lock = new Object();

    public NQueensSolverGUI() {
        this.chessboardPanel = new ChessboardPanel();
        this.latch = new CountDownLatch(1);
        this.frame = new JFrame("N-Queens Solver");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.startButton = new JButton("Start");

        solutionTextArea = new JTextArea();
        solutionTextArea.setFont(new Font("Arial", Font.PLAIN, 14));
        solutionTextArea.setEditable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());

        mainPanel.add(chessboardPanel, BorderLayout.WEST);

        JPanel inputPanel = new JPanel();
        JLabel sizeLabel = new JLabel("Board Size:");
        sizeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        boardSizeTextField = new JTextField(5);

        startButton = new JButton("Start");
        startButton.addActionListener(e -> startSolver());

        JButton slowButton = new JButton("Slow");
        slowButton.addActionListener(e -> setSpeed(SLOW));

        JButton moderateButton = new JButton("Moderate");
        moderateButton.addActionListener(e -> setSpeed(MODERATE));

        JButton fastButton = new JButton("Fast");
        fastButton.addActionListener(e -> setSpeed(FAST));

        inputPanel.add(sizeLabel);
        inputPanel.add(boardSizeTextField);
        inputPanel.add(startButton);
        inputPanel.add(slowButton);
        inputPanel.add(moderateButton);
        inputPanel.add(fastButton);

        mainPanel.add(inputPanel, BorderLayout.NORTH);

        mainPanel.add(new JScrollPane(solutionTextArea), BorderLayout.CENTER);

        this.frame.setLayout(new BorderLayout());
        this.frame.add(mainPanel, BorderLayout.CENTER);

        int panelSize = Math.max(800, chessboardPanel.getSize().height);
        chessboardPanel.setPreferredSize(new Dimension(panelSize, panelSize));

        this.frame.pack();
        this.frame.setLocationRelativeTo(null);

        slowButton.setBackground(Color.YELLOW);
        moderateButton.setBackground(Color.ORANGE);
        fastButton.setBackground(Color.RED);
    }

    private void startSolver() {
        // Disable the start button to prevent multiple clicks
        startButton.setEnabled(false);

        String input = boardSizeTextField.getText().trim();

        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter a valid number for the board size.");

            // Enable the start button since an error occurred
            startButton.setEnabled(true);
            return;
        }

        try {
            int boardSize = Integer.parseInt(input);

            // Validate that the entered number is positive
            if (boardSize <= 0) {
                JOptionPane.showMessageDialog(frame, "Please enter a positive number for the board size.");

                // Enable the start button since an error occurred
                startButton.setEnabled(true);
                return;
            }

            chessboardPanel.resetBoard(boardSize);
            solutionTextArea.setText(""); // Clear previous solutions

            // Run placeQueens on a separate thread to avoid blocking the GUI thread
            Thread solverThread = new Thread(() -> {
                placeQueens(0);
                latch.countDown(); // Signal the end of solving to the GUI thread can exit

                // Enable the start button after solving is completed
                SwingUtilities.invokeLater(() -> startButton.setEnabled(true));
            });
            solverThread.start();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid number format. Please enter a valid integer for the board size.");

            // If error.
            startButton.setEnabled(true);
        }
    }

    @Override
    public void run() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                frame.setVisible(true);
            });
            latch.await(); // Wait for the solver to finish
        } catch (Exception e) {
            System.out.println("GUI thread interrupted.");
        }
    }

    private void placeQueens(int currentCol) {
        // Base case: all queens have been placed
        if (currentCol == chessboardPanel.getChessboard().getSize()) {
            // Increment the solution count and print the solution
            chessboardPanel.getChessboard().incrementSolutionCount();
            printSolution();

            // Set the solution found flag
            solutionFound = true;

            // Show a message dialog
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(frame, "Solution found!");
                synchronized (lock) {
                    solutionFound = false;
                    lock.notify(); // Notify waiting thread (if any) that the solution is acknowledged
                }
            });

            // Wait until the user clicks "OK"
            waitForUserInput();

            return;
        }

        for (int row = 0; row < chessboardPanel.getChessboard().getSize(); row++) {
            if (chessboardPanel.getChessboard().isSafe(row, currentCol)) {
                chessboardPanel.getChessboard().placeQueen(row, currentCol);
                chessboardPanel.fireChessboardUpdated();
                waitForEvent();
                placeQueens(currentCol + 1);

                chessboardPanel.getChessboard().removeQueen(row, currentCol);
                chessboardPanel.fireChessboardUpdated();
                waitForEvent();

                // Check if the solution has been found and pause the solver
                if (solutionFound) {
                    waitForUserInput();
                }
            }
        }
    }

    private void waitForUserInput() {
        synchronized (lock) {
            while (solutionFound) {
                try {
                    lock.wait(); // Release the lock and wait
                } catch (InterruptedException e) {
                    System.out.println("Solver thread interrupted.");
                }
            }
        }
    }

    private void printSolution() {
        StringBuilder solutionText = new StringBuilder();
        solutionText.append("Number of solutions found: ")
                .append(chessboardPanel.getChessboard().getSolutionCount())
                .append("\n");

        solutionText.append("[");
        for (int col = 0; col < chessboardPanel.getChessboard().getSize(); col++) {
            solutionText.append(chessboardPanel.getChessboard().getQueenRow(col) + 1);
            if (col < chessboardPanel.getChessboard().getSize() - 1) {
                solutionText.append(" ");
            }
        }
        solutionText.append(" ]\n\n");

        SwingUtilities.invokeLater(() -> solutionTextArea.append(solutionText.toString()));
    }

    // event to wait for the thread to repaint the board
    private void waitForEvent() {
        try {
            Thread.sleep(speed);
        } catch (InterruptedException e) {
            System.out.println("Solver thread interrupted.");
        }
    }

    // set the speed of the solver
    private void setSpeed(int newSpeed) {
        try {
            speed = newSpeed;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid number format. Please enter a valid integer for the board size.");
        }
    }

    // ---------------------------- MAIN METHOD ----------------------------
    public static void nQueensSolver() {
        SwingUtilities.invokeLater(() -> {
            NQueensSolverGUI nQueensSolverGUI = new NQueensSolverGUI();
            Thread guiThread = new Thread(nQueensSolverGUI);
            guiThread.start();
        });
    }
}
