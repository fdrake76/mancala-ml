package com.freddrake.mancala.mancalaml.engine;

import com.freddrake.mancala.mancalaml.GameBoard;

public abstract class AbstractGamingEngine implements GamingEngine {

	@Override
	public void onAfterMove(GameBoard gameBoard) {}

	@Override
	public void onAfterGame(GameBoard gameBoard) {}

	@Override
    public void onBeforeSession() {}

	@Override
    public void onAfterSession() {}

	@Override
	public void saveNetwork() {}
}
