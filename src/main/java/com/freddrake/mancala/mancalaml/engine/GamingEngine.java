package com.freddrake.mancala.mancalaml.engine;

import com.freddrake.mancala.mancalaml.GameBoard;

public interface GamingEngine {
	int chooseMove(GameBoard gameBoard);
	
	boolean isGameOver(GameBoard gameBoard);

    /**
     * Executes a game move.
     * @param gameBoard
     * @return true if the game is over, false otherwise
     */
	boolean executeMove(GameBoard gameBoard);
}
