package com.freddrake.mancala.mancalaml.engine;

import com.freddrake.mancala.mancalaml.GameBoard;
import com.freddrake.mancala.mancalaml.MancalaException;
import com.freddrake.mancala.mancalaml.Player;
import lombok.Builder;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class RandomEngine extends AbstractGamingEngine {
	private Random random;

	@Builder
	private RandomEngine(Random random, @NonNull Player player) {
	    this.random = Optional.ofNullable(random).orElse(new Random());
	    this.player = player;
    }
	
	@Override
	public int chooseMove(GameBoard gameBoard) {
		List<Integer> validMoves = gameBoard.validMoves(player);
		if (validMoves.isEmpty()) {
			throw new MancalaException("Cannot play a move");
		}
		
		return validMoves.get(random.nextInt(validMoves.size()));
	}

    @Override
    public boolean isGameOver(GameBoard gameBoard) {
        return gameBoard.isGameOver(player);
    }


}
