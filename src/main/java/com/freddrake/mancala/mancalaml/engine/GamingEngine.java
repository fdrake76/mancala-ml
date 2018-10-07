package com.freddrake.mancala.mancalaml.engine;

import com.freddrake.mancala.mancalaml.GameBoard;

public interface GamingEngine {
	int chooseMove(GameBoard gameBoard);
	
	void onAfterMove(GameBoard gameBoard);
	
	void onAfterGame(GameBoard gameBoard);

	void onBeforeSession();

	void onAfterSession();
	
	void saveNetwork();
}
