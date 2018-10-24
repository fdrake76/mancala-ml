package com.freddrake.mancala.mancalaml.engine;

import com.freddrake.mancala.mancalaml.GameBoard;
import com.freddrake.mancala.mancalaml.Player;

public abstract class AbstractGamingEngine implements GamingEngine {
	protected Player player;

	@Override
    public boolean executeMove(GameBoard gameBoard) {
	    boolean playAgain = true;
	    while (playAgain) {
	        if (gameBoard.isGameOver(player)) {
	            // We want to play but game is over.
                return true;
            }
	        int move = chooseMove(gameBoard);
	        playAgain = gameBoard.executeMove(player, move);
        }

        return false;
    }
}
