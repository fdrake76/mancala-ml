package com.freddrake.mancala.mancalaml.random;

import java.util.List;
import java.util.Random;

import com.freddrake.mancala.mancalaml.AbstractGamingEngine;
import com.freddrake.mancala.mancalaml.GameBoard;
import com.freddrake.mancala.mancalaml.GameBoard.Player;
import com.freddrake.mancala.mancalaml.MancalaException;

import lombok.Builder;

@Builder
public class RandomEngine extends AbstractGamingEngine {
	@Builder.Default private Random random = new Random();
	private Player player;
	
	@Override
	public int chooseMove(GameBoard gameBoard) {
		List<Integer> validMoves = gameBoard.validMoves(player);
		if (validMoves.isEmpty()) {
			throw new MancalaException("Cannot play a move");
		}
		
		return validMoves.get(random.nextInt(validMoves.size()));
	}

}
