##  NQueen Problem Solver with Multithreading

This project solves the classic N Queen problem using multithreading and provides a graphical user interface (GUI) to visualize the exploration of the solution space. The N Queen problem involves placing N chess queens on an N×N chessboard in such a way that no two queens attack each other.

## Problem Modeling

The N Queen board is represented as an N×N matrix, where each cell can either contain a queen or remain empty. The solution space is explored using multiple threads, with each thread responsible for exploring a specific part of the search space.

## Multithreading and Backtracking

The main thread creates N threads or N/x (where x is the number of processors) to explore the solution space for the board.
Each thread starts with the position of the queen in the first row and employs backtracking to explore possibilities.
Backtracking is used to efficiently explore and prune the search space, avoiding unnecessary computations.

## GUI
Input: N, the board dimension.
Output: Real-time visualization of the exploration of the solution space.
