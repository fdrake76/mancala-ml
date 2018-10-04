package com.freddrake.mancala.mancalaml;

public abstract class AbstractGamingEngine implements GamingEngine {

	@Override
	public void onAfterMove(GameBoard gameBoard) {}

	@Override
	public void onAfterGame(GameBoard gameBoard) {}
	
	@Override
	public void loadNetwork() {}
	
	@Override
	public void saveNetwork() {}
}
