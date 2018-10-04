package com.freddrake.mancala.mancalaml;

public interface GamingEngine {
	public int chooseMove(GameBoard gameBoard);
	
	public void onAfterMove(GameBoard gameBoard);
	
	public void onAfterGame(GameBoard gameBoard);
	
	public void loadNetwork();
	
	public void saveNetwork();
}
